function toggleEvaluaciones(index) {
    const row = document.getElementById('eval-row-' + index);
    const icon = document.getElementById('icon-chevron-' + index);
    if (!row || !icon) return;

    if (row.classList.contains('hidden')) {
        row.classList.remove('hidden');
        icon.classList.add('rotate-90');
    } else {
        row.classList.add('hidden');
        icon.classList.remove('rotate-90');
    }
}

function switchSubPeriodo(asigIdx, periodoNum) {
    for (let p = 1; p <= 3; p++) {
        const pane = document.getElementById('pane-' + asigIdx + '-' + p);
        const tab = document.getElementById('tab-' + asigIdx + '-' + p);
        if (!pane || !tab) continue;

        if (p === periodoNum) {
            pane.classList.remove('hidden');
            tab.className = "subtab-btn px-3 py-1 rounded-md text-[11px] font-black bg-sidebar text-white shadow-2xs cursor-pointer";
        } else {
            pane.classList.add('hidden');
            tab.className = "subtab-btn px-3 py-1 rounded-md text-[11px] font-bold text-text-secondary hover:text-sidebar cursor-pointer";
        }
    }
}
