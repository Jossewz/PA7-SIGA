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

// Formulario Estudiante: Enviar Solicitud
function handleFormSolicitar(e) {
    e.preventDefault();
    const tipo = document.getElementById('select-tipo-cert').value;
    const motivo = document.getElementById('c-motivo').value;
    const fechaHoy = new Date().toISOString().split('T')[0];
    const numCod = Math.floor(100 + Math.random() * 900);
    const codId = 'CERT-2026-' + numCod;

    // Agregar a la tabla de solicitudes del estudiante
    const tbodyEst = document.getElementById('tbody-solicitudes-estudiante');
    const trEst = document.createElement('tr');
    trEst.className = "hover:bg-[#fcfdfb] transition-colors";
    trEst.innerHTML = `
        <td class="p-3.5 pl-5 font-black text-sidebar text-[11px]">${codId}</td>
        <td class="p-3.5">
            <div class="font-black text-text-primary text-[12px]">${tipo}</div>
            <div class="text-[10px] font-semibold text-text-secondary">${motivo}</div>
        </td>
        <td class="p-3.5 font-semibold text-text-secondary">${fechaHoy}</td>
        <td class="p-3.5 text-center">
            <span class="px-2.5 py-1 rounded-full text-[10px] font-black uppercase tracking-wide bg-amber-100 text-amber-800 border border-amber-300">Pendiente</span>
        </td>
        <td class="p-3.5 pr-5 text-right">
            <span class="text-[11px] font-bold text-text-secondary italic">En proceso...</span>
        </td>
    `;
    tbodyEst.prepend(trEst);

    // Agregar a la bandeja del Admin
    const tbodyAdmin = document.getElementById('tbody-admin-solicitudes');
    const trAdmin = document.createElement('tr');
    trAdmin.className = "hover:bg-[#fcfdfb] transition-colors row-solicitud-admin";
    trAdmin.setAttribute('data-estado', 'Pendiente');
    trAdmin.innerHTML = `
        <td class="p-3.5 pl-5 font-black text-sidebar text-[11px]">${codId}</td>
        <td class="p-3.5">
            <div class="font-black text-text-primary text-[12px]">Mateo Álvarez Restrepo</div>
            <div class="text-[10px] font-semibold text-text-secondary">Doc: 1098432101 • 11° - 01</div>
        </td>
        <td class="p-3.5">
            <div class="font-bold text-sidebar text-[12px]">${tipo}</div>
            <div class="text-[10px] font-semibold text-text-secondary">${motivo}</div>
        </td>
        <td class="p-3.5 font-semibold text-text-secondary">${fechaHoy}</td>
        <td class="p-3.5 text-center">
            <span class="px-2.5 py-1 rounded-full text-[10px] font-black uppercase tracking-wide bg-amber-100 text-amber-800 border border-amber-300">Pendiente</span>
        </td>
        <td class="p-3.5 pr-5 text-right">
            <button type="button" 
                    data-id="${codId}"
                    data-estudiante="Mateo Álvarez Restrepo"
                    data-tipo="${tipo}"
                    data-motivo="${motivo}"
                    data-estado="Pendiente"
                    onclick="openModalResponder(this)"
                    class="px-3.5 py-1.5 text-[11px] font-black text-white bg-sidebar hover:brightness-97 rounded-lg transition-all flex items-center gap-1.5 ml-auto cursor-pointer shadow-2xs">
                <i data-lucide="message-square-plus" class="size-3.5 stroke-[2.5]"></i>
                <span>Atender Solicitud</span>
            </button>
        </td>
    `;
    tbodyAdmin.prepend(trAdmin);

    document.getElementById('form-solicitar-certificado').reset();
    if (window.lucide) lucide.createIcons();
    alert('¡Solicitud de ' + tipo + ' enviada correctamente a Secretaría!');
}

// Modal Ver Respuesta para Estudiante
function openModalVerRespuesta(btn) {
    document.getElementById('vr-codigo').innerText = btn.dataset.id;
    document.getElementById('vr-titulo').innerText = btn.dataset.tipo;
    document.getElementById('vr-mensaje').innerText = btn.dataset.mensaje || 'Se adjunta el certificado solicitado.';
    document.getElementById('vr-nombre-archivo').innerText = btn.dataset.archivo || 'Certificado_Oficial.pdf';
    
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

// Guardar Respuesta del Admin
function handleFormResponder(e) {
    e.preventDefault();
    const id = document.getElementById('resp-solicitud-id').value;
    const estado = document.getElementById('resp-estado-select').value;
    const mensaje = document.getElementById('resp-mensaje-txt').value;
    const fileInput = document.getElementById('resp-file-input');
    const nombreArchivo = (fileInput.files && fileInput.files[0]) ? fileInput.files[0].name : 'Certificado_Firmado_IEACI.pdf';

    // Actualizar la fila en la tabla de Admin
    document.querySelectorAll('.row-solicitud-admin').forEach(tr => {
        const btn = tr.querySelector('button');
        if (btn && btn.dataset.id === id) {
            tr.setAttribute('data-estado', estado);
            const tdEstado = tr.children[4];
            tdEstado.innerHTML = `<span class="px-2.5 py-1 rounded-full text-[10px] font-black uppercase tracking-wide bg-pill-green text-sidebar border border-[#8ce383]">${estado}</span>`;
            btn.dataset.estado = estado;
            btn.querySelector('span').innerText = 'Ver / Modificar';
        }
    });

    // Actualizar la vista del Estudiante en tiempo real
    document.querySelectorAll('#tbody-solicitudes-estudiante tr').forEach(tr => {
        const tdCod = tr.children[0];
        if (tdCod && tdCod.innerText.trim() === id) {
            const tdEstado = tr.children[3];
            const tdAccion = tr.children[4];
            const tipoCert = tr.children[1].querySelector('.font-black').innerText;

            tdEstado.innerHTML = `<span class="px-2.5 py-1 rounded-full text-[10px] font-black uppercase tracking-wide bg-pill-green text-sidebar border border-[#8ce383]">${estado}</span>`;
            tdAccion.innerHTML = `
                <div class="flex justify-end gap-1.5">
                    <button type="button" 
                            data-id="${id}"
                            data-tipo="${tipoCert}"
                            data-mensaje="${mensaje}"
                            data-archivo="${nombreArchivo}"
                            onclick="openModalVerRespuesta(this)"
                            class="px-3 py-1.5 bg-[#eafbe4] text-sidebar border border-[#c4eec0] hover:bg-[#d8f5ce] rounded-lg text-[10px] font-black transition-all flex items-center gap-1.5 cursor-pointer shadow-2xs">
                        <i data-lucide="download" class="size-3.5 stroke-[2.5]"></i>
                        <span>Ver y Descargar</span>
                    </button>
                </div>
            `;
        }
    });

    closeModalResponder();
    if (window.lucide) lucide.createIcons();
    alert('¡Respuesta enviada correctamente al estudiante!');
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
