/* Data mock de estudiantes del servidor */
let estudiantesMockData = [
    { id: 1, nombre: 'Álvarez Restrepo, Mateo', documento: '1098432101', asistencia: 'Presente' },
    { id: 2, nombre: 'Bermúdez Castro, Sofia', documento: '1098432102', asistencia: 'Presente' },
    { id: 3, nombre: 'Cárdenas Morales, Juan Diego', documento: '1098432103', asistencia: 'No presente' },
    { id: 4, nombre: 'Díaz Gómez, Valentina', documento: '1098432104', asistencia: 'Presente' },
    { id: 5, nombre: 'Espinosa Vargas, Andrés Felipe', documento: '1098432105', asistencia: 'Excusado' },
    { id: 6, nombre: 'Franco Gutiérrez, Isabella', documento: '1098432106', asistencia: 'Presente' },
    { id: 7, nombre: 'Gómez Hernández, Santiago', documento: '1098432107', asistencia: 'No presente' }
];

/* Estado por Períodos (Periodo 1, Periodo 2, Periodo 3) */
let periodosConfig = {
    '1': {
        label: 'Periodo 1',
        notas: [
            { nombre: 'Nota 1', peso: 25 },
            { nombre: 'Nota 2', peso: 25 },
            { nombre: 'Nota 3', peso: 25 },
            { nombre: 'Nota 4', peso: 25 }
        ],
        estudiantesNotas: {
            1: [4.5, 4.0, 4.8, 4.2],
            2: [3.8, 4.2, 3.5, 4.0],
            3: [2.5, 3.0, 2.8, 3.2],
            4: [4.8, 5.0, 4.7, 4.9],
            5: [3.2, 3.5, 4.0, 3.0],
            6: [4.0, 4.2, 4.5, 4.3],
            7: [1.8, 2.5, 2.0, 2.2]
        }
    },
    '2': {
        label: 'Periodo 2',
        notas: [
            { nombre: 'Nota 1', peso: 35 },
            { nombre: 'Nota 2', peso: 35 },
            { nombre: 'Nota 3', peso: 30 }
        ],
        estudiantesNotas: {
            1: [4.2, 4.5, 4.0],
            2: [4.0, 3.8, 4.2],
            3: [3.0, 2.9, 3.1],
            4: [5.0, 4.8, 4.9],
            5: [3.5, 3.8, 4.0],
            6: [4.2, 4.4, 4.3],
            7: [2.0, 2.2, 2.4]
        }
    },
    '3': {
        label: 'Periodo 3',
        notas: [
            { nombre: 'Nota 1', peso: 50 },
            { nombre: 'Nota 2', peso: 50 }
        ],
        estudiantesNotas: {
            1: [4.6, 4.4],
            2: [4.1, 4.3],
            3: [3.2, 3.0],
            4: [4.9, 5.0],
            5: [3.6, 3.9],
            6: [4.5, 4.2],
            7: [2.5, 2.6]
        }
    }
};

let periodoActivo = localStorage.getItem('siga_periodo_activo') || '1';

document.addEventListener('DOMContentLoaded', () => {
    seleccionarPeriodo(periodoActivo);
});

// Cambiar y Guardar Período Activo
function seleccionarPeriodo(num) {
    periodoActivo = num;
    localStorage.setItem('siga_periodo_activo', num);

    ['1', '2', '3'].forEach(p => {
        const btn = document.getElementById('btn-p' + p);
        if (btn) {
            if (p === num) {
                btn.className = "px-4 py-2 rounded-md text-[12px] font-black bg-sidebar text-white shadow-2xs cursor-pointer";
            } else {
                btn.className = "px-4 py-2 rounded-md text-[12px] font-bold text-text-secondary hover:text-sidebar hover:bg-[#f7fcf6] cursor-pointer";
            }
        }
    });

    const lbl = document.getElementById('periodo-activo-label');
    if (lbl) lbl.innerText = periodosConfig[num].label;
    renderizarTablaNotas();
}

// Renderizar la tabla de notas ajustada sin forzar scroll
function renderizarTablaNotas() {
    const config = periodosConfig[periodoActivo];
    const theadRow = document.getElementById('thead-row');
    const tbody = document.getElementById('tbody-estudiantes');
    if (!theadRow || !tbody) return;

    theadRow.innerHTML = `
        <th class="p-3 pl-5 w-8 text-center">#</th>
        <th class="p-3 min-w-[160px]">Estudiante</th>
    `;

    config.notas.forEach((nota, idx) => {
        const th = document.createElement('th');
        th.className = "p-2.5 text-center border-x border-[#c4eec0]/30 bg-[#f4fbf2] px-1";
        th.innerHTML = `
            <div class="flex flex-col items-center gap-1">
                <div class="flex items-center justify-between w-full text-[10px] font-black uppercase text-sidebar px-0.5">
                    <span>${nota.nombre}</span>
                    ${config.notas.length > 1 ? `<button type="button" onclick="eliminarNota(${idx})" class="text-alert-red hover:bg-red-50 p-0.5 rounded cursor-pointer" title="Eliminar Nota"><i data-lucide="trash-2" class="size-3"></i></button>` : ''}
                </div>
                <div class="flex items-center gap-0.5 bg-white px-1.5 py-0.5 rounded border border-[#c4eec0] shadow-2xs">
                    <span class="text-[9px] font-bold text-text-secondary">%</span>
                    <input type="number" value="${nota.peso}" min="0" max="100" oninput="actualizarPesoNota(${idx}, this.value)" class="w-8 text-center font-bold text-sidebar text-[11px] focus:outline-none focus:ring-1 focus:ring-sidebar rounded p-0">
                </div>
            </div>
        `;
        theadRow.appendChild(th);
    });

    theadRow.innerHTML += `
        <th class="p-3 text-center w-24">Nota Final</th>
        <th class="p-3 pr-5 text-center w-32">Asistencia</th>
    `;

    tbody.innerHTML = '';
    estudiantesMockData.forEach(est => {
        const tr = document.createElement('tr');
        tr.className = "hover:bg-[#fcfdfb] transition-colors est-row";
        tr.setAttribute('data-id', est.id);

        let notasEst = config.estudiantesNotas[est.id] || [];
        while (notasEst.length < config.notas.length) notasEst.push(4.0);

        let tdsNotasHTML = '';
        config.notas.forEach((_, idx) => {
            const val = notasEst[idx] !== undefined ? notasEst[idx] : 4.0;
            tdsNotasHTML += `
                <td class="p-2.5 text-center border-x border-[#c4eec0]/20 px-1">
                    <input type="number" step="0.1" min="0" max="5" value="${val}" oninput="recalcularFilaEstudiante(this, ${est.id}, ${idx})" class="w-12 p-1 text-center font-bold border border-[#c4eec0] rounded bg-white text-sidebar focus:ring-1 focus:ring-sidebar n-input text-[12px]">
                </td>
            `;
        });

        tr.innerHTML = `
            <td class="p-3 pl-5 font-bold text-text-secondary text-center">${est.id}</td>
            <td class="p-3">
                <div class="font-black text-text-primary text-[12px]">${est.nombre}</div>
                <div class="text-[10px] font-semibold text-text-secondary">Doc: ${est.documento}</div>
            </td>
            ${tdsNotasHTML}
            <td class="p-3 text-center font-black">
                <span class="px-2.5 py-1 rounded-full text-[11px] font-black text-white nota-final-badge bg-sidebar">0.00</span>
            </td>
            <td class="p-3 pr-5 text-center">
                <button type="button" 
                        onclick="toggleAsistencia(this)" 
                        data-estado="${est.asistencia}"
                        class="px-2.5 py-1 rounded-full text-[10px] font-black uppercase tracking-wide transition-all cursor-pointer flex items-center justify-center gap-1 mx-auto badge-asistencia ${getClassesAsistencia(est.asistencia)}">
                    ${getIconAsistencia(est.asistencia)}
                    <span class="ast-text">${est.asistencia}</span>
                </button>
            </td>
        `;

        tbody.appendChild(tr);
    });

    if (window.lucide) {
        lucide.createIcons();
    }

    recalcularPonderacionesYNotas();
}

function agregarNota() {
    const config = periodosConfig[periodoActivo];
    const numNueva = config.notas.length + 1;
    config.notas.push({ nombre: 'Nota ' + numNueva, peso: 10 });

    estudiantesMockData.forEach(est => {
        if (!config.estudiantesNotas[est.id]) config.estudiantesNotas[est.id] = [];
        config.estudiantesNotas[est.id].push(4.0);
    });

    renderizarTablaNotas();
}

function eliminarNota(idx) {
    const config = periodosConfig[periodoActivo];
    if (config.notas.length <= 1) return;
    config.notas.splice(idx, 1);

    estudiantesMockData.forEach(est => {
        if (config.estudiantesNotas[est.id]) {
            config.estudiantesNotas[est.id].splice(idx, 1);
        }
    });

    renderizarTablaNotas();
}

function actualizarPesoNota(idx, valor) {
    const valNum = parseFloat(valor) || 0;
    periodosConfig[periodoActivo].notas[idx].peso = valNum;
    recalcularPonderacionesYNotas();
}

function recalcularFilaEstudiante(inputElem, estId, notaIdx) {
    const valNum = parseFloat(inputElem.value) || 0;
    periodosConfig[periodoActivo].estudiantesNotas[estId][notaIdx] = valNum;
    recalcularPonderacionesYNotas();
}

function recalcularPonderacionesYNotas() {
    const config = periodosConfig[periodoActivo];
    let sumaPesos = 0;
    config.notas.forEach(n => sumaPesos += (parseFloat(n.peso) || 0));

    const badge = document.getElementById('peso-val-badge');
    const totalVal = document.getElementById('peso-total-val');
    if (totalVal) totalVal.innerText = sumaPesos + '%';

    if (badge) {
        if (sumaPesos === 100) {
            badge.className = "px-4 py-1.5 bg-pill-green text-sidebar border border-[#8ce383] rounded-full text-[11px] font-black flex items-center gap-1.5 shadow-2xs shrink-0";
        } else {
            badge.className = "px-4 py-1.5 bg-pill-red text-alert-red border border-red-300 rounded-full text-[11px] font-black flex items-center gap-1.5 shadow-2xs shrink-0";
        }
    }

    document.querySelectorAll('#tbody-estudiantes tr').forEach(tr => {
        const estId = tr.getAttribute('data-id');
        const notas = config.estudiantesNotas[estId] || [];
        let notaFinal = 0;

        config.notas.forEach((n, idx) => {
            const notaVal = notas[idx] !== undefined ? notas[idx] : 0;
            const pesoFrac = (n.peso || 0) / 100;
            notaFinal += (notaVal * pesoFrac);
        });

        const badgeNF = tr.querySelector('.nota-final-badge');
        if (badgeNF) {
            badgeNF.innerText = notaFinal.toFixed(2);
            if (notaFinal >= 3.5) {
                badgeNF.className = "px-2.5 py-1 rounded-full text-[11px] font-black text-white nota-final-badge bg-sidebar";
            } else if (notaFinal >= 3.0) {
                badgeNF.className = "px-2.5 py-1 rounded-full text-[11px] font-black text-white nota-final-badge bg-amber-500";
            } else {
                badgeNF.className = "px-2.5 py-1 rounded-full text-[11px] font-black text-white nota-final-badge bg-alert-red";
            }
        }
    });
}

function guardarCambiosDetalle() {
    localStorage.setItem('siga_periodo_activo', periodoActivo);
    alert('¡Cambios guardados correctamente para el ' + periodosConfig[periodoActivo].label + '!');
}

function getClassesAsistencia(estado) {
    if (estado === 'Presente') return 'bg-pill-green text-sidebar border border-[#8ce383]';
    if (estado === 'No presente') return 'bg-pill-red text-alert-red border border-red-300';
    return 'bg-amber-100 text-amber-800 border border-amber-300';
}

function getIconAsistencia(estado) {
    if (estado === 'Presente') return '<i data-lucide="check" class="size-3 stroke-[2.5]"></i>';
    if (estado === 'No presente') return '<i data-lucide="x" class="size-3 stroke-[2.5]"></i>';
    return '<i data-lucide="file-text" class="size-3 stroke-[2.5]"></i>';
}

function toggleAsistencia(btn) {
    const actual = btn.getAttribute('data-estado');
    let nuevo = 'Presente';

    if (actual === 'Presente') nuevo = 'No presente';
    else if (actual === 'No presente') nuevo = 'Excusado';

    btn.setAttribute('data-estado', nuevo);
    btn.className = `px-2.5 py-1 rounded-full text-[10px] font-black uppercase tracking-wide transition-all cursor-pointer flex items-center justify-center gap-1 mx-auto badge-asistencia ${getClassesAsistencia(nuevo)}`;
    btn.innerHTML = `${getIconAsistencia(nuevo)}<span class="ast-text">${nuevo}</span>`;

    if (window.lucide) lucide.createIcons();
}
