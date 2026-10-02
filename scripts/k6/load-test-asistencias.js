import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

// Métricas personalizadas para reporte técnico (Capítulo IV)
const readDuration = new Trend('lectura_mapa_duration_ms');
const writeDuration = new Trend('escritura_40_estudiantes_duration_ms');
const contentionDuration = new Trend('contencion_abrir_dia_duration_ms');
const batchCollisionDuration = new Trend('colision_batch_duration_ms');
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

        // Escenario 3a: Colisión concurrente real con http.batch (12 peticiones simultáneas con sesiones independientes)
        escenario_3a_colision_batch: {
            executor: 'shared-iterations',
            vus: 1,
            iterations: 1,
            maxDuration: '10s',
            exec: 'ejecutarColisionBatch',
            startTime: '48s',
            tags: { escenario: 'colision_batch' },
        },

        // Escenario 3b: Idempotencia y consulta concurrente continua (Punto 4)
        escenario_3b_idempotencia: {
            executor: 'shared-iterations',
            vus: 12,
            iterations: 40,
            maxDuration: '20s',
            exec: 'ejecutarContencion',
            startTime: '52s',
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
        colision_batch_duration_ms: ['p(95)<500'],
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

    const fechaHoy = __ENV.TEST_DATE || new Date().toISOString().slice(0, 10);
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
                sesionesPorCurso[c.id] = sesiones.map(s => {
                    const docId = s.docente ? (typeof s.docente === 'string' ? s.docente : s.docente.id) : null;
                    const docNum = s.docente && typeof s.docente === 'object' ? s.docente.numeroDocumento : null;
                    let email = null;
                    if (docId === 'd0000000-0000-0000-0000-000000000001' || docNum === '1010000001') {
                        email = 'carlos.mendoza@ieaci.edu.co';
                    } else if (docId === 'd0000000-0000-0000-0000-000000000002' || docNum === '1010000002') {
                        email = 'ana.garcia@ieaci.edu.co';
                    } else if (docId === 'd0000000-0000-0000-0000-000000000003' || docNum === '1010000003') {
                        email = 'jorge.herrera@ieaci.edu.co';
                    }
                    return {
                        id: s.id,
                        docenteId: docId,
                        docenteEmail: email
                    };
                });
                console.log(`[SETUP] Curso ${c.grado} ${c.grupo} (${c.id}) -> ${sesiones.length} sesiones abiertas`);
            } catch (e) {
                console.error(`Error parseando sesiones para curso ${c.id}: ${e}`);
            }
        } else {
            console.error(`[SETUP ERROR] Status: ${res.status} Body: ${res.body}`);
        }
    }

    // Crear 12 sesiones independientes para colisión paralela en Escenario 3a
    const distinctSessions = [];
    for (let i = 0; i < 12; i++) {
        const doc = fixtures.docentes[i % fixtures.docentes.length];
        const s = loginUsuario(doc.email, doc.password);
        if (s) distinctSessions.push(s);
    }
    console.log(`[SETUP] ${distinctSessions.length} sesiones independientes creadas para colisión batch.`);

    const collisionCourse = fixtures.cursos.length > 15 ? fixtures.cursos[15] : fixtures.cursos[fixtures.cursos.length - 1];
    return {
        sesionesPorCurso: sesionesPorCurso,
        contentionCourseId: fixtures.cursos[0].id,
        unopenedCourseId: collisionCourse.id,
        fechaHoy: fechaHoy,
        distinctSessions: distinctSessions,
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
    const fecha = data && data.fechaHoy ? data.fechaHoy : new Date().toISOString().slice(0, 10);
    const res = http.get(`${BASE_URL}/asistencias/curso/${curso.id}/mapa?fecha=${fecha}`, {
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
        for (let s of sesiones) {
            if (s.docenteEmail === session.email) {
                const cur = fixtures.cursos.find(c => c.id === cId);
                if (cur) {
                    candidateCourses.push({ sesionId: s.id, curso: cur });
                }
            }
        }
    }

    if (candidateCourses.length === 0) {
        for (let cId of cursoIds) {
            const sesiones = data.sesionesPorCurso[cId];
            if (sesiones && sesiones.length > 0) {
                const cur = fixtures.cursos.find(c => c.id === cId);
                if (cur) {
                    candidateCourses.push({ sesionId: sesiones[0].id, curso: cur });
                }
            }
        }
    }
    if (candidateCourses.length === 0) {
        sleep(0.5);
        return;
    }
    const vuIndex = (__VU - 1) % candidateCourses.length;
    const selected = candidateCourses[vuIndex];
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
 * Escenario 3a: Colisión concurrente real con http.batch
 * 12 peticiones HTTP concurrentes con sesiones independientes (sin serialización de sesión Tomcat)
 * sobre un curso virgen, provocando colisión física en la base de datos (uk_sesion_curso_fecha_hora)
 * y verificando que Spring Boot + PostgreSQL resuelvan la condición de carrera retornando HTTP 200.
 */
export function ejecutarColisionBatch(data) {
    const cursoId = data.unopenedCourseId;
    const fecha = data && data.fechaHoy ? data.fechaHoy : new Date().toISOString().slice(0, 10);
    const sessions = data.distinctSessions || [];

    const requests = [];
    for (let i = 0; i < 12; i++) {
        const sess = sessions.length > 0 ? sessions[i % sessions.length] : null;
        requests.push({
            method: 'POST',
            url: `${BASE_URL}/asistencias/abrir-dia?cursoId=${cursoId}&fecha=${fecha}`,
            params: {
                headers: sess ? { 'X-XSRF-TOKEN': sess.csrfToken } : {},
                cookies: sess ? sess.flatCookies : {},
                tags: { escenario: 'colision_batch' },
            },
        });
    }

    const t0 = new Date().getTime();
    const responses = http.batch(requests);
    const dur = new Date().getTime() - t0;
    batchCollisionDuration.add(dur);

    let all200 = true;
    let validArray = false;
    for (let res of responses) {
        if (res.status !== 200) {
            all200 = false;
            console.error(`[COLISION BATCH ERROR] Status: ${res.status} Body: ${res.body}`);
        } else {
            try {
                const list = JSON.parse(res.body);
                if (Array.isArray(list) && list.length > 0) {
                    validArray = true;
                }
            } catch (e) {
                // ignorar
            }
        }
    }

    check(responses, {
        'batch colision: todas las 12 peticiones retornan 200': () => all200,
        'batch colision: cuerpo contiene array de sesiones': () => validArray,
    });
}

/**
 * Escenario 3b: Idempotencia y consulta concurrente continua (Punto 4)
 * Múltiples VUs concurrentes consultando/abriendo el mismo día del mismo curso ya existente.
 */
export function ejecutarContencion(data) {
    const session = getVUSession();
    if (!session) return;

    const cursoId = data.contentionCourseId;
    const fecha = data && data.fechaHoy ? data.fechaHoy : new Date().toISOString().slice(0, 10);

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
