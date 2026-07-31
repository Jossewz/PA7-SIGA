// Descripciones oficiales de las razones del reporte
const descripcionesRazones = {
    'Bajo rendimiento': {
        titulo: 'Bajo rendimiento académico',
        body: 'Notas muy bajas o pérdida constante de materias en el período escolar.'
    },
    'Incumplimiento': {
        titulo: 'Incumplimiento de deberes',
        body: 'No entregar tareas, talleres o proyectos asignados dentro de los plazos fijados.'
    },
    'Falta de atención': {
        titulo: 'Falta de atención e interés',
        body: 'No participar, distraerse habitualmente o ignorar las explicaciones del docente en clase.'
    },
    'Agresión': {
        titulo: 'Agresión o violencia',
        body: 'Peleas físicas, insultos, acoso o maltrato verbal a compañeros o miembros del colegio.'
    },
    'Falta de respeto': {
        titulo: 'Falta de respeto a la autoridad',
        body: 'Desobedecer conscientemente las normas del aula o responder de forma desafiante al profesor.'
    },
    'Daño a la escuela': {
        titulo: 'Daño o vandalismo a la infraestructura',
        body: 'Romper, deteriorar o sustraer bienes del salón, laboratorios o instalaciones institucionales.'
    },
    'Ausencias': {
        titulo: 'Inasistencias injustificadas',
        body: 'Faltar a la jornada escolar de forma recurrente sin justificación de los padres o acudientes.'
    },
    'Llegadas tarde': {
        titulo: 'Llegadas tarde habituales',
        body: 'Ingresar con impuntualidad sistemática a la institución o a los bloques de clase.'
    },
    'Salud o riesgo': {
        titulo: 'Situación de salud o riesgo',
        body: 'Evidenciar señales de desánimo extremo, enfermedad visible o posibles vulnerabilidades en el hogar.'
    }
};

function actualizarDescripcionRazon(razon) {
    const info = descripcionesRazones[razon];
    if (info) {
        document.getElementById('razon-title').innerText = info.titulo;
        document.getElementById('razon-body').innerText = info.body;
    }
}

// Conmutador de Roles para Pruebas (Docente, Admin, Estudiante)
function cambiarRolSimulado(rol) {
    const btnDoc = document.getElementById('btn-rol-docente');
    const btnAdmin = document.getElementById('btn-rol-admin');
    const btnEst = document.getElementById('btn-rol-estudiante');

    const vDoc = document.getElementById('vista-docente');
    const vAdmin = document.getElementById('vista-admin');
    const vEst = document.querySelectorAll('#vista-estudiante');

    const btnInactive = "px-3 py-1.5 rounded-lg text-xs font-bold text-text-secondary hover:text-sidebar hover:bg-[#f7fcf6] transition-all flex items-center gap-1.5 cursor-pointer";
    const btnActive = "px-3 py-1.5 rounded-lg text-xs font-black transition-all flex items-center gap-1.5 cursor-pointer bg-sidebar text-white shadow-2xs";

    btnDoc.className = btnInactive;
    btnAdmin.className = btnInactive;
    btnEst.className = btnInactive;

    vDoc.classList.add('hidden');
    vAdmin.classList.add('hidden');
    vEst.forEach(el => el.classList.add('hidden'));

    if (rol === 'docente') {
        btnDoc.className = btnActive;
        vDoc.classList.remove('hidden');
    } else if (rol === 'admin') {
        btnAdmin.className = btnActive;
        vAdmin.classList.remove('hidden');
    } else {
        btnEst.className = btnActive;
        vEst.forEach(el => el.classList.remove('hidden'));
    }
}

// Formulario Docente: Crear Reporte
function handleFormCrearReporte(e) {
    e.preventDefault();
    const estudiante = document.getElementById('select-estudiante-reporte').value;
    const razon = document.getElementById('select-razon-reporte').value;
    const detalles = document.getElementById('reporte-detalles-txt').value;
    const fechaHoy = new Date().toISOString().split('T')[0];
    const numCod = Math.floor(100 + Math.random() * 900);
    const codId = 'REP-2026-' + numCod;

    let categoria = 'Razones Académicas';
    if (['Agresión', 'Falta de respeto', 'Daño a la escuela'].includes(razon)) categoria = 'Convivencia y Disciplina';
    if (['Ausencias', 'Llegadas tarde', 'Salud o riesgo'].includes(razon)) categoria = 'Asistencia y Salud';

    // Insertar en la tabla del Docente
    const tbodyDoc = document.getElementById('tbody-reportes-docente');
    const trDoc = document.createElement('tr');
    trDoc.className = "hover:bg-[#fcfdfb] transition-colors";
    trDoc.innerHTML = `
        <td class="p-3.5 pl-5 font-black text-sidebar text-[11px]">${codId}</td>
        <td class="p-3.5">
            <div class="font-black text-text-primary text-[12px]">${estudiante}</div>
            <div class="text-[10px] font-semibold text-text-secondary">11° - 01</div>
        </td>
        <td class="p-3.5">
            <div class="font-bold text-sidebar text-[12px]">${razon}</div>
            <div class="text-[10px] font-semibold text-text-secondary">${categoria}</div>
        </td>
        <td class="p-3.5 text-center">
            <span class="px-2.5 py-1 rounded-full text-[10px] font-black uppercase tracking-wide bg-amber-100 text-amber-800 border border-amber-300">Pendiente</span>
        </td>
        <td class="p-3.5 pr-5 text-right font-bold text-[11px]">
            <span class="text-text-secondary italic">En evaluación...</span>
        </td>
    `;
    tbodyDoc.prepend(trDoc);

    // Insertar en la bandeja del Admin
    const tbodyAdmin = document.getElementById('tbody-admin-reportes');
    const trAdmin = document.createElement('tr');
    trAdmin.className = "hover:bg-[#fcfdfb] transition-colors row-reporte-admin";
    trAdmin.setAttribute('data-estado', 'Pendiente');
    trAdmin.innerHTML = `
        <td class="p-3.5 pl-5 font-black text-sidebar text-[11px]">${codId}</td>
        <td class="p-3.5">
            <div class="font-black text-text-primary text-[12px]">${estudiante}</div>
            <div class="text-[10px] font-semibold text-text-secondary">11° - 01</div>
        </td>
        <td class="p-3.5 font-semibold text-text-secondary">Prof. Jorge Eliécer Rojas</td>
        <td class="p-3.5">
            <div class="font-bold text-sidebar text-[12px]">${razon}</div>
            <div class="text-[10px] font-semibold text-text-secondary">${categoria}</div>
        </td>
        <td class="p-3.5 text-center">
            <span class="px-2.5 py-1 rounded-full text-[10px] font-black uppercase tracking-wide bg-amber-100 text-amber-800 border border-amber-300">Pendiente</span>
        </td>
        <td class="p-3.5 pr-5 text-right">
            <button type="button" 
                    data-id="${codId}"
                    data-estudiante="${estudiante}"
                    data-grado="11° - 01"
                    data-docente="Prof. Jorge Eliécer Rojas"
                    data-razon="${razon}"
                    data-categoria="${categoria}"
                    data-detalles="${detalles}"
                    data-estado="Pendiente"
                    onclick="openModalAtenderReporte(this)"
                    class="px-3.5 py-1.5 text-[11px] font-black text-white bg-sidebar hover:brightness-97 rounded-lg transition-all flex items-center gap-1.5 ml-auto cursor-pointer shadow-2xs">
                <i data-lucide="user-check" class="size-3.5 stroke-[2.5]"></i>
                <span>Revisar Reporte</span>
            </button>
        </td>
    `;
    tbodyAdmin.prepend(trAdmin);

    document.getElementById('form-crear-reporte').reset();
    if (window.lucide) lucide.createIcons();
    alert('¡Reporte enviado exitosamente a la Dirección / Coordinación!');
}

// Modal Admin: Atender Reporte
function openModalAtenderReporte(btn) {
    document.getElementById('ar-reporte-id').value = btn.dataset.id;
    document.getElementById('ar-codigo').innerText = btn.dataset.id;
    document.getElementById('ar-estudiante-nombre').innerText = btn.dataset.estudiante || 'Estudiante';
    document.getElementById('ar-grado-txt').innerText = btn.dataset.grado || '11° - 01';
    document.getElementById('ar-razon-txt').innerText = btn.dataset.razon || 'Bajo rendimiento';
    document.getElementById('ar-detalles-txt').innerText = `"${btn.dataset.detalles || 'Sin detalles'}"`;

    const modal = document.getElementById('modal-atender-reporte');
    const dialog = document.getElementById('dialog-atender-reporte');
    modal.classList.remove('hidden');
    setTimeout(() => {
        dialog.classList.remove('scale-95', 'opacity-0');
        dialog.classList.add('scale-100', 'opacity-100');
    }, 10);
}

function closeModalAtenderReporte() {
    const modal = document.getElementById('modal-atender-reporte');
    const dialog = document.getElementById('dialog-atender-reporte');
    dialog.classList.remove('scale-100', 'opacity-100');
    dialog.classList.add('scale-95', 'opacity-0');
    setTimeout(() => {
        modal.classList.add('hidden');
    }, 300);
}

function toggleCamposCitacion(decision) {
    const sec = document.getElementById('seccion-citacion');
    if (decision === 'Aceptado') {
        sec.classList.remove('hidden');
    } else {
        sec.classList.add('hidden');
    }
}

// Admin: Guardar Decisión y Notificar
function handleFormAtender(e) {
    e.preventDefault();
    const id = document.getElementById('ar-reporte-id').value;
    const decision = document.getElementById('ar-decision-select').value;
    const fechaCit = document.getElementById('ar-fecha-citacion').value;
    const requiereAcudiente = document.getElementById('ar-requiere-acudiente').checked;
    const obs = document.getElementById('ar-obs-txt').value;

    // Actualizar tabla del Admin
    document.querySelectorAll('.row-reporte-admin').forEach(tr => {
        const btn = tr.querySelector('button');
        if (btn && btn.dataset.id === id) {
            tr.setAttribute('data-estado', decision);
            const tdEst = tr.children[4];
            tdEst.innerHTML = `<span class="px-2.5 py-1 rounded-full text-[10px] font-black uppercase tracking-wide ${decision === 'Aceptado' ? 'bg-pill-green text-sidebar border border-[#8ce383]' : 'bg-pill-red text-alert-red border border-red-300'}">${decision}</span>`;
            btn.querySelector('span').innerText = 'Ver Citación';
        }
    });

    // Actualizar tabla del Docente
    document.querySelectorAll('#tbody-reportes-docente tr').forEach(tr => {
        const tdCod = tr.children[0];
        if (tdCod && tdCod.innerText.trim() === id) {
            const tdEstado = tr.children[3];
            const tdCit = tr.children[4];
            tdEstado.innerHTML = `<span class="px-2.5 py-1 rounded-full text-[10px] font-black uppercase tracking-wide ${decision === 'Aceptado' ? 'bg-pill-green text-sidebar border border-[#8ce383]' : 'bg-pill-red text-alert-red border border-red-300'}">${decision}</span>`;
            tdCit.innerHTML = decision === 'Aceptado' ? `<span class="text-sidebar font-bold">${fechaCit}</span>` : `<span class="text-alert-red font-bold">Archivado</span>`;
        }
    });

    // Actualizar la Vista del Estudiante
    if (decision === 'Aceptado') {
        document.getElementById('est-fecha-citacion').innerText = fechaCit;
        document.getElementById('est-obs-admin').innerText = obs;
        const badgeAcudiente = document.getElementById('badge-acudiente-req');

        if (requiereAcudiente) {
            badgeAcudiente.className = "px-4 py-2 bg-pill-red text-alert-red border border-red-300 rounded-xl text-xs font-black flex items-center gap-2 shadow-2xs shrink-0";
            badgeAcudiente.innerHTML = `<i data-lucide="users" class="size-4 stroke-[2.5]"></i><span>CITACIÓN CON ACUDIENTE / PADRE DE FAMILIA</span>`;
        } else {
            badgeAcudiente.className = "px-4 py-2 bg-pill-green text-sidebar border border-[#8ce383] rounded-xl text-xs font-black flex items-center gap-2 shadow-2xs shrink-0";
            badgeAcudiente.innerHTML = `<i data-lucide="user" class="size-4 stroke-[2.5]"></i><span>CITACIÓN INDIVIDUAL AL ESTUDIANTE</span>`;
        }
    }

    closeModalAtenderReporte();
    if (window.lucide) lucide.createIcons();
    alert('¡El reporte ha sido procesado y el estudiante ha sido notificado!');
}

// Filtrar Reportes Admin
function filtrarReportesAdmin(filtro) {
    document.querySelectorAll('.row-reporte-admin').forEach(tr => {
        if (filtro === 'todos' || tr.dataset.estado === filtro) {
            tr.classList.remove('hidden');
        } else {
            tr.classList.add('hidden');
        }
    });
}
