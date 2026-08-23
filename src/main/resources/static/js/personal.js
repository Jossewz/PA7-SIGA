function openModalPersonal() {
    const modal = document.getElementById('modal-personal');
    const dialog = document.getElementById('modal-personal-dialog');
    modal.classList.remove('hidden');
    setTimeout(() => {
        dialog.classList.remove('scale-95', 'opacity-0');
        dialog.classList.add('scale-100', 'opacity-100');
    }, 10);
}

function closeModalPersonal() {
    const modal = document.getElementById('modal-personal');
    const dialog = document.getElementById('modal-personal-dialog');
    dialog.classList.remove('scale-100', 'opacity-100');
    dialog.classList.add('scale-95', 'opacity-0');
    setTimeout(() => {
        modal.classList.add('hidden');
    }, 300);
}

function filtrarPersonal(tipo) {
    const rows = document.querySelectorAll('.personal-row');
    rows.forEach(r => {
        if (tipo === 'todos' || r.dataset.tipo === tipo) {
            r.classList.remove('hidden');
        } else {
            r.classList.add('hidden');
        }
    });
}
