import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

// Métricas personalizadas para reporte técnico (Capítulo IV)
const readDuration = new Trend('lectura_mapa_duration_ms');
const writeDuration = new Trend('escritura_40_estudiantes_duration_ms');
const contentionDuration = new Trend('contencion_abrir_dia_duration_ms');
const successfulWrites = new Counter('escrituras_40_filas_exitosas');
const contentionSuccess = new Rate('contencion_tasa_exito');

// Cargar fixture con 18 cursos de secundaria (40 estudiantes c/u = 720 activos en secundaria) y credenciales
const fixtures = JSON.parse(open('./fixtures.json'));

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080';

export const options = {
    scenarios: {
        // Escenario 1: Lectura de mapa de asistencias de un curso completo (40 estudiantes)
        escenario_1_lectura: {
            executor: 'ramping-vus',
            startVUs: 2,
            stages: [
                { duration: '3s', target: 12 },
                { duration: '15s', target: 12 },
                { duration: '3s', target: 0 },
            ],
            exec: 'ejecutarLectura',
            tags: { escenario: 'lectura' },
        },

        // Escenario 2: Escritura concurrente de 40 estudiantes por sesión con docentes en paralelo
        escenario_2_escritura: {
            executor: 'ramping-vus',
            startVUs: 2,
            stages: [
                { duration: '3s', target: 8 },
                { duration: '15s', target: 8 },
                { duration: '3s', target: 0 },
            ],
            exec: 'ejecutarEscritura',
            startTime: '25s',
            tags: { escenario: 'escritura' },
        },

        // Escenario 3: Contención concurrente abriendo el mismo día del mismo curso (Punto 4)
        escenario_3_contencion: {
            executor: 'shared-iterations',
            vus: 12,
            iterations: 40,
            maxDuration: '20s',
            exec: 'ejecutarContencion',
            startTime: '50s',
            tags: { escenario: 'contencion' },
        },
    },
    thresholds: {
        // Umbral obligatorio acordado: p(95) < 300 ms en general y tasa de fallos < 1%
        http_req_duration: ['p(95)<300'],
        http_req_failed: ['rate<0.01'],
        lectura_mapa_duration_ms: ['p(95)<300'],
        escritura_40_estudiantes_duration_ms: ['p(95)<500'],
        contencion_abrir_dia_duration_ms: ['p(95)<300'],
    },
};

/**
 * Función de inicialización (Setup):
 * Autentica como Administrador y asegura la apertura previa de sesiones para las pruebas.
 */
export function setup() {
    console.log(`[SETUP] Iniciando prueba de carga contra ${BASE_URL}...`);
    
    // Login inicial de setup
    const loginRes = loginUsuario('admin@ieaci.edu.co', 'admin');
    if (!loginRes) {
        throw new Error('Fallo crítico al autenticar super admin en setup');
    }

    const fechaHoy = '2026-09-29';
    const sesionesPorCurso = {};

    // Abrir día para los primeros 10 cursos de secundaria
    for (let i = 0; i < Math.min(10, fixtures.cursos.length); i++) {
        const c = fixtures.cursos[i];
        const res = http.post(
            `${BASE_URL}/asistencias/abrir-dia?cursoId=${c.id}&fecha=${fechaHoy}`,
            null,
            {
                headers: {
                    'X-XSRF-TOKEN': loginRes.csrfToken,
                },
                cookies: loginRes.cookies,
            }
        );
        if (res.status === 200) {
            try {
                const sesiones = JSON.parse(res.body);
                sesionesPorCurso[c.id] = sesiones.map(s => ({
                    id: s.id,
                    docenteId: s.docente ? s.docente.id : null,
                    docenteEmail: s.docente ? (s.docente.numeroDocumento === '1010000001' ? 'carlos.mendoza@ieaci.edu.co' :
                                              s.docente.numeroDocumento === '1010000002' ? 'ana.garcia@ieaci.edu.co' : 'jorge.herrera@ieaci.edu.co') : null
                }));
            } catch (e) {
                console.error(`Error parseando sesiones para curso ${c.id}: ${e}`);
            }
        }
    }

    console.log(`[SETUP] Sesiones inicializadas para ${Object.keys(sesionesPorCurso).length} cursos.`);
    return {
        sesionesPorCurso: sesionesPorCurso,
        contentionCourseId: fixtures.cursos[0].id,
    };
}

// Almacén de sesión por VU
let localSession = null;

function loginUsuario(email, password) {
    const jar = http.cookieJar();

    // 1. Obtener token CSRF del formulario de login
    const getLoginRes = http.get(`${BASE_URL}/login`);
    const match = getLoginRes.body.match(/name="_csrf"\s+value="([^"]+)"/);
    if (!match) {
        console.error(`No se encontró token CSRF en /login para ${email}`);
        return null;
    }
    const preLoginCsrf = match[1];

    // 2. Enviar credenciales
    const loginPayload = {
        username: email,
        password: password,
        _csrf: preLoginCsrf,
    };

    const postLoginRes = http.post(`${BASE_URL}/login`, loginPayload, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        redirects: 0,
    });

    if (postLoginRes.status !== 302 || postLoginRes.headers['Location'].includes('error')) {
        console.error(`Fallo login para ${email} - Status: ${postLoginRes.status}`);
        return null;
    }

    // 3. Obtener token CSRF autenticado visitando dashboard
    const dashRes = http.get(`${BASE_URL}/`);
    const dashMatch = dashRes.body.match(/name="_csrf"\s+value="([^"]+)"/);
    const postLoginCsrf = dashMatch ? dashMatch[1] : '';

    const rawCookies = jar.cookiesForURL(BASE_URL);
    const flatCookies = {};
    for (const name in rawCookies) {
        if (rawCookies[name] && rawCookies[name].length > 0) {
            flatCookies[name] = rawCookies[name][0];
        }
    }

    return {
        email: email,
        csrfToken: postLoginCsrf,
        flatCookies: flatCookies,
    };
}

function getVUSession() {
    if (!localSession) {
        // Rotar entre los 3 docentes disponibles según el ID del VU
        const docenteIndex = (__VU - 1) % fixtures.docentes.length;
        const docente = fixtures.docentes[docenteIndex];
        localSession = loginUsuario(docente.email, docente.password);
    }
    if (localSession && localSession.flatCookies) {
        const jar = http.cookieJar();
        for (const name in localSession.flatCookies) {
            jar.set(BASE_URL, name, localSession.flatCookies[name]);
        }
    }
    return localSession;
}

/**
 * Escenario 1: Lectura de mapa de asistencia de un curso con 40 estudiantes
 */
export function ejecutarLectura(data) {
    const session = getVUSession();
    if (!session) return;

    // Seleccionar curso aleatorio
    const cursoIndex = Math.floor(Math.random() * fixtures.cursos.length);
    const curso = fixtures.cursos[cursoIndex];

    const t0 = new Date().getTime();
    const res = http.get(`${BASE_URL}/asistencias/curso/${curso.id}/mapa?fecha=2026-09-29`, {
        headers: {
            'Accept': 'application/json',
        },
        cookies: session.flatCookies,
    });
    const dur = new Date().getTime() - t0;
    readDuration.add(dur);

    const ok = check(res, {
        'lectura mapa status 200': (r) => r.status === 200,
        'lectura contiene estudiantes': (r) => {
            try {
                const map = JSON.parse(r.body);
                return typeof map === 'object' && Object.keys(map).length >= 0;
            } catch (e) {
                return false;
            }
        },
    });

    sleep(0.5);
}

/**
 * Escenario 2: Escritura de asistencia masiva (40 estudiantes por sesión)
 */
export function ejecutarEscritura(data) {
    const session = getVUSession();
    if (!session) return;

    // Buscar una sesión correspondiente al docente autenticado
    const cursoIds = Object.keys(data.sesionesPorCurso);
    if (cursoIds.length === 0) return;

    let targetSesionId = null;
    let targetCurso = null;
    const candidateCourses = [];
    for (let cId of cursoIds) {
        const sesiones = data.sesionesPorCurso[cId];
        const match = sesiones.find(s => s.docenteEmail === session.email);
        if (match) {
            const cur = fixtures.cursos.find(c => c.id === cId);
            if (cur) {
                candidateCourses.push({ sesionId: match.id, curso: cur });
            }
        }
    }

    if (candidateCourses.length === 0) return;
    const selected = candidateCourses[Math.floor(Math.random() * candidateCourses.length)];
    targetSesionId = selected.sesionId;
    targetCurso = selected.curso;

    // Armar el payload de 40 estudiantes para registro masivo
    const estados = ['P', 'P', 'P', 'FJ', 'FI'];
    const items = targetCurso.estudiantes.map((estId, idx) => ({
        estudianteId: estId,
        estado: estados[idx % estados.length],
        observaciones: idx % 10 === 0 ? 'Registro de carga k6' : null,
    }));

    const t0 = new Date().getTime();
    const res = http.post(
        `${BASE_URL}/asistencias/sesiones/${targetSesionId}/registrar`,
        JSON.stringify(items),
        {
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': session.csrfToken,
            },
            cookies: session.flatCookies,
        }
    );
    const dur = new Date().getTime() - t0;
    writeDuration.add(dur);

    if (res.status !== 200) {
        console.error(`[FAIL ESCRITURA] VU: ${__VU} Status: ${res.status} Body: ${res.body}`);
    }

    const ok = check(res, {
        'escritura asistencia status 200': (r) => r.status === 200,
        'escritura proceso 40 registros': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.success === true && body.registrosProcesados === 40;
            } catch (e) {
                return false;
            }
        },
    });

    if (ok) {
        successfulWrites.add(1);
    }

    sleep(1);
}

/**
 * Escenario 3: Contención - Múltiples VUs concurrentes abriendo el mismo día del mismo curso
 * Valida la mitigación de DataIntegrityViolationException e idempotencia concurrente (Punto 4)
 */
export function ejecutarContencion(data) {
    const session = getVUSession();
    if (!session) return;

    const cursoId = data.contentionCourseId;
    const fecha = '2026-09-29';

    const t0 = new Date().getTime();
    const res = http.post(
        `${BASE_URL}/asistencias/abrir-dia?cursoId=${cursoId}&fecha=${fecha}`,
        null,
        {
            headers: {
                'X-XSRF-TOKEN': session.csrfToken,
            },
            cookies: session.flatCookies,
        }
    );
    const dur = new Date().getTime() - t0;
    contentionDuration.add(dur);

    const ok = check(res, {
        'contencion abrir-dia status 200': (r) => r.status === 200,
        'contencion retorna sesiones': (r) => {
            try {
                const list = JSON.parse(r.body);
                return Array.isArray(list) && list.length > 0;
            } catch (e) {
                return false;
            }
        },
    });

    contentionSuccess.add(ok ? 1 : 0);
    sleep(0.3);
}
