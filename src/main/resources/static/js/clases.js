function handleRowClick(tr, event) {
    if (event.target.closest('button')) return;
    window.location.href = '/clases/gestion?codigo=' + encodeURIComponent(tr.dataset.codigo);
}

function handleHorarioBtn(btn, event) {
    event.stopPropagation();
    openHorarioModal(btn.dataset.codigo, btn.dataset.tiene === 'true');
}

function handleGestionBtn(btn, event) {
    event.stopPropagation();
    window.location.href = '/clases/gestion?codigo=' + encodeURIComponent(btn.dataset.codigo);
}

// Modal Crear Curso
function openCursoModal() {
    const modal = document.getElementById('curso-modal');
    const dialog = document.getElementById('curso-modal-dialog');
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

// Modal Horario
function openHorarioModal(codigo, tieneHorario) {
    const titleElem = document.getElementById('horario-title');
    if (titleElem) titleElem.innerText = 'Asignación de Horario – Curso ' + codigo;
    const modal = document.getElementById('horario-modal');
    const dialog = document.getElementById('horario-modal-dialog');
    modal.classList.remove('hidden');
    setTimeout(() => {
        dialog.classList.remove('scale-95', 'opacity-0');
        dialog.classList.add('scale-100', 'opacity-100');
    }, 10);
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
