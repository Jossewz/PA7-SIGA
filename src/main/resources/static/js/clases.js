function handleRowClick(tr, event) {
    if (event.target.closest('button')) return;
    window.location.href = '/clases/gestion?codigo=' + encodeURIComponent(tr.dataset.codigo);
}

function handleHorarioBtn(btn, event) {
    if (event) event.stopPropagation();
    openHorarioModal(btn.dataset.id, btn.dataset.codigo);
}

// Modal Horario
function openHorarioModal(cursoId, codigo) {
    const titleElem = document.getElementById('horario-title');
    if (titleElem) titleElem.innerText = 'Asignación de Horario – Curso ' + (codigo || '');

    const cursoIdInput = document.getElementById('h-curso-id');
    if (cursoIdInput) cursoIdInput.value = cursoId || '';

    const modal = document.getElementById('horario-modal');
    const dialog = document.getElementById('horario-modal-dialog');
    modal.classList.remove('hidden');
    setTimeout(() => {
        dialog.classList.remove('scale-95', 'opacity-0');
        dialog.classList.add('scale-100', 'opacity-100');
    }, 10);
}

function handleGestionBtn(btn, event) {
    event.stopPropagation();
    window.location.href = '/clases/gestion?codigo=' + encodeURIComponent(btn.dataset.codigo);
}

function handleEditarCursoBtn(btn, event) {
    if (event) event.stopPropagation();
    const data = {
        id: btn.dataset.id,
        codigoCurso: btn.dataset.codigo,
        grado: btn.dataset.grado,
        jornada: btn.dataset.jornada,
        cupos: btn.dataset.cupos,
        directorId: btn.dataset.directorid
    };
    openCursoModal(data);
}

// Modal Crear / Editar Curso
function openCursoModal(data) {
    const modal = document.getElementById('curso-modal');
    const dialog = document.getElementById('curso-modal-dialog');
    const title = document.getElementById('modal-curso-title');
    const btnText = document.getElementById('modal-curso-btn-text');

    const idInput = document.getElementById('c-id');
    const gradeSelect = document.getElementById('c-grade');
    const jornadaSelect = document.getElementById('c-jornada');
    const directorSelect = document.getElementById('c-director');
    const cuposInput = document.getElementById('c-cupos');

    if (data) {
        if (title) title.innerText = 'Editar Curso ' + (data.codigoCurso || '');
        if (btnText) btnText.innerText = 'Guardar Cambios';
        if (idInput) idInput.value = data.id || '';
        if (gradeSelect) gradeSelect.value = data.grado || '11°';
        if (jornadaSelect) jornadaSelect.value = data.jornada || 'Mañana';
        if (directorSelect) directorSelect.value = data.directorId || '';
        if (cuposInput) cuposInput.value = data.cupos || 35;
    } else {
        if (title) title.innerText = 'Crear Nuevo Curso';
        if (btnText) btnText.innerText = 'Crear Curso';
        if (idInput) idInput.value = '';
        const form = document.getElementById('form-register-curso');
        if (form) form.reset();
    }

    modal.classList.remove('hidden');
    setTimeout(() => {
        dialog.classList.remove('scale-95', 'opacity-0');
        dialog.classList.add('scale-100', 'opacity-100');
    }, 10);
}

function closeCursoModal() {
    const modal = document.getElementById('curso-modal');
    const dialog = document.getElementById('curso-modal-dialog');
    dialog.classList.remove('scale-100', 'opacity-100');
    dialog.classList.add('scale-95', 'opacity-0');
    setTimeout(() => {
        modal.classList.add('hidden');
    }, 300);
}



function closeHorarioModal() {
    const modal = document.getElementById('horario-modal');
    const dialog = document.getElementById('horario-modal-dialog');
    dialog.classList.remove('scale-100', 'opacity-100');
    dialog.classList.add('scale-95', 'opacity-0');
    setTimeout(() => {
        modal.classList.add('hidden');
    }, 300);
}

function agregarFilaHorario() {
    const tbody = document.getElementById('horario-tbody');
    if (!tbody) return;

    const slotRows = tbody.querySelectorAll('tr.slot-row');
    const nextIdx = slotRows.length;
    const blockNum = nextIdx + 1;

    const tr = document.createElement('tr');
    tr.className = 'slot-row hover:bg-slate-50/50';

    const materiasOpts = (window.materiasGlobales && Array.isArray(window.materiasGlobales))
        ? window.materiasGlobales.map(m => `<option value="${m.id}">${m.nombre}</option>`).join('')
        : '';

    const docentesOpts = (window.docentesGlobales && Array.isArray(window.docentesGlobales))
        ? window.docentesGlobales.map(d => `<option value="${d.id}">${d.nombreCompleto}</option>`).join('')
        : '';

    const dias = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes'];

    let html = `
        <td class="p-3 text-left bg-[#f7fcf6]/50 border-r border-[#c4eec0]/60 font-bold">
            <div class="flex justify-between items-center mb-1">
                <span class="text-sidebar text-[11px] font-black slot-title">Bloque ${blockNum}</span>
                <button type="button" onclick="eliminarFilaHorario(this)" class="text-text-secondary hover:text-alert-red p-1 rounded cursor-pointer" title="Eliminar bloque">
                    <i data-lucide="trash-2" class="size-3.5"></i>
                </button>
            </div>
            <div class="flex items-center gap-1">
                <input type="time" name="slot_${nextIdx}_inicio" value="12:30" class="p-1 border border-[#c4eec0] rounded text-[10px] font-bold bg-white text-center w-20">
                <span class="text-text-secondary font-bold">-</span>
                <input type="time" name="slot_${nextIdx}_fin" value="13:30" class="p-1 border border-[#c4eec0] rounded text-[10px] font-bold bg-white text-center w-20">
            </div>
        </td>
    `;

    dias.forEach((dia, dIdx) => {
        const borderClass = dIdx < 4 ? 'border-r border-[#c4eec0]/30' : '';
        html += `
            <td class="p-2 ${borderClass}">
                <select name="slot_${nextIdx}_${dia}_materiaId" class="w-full p-1.5 bg-white border border-[#c4eec0] rounded-lg text-[10px] font-bold text-text-primary mb-1 focus:ring-1 focus:ring-sidebar">
                    <option value="">-- Materia --</option>
                    ${materiasOpts}
                </select>
                <select name="slot_${nextIdx}_${dia}_docenteId" class="w-full p-1.5 bg-white border border-[#c4eec0] rounded-lg text-[10px] font-semibold text-sidebar focus:ring-1 focus:ring-sidebar">
                    <option value="">-- Docente --</option>
                    ${docentesOpts}
                </select>
            </td>
        `;
    });

    tr.innerHTML = html;
    tbody.appendChild(tr);

    if (window.lucide) lucide.createIcons();
    renumerarBloquesHorario();
}

function eliminarFilaHorario(btn) {
    const tr = btn.closest('tr');
    if (tr) {
        tr.remove();
        renumerarBloquesHorario();
    }
}

function renumerarBloquesHorario() {
    const tbody = document.getElementById('horario-tbody');
    if (!tbody) return;
    const slotTitles = tbody.querySelectorAll('.slot-title');
    slotTitles.forEach((span, idx) => {
        span.innerText = `Bloque ${idx + 1}`;
    });
}
