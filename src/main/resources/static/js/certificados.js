// Descripciones oficiales para los tipos de certificados
const descripcionesCertificados = {
    'Certificado de estudios': {
        titulo: 'Certificado de estudios',
        body: 'Documento oficial que acredita los grados académicos que el estudiante ya ha cursado y aprobado exitosamente en la institución.'
    },
    'Certificado de notas': {
        titulo: 'Certificado de notas',
        body: 'Detalla las calificaciones e informe específico de rendimiento de cada asignatura por período académico o año lectivo.'
    },
    'Copia del acta de grado o diploma': {
        titulo: 'Copia del acta de grado o diploma',
        body: 'Se solicita si el alumno ya se graduó de bachiller y requiere una reposición del original o soportes para ingreso a la universidad.'
    },
    'Constancia de matrícula': {
        titulo: 'Constancia de matrícula',
        body: 'Confirma que el estudiante se encuentra activo y matriculado en el año lectivo en curso. Sirve para trámites de subsidios, Cajas de Compensación o EPS.'
    },
    'Paz y salvo': {
        titulo: 'Paz y salvo institucional',
        body: 'Demuestra que el estudiante se encuentra al día con sus compromisos financieros (pensión, matrícula, biblioteca y materiales).'
    },
    'Certificado de comportamiento o conducta': {
        titulo: 'Certificado de comportamiento o conducta',
        body: 'Describe el desempeño social, valores y registro disciplinario del estudiante según el observador del alumno.'
    }
};

function actualizarDescripcionCert(tipo) {
    const info = descripcionesCertificados[tipo];
    if (info) {
        document.getElementById('desc-title').innerText = info.titulo;
        document.getElementById('desc-body').innerText = info.body;
    }
}

// Cambio de Rol Simulado (Estudiante vs Admin)
function cambiarRolSimulado(rol) {
    const btnEst = document.getElementById('btn-rol-estudiante');
    const btnAdmin = document.getElementById('btn-rol-admin');
    const vEst = document.getElementById('vista-estudiante');
    const vAdmin = document.getElementById('vista-admin');

    if (rol === 'estudiante') {
        btnEst.className = "px-3.5 py-2 rounded-lg text-xs font-black transition-all flex items-center gap-2 cursor-pointer bg-sidebar text-white shadow-2xs";
        btnAdmin.className = "px-3.5 py-2 rounded-lg text-xs font-bold text-text-secondary hover:text-sidebar hover:bg-[#f7fcf6] transition-all flex items-center gap-2 cursor-pointer";
        vEst.classList.remove('hidden');
        vAdmin.classList.add('hidden');
    } else {
        btnAdmin.className = "px-3.5 py-2 rounded-lg text-xs font-black transition-all flex items-center gap-2 cursor-pointer bg-sidebar text-white shadow-2xs";
        btnEst.className = "px-3.5 py-2 rounded-lg text-xs font-bold text-text-secondary hover:text-sidebar hover:bg-[#f7fcf6] transition-all flex items-center gap-2 cursor-pointer";
        vAdmin.classList.remove('hidden');
        vEst.classList.add('hidden');
    }
}

// Modal Ver Respuesta para Estudiante
function openModalVerRespuesta(btn) {
    document.getElementById('vr-codigo').innerText = btn.dataset.id;
    document.getElementById('vr-titulo').innerText = btn.dataset.tipo;
    document.getElementById('vr-mensaje').innerText = btn.dataset.mensaje || 'Se adjunta el certificado solicitado.';

    const archivoUrl = btn.dataset.archivo;
    const cajaAdjunto = document.getElementById('caja-archivo-adjunto');
    const btnDescargar = document.getElementById('btn-descargar-archivo');
    const nombreArchivo = document.getElementById('vr-nombre-archivo');

    if (archivoUrl && archivoUrl !== 'null' && archivoUrl !== 'undefined' && archivoUrl !== '') {
        cajaAdjunto.classList.remove('hidden');
        btnDescargar.href = archivoUrl;
        btnDescargar.setAttribute('target', '_blank');

        let cleanName = 'Certificado_Oficial.pdf';
        if (archivoUrl.includes('key=')) {
            cleanName = archivoUrl.split('key=')[1].split('/').pop();
        } else if (archivoUrl.includes('/')) {
            cleanName = archivoUrl.split('/').pop();
        }
        nombreArchivo.innerText = btn.dataset.archivoNombre || cleanName;
    } else {
        cajaAdjunto.classList.add('hidden');
    }

    const modal = document.getElementById('modal-ver-respuesta');
    const dialog = document.getElementById('dialog-ver-respuesta');
    modal.classList.remove('hidden');
    setTimeout(() => {
        dialog.classList.remove('scale-95', 'opacity-0');
        dialog.classList.add('scale-100', 'opacity-100');
    }, 10);
}

function closeModalVerRespuesta() {
    const modal = document.getElementById('modal-ver-respuesta');
    const dialog = document.getElementById('dialog-ver-respuesta');
    dialog.classList.remove('scale-100', 'opacity-100');
    dialog.classList.add('scale-95', 'opacity-0');
    setTimeout(() => {
        modal.classList.add('hidden');
    }, 300);
}

// Modal Responder para Personal Administrativo
function openModalResponder(btn) {
    document.getElementById('resp-solicitud-id').value = btn.dataset.id;
    document.getElementById('resp-codigo').innerText = btn.dataset.id;
    document.getElementById('resp-estudiante-nombre').innerText = btn.dataset.estudiante || 'Estudiante';
    document.getElementById('resp-tipo-cert').innerText = btn.dataset.tipo || 'Certificado';
    document.getElementById('resp-motivo-txt').innerText = 'Motivo: ' + (btn.dataset.motivo || 'N/A');

    const modal = document.getElementById('modal-responder-solicitud');
    const dialog = document.getElementById('dialog-responder');
    modal.classList.remove('hidden');
    setTimeout(() => {
        dialog.classList.remove('scale-95', 'opacity-0');
        dialog.classList.add('scale-100', 'opacity-100');
    }, 10);
}

function closeModalResponder() {
    const modal = document.getElementById('modal-responder-solicitud');
    const dialog = document.getElementById('dialog-responder');
    dialog.classList.remove('scale-100', 'opacity-100');
    dialog.classList.add('scale-95', 'opacity-0');
    setTimeout(() => {
        modal.classList.add('hidden');
    }, 300);
}

function actualizarNombreFileAdjunto(input) {
    if (input.files && input.files[0]) {
        document.getElementById('txt-file-adjunto').innerText = input.files[0].name;
    }
}

// Filtrar Solicitudes Admin
function filtrarTablaAdmin(filtro) {
    document.querySelectorAll('.row-solicitud-admin').forEach(tr => {
        if (filtro === 'todos' || tr.dataset.estado === filtro) {
            tr.classList.remove('hidden');
        } else {
            tr.classList.add('hidden');
        }
    });
}
