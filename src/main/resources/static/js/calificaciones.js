document.addEventListener('DOMContentLoaded', () => {
    cargarBoletinEstudiante();
});

function cargarBoletinEstudiante() {
    const tbody = document.getElementById('tbody-boletin');
    if (!tbody) return;

    const estId = window.estudianteIdActivo || '';
    const estDoc = window.estudianteDocActivo || '';

    if (!estId && !estDoc) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="p-8 text-center text-text-muted italic">
                    No hay un estudiante seleccionado para visualizar su boletín.
                </td>
            </tr>
        `;
        return;
    }

    // Buscar en LocalStorage todas las planillas registradas por los docentes
    const todasMateriasData = {};

    for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (key && key.startsWith('siga_notas_')) {
            try {
                const store = JSON.parse(localStorage.getItem(key));
                // store[materia][periodo] = [ { nombre, fechaDisplay, peso, estudiantesNotas } ]
                Object.keys(store).forEach(materiaName => {
                    if (!todasMateriasData[materiaName]) {
                        todasMateriasData[materiaName] = { '1': [], '2': [], '3': [] };
                    }
                    Object.keys(store[materiaName]).forEach(periodoNum => {
                        if (Array.isArray(store[materiaName][periodoNum])) {
                            todasMateriasData[materiaName][periodoNum] = store[materiaName][periodoNum];
                        }
                    });
                });
            } catch (e) {
                console.warn("Error leyendo calificaciones de storage:", e);
            }
        }
    }

    const materiasNombres = Object.keys(todasMateriasData);

    if (materiasNombres.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="p-8 text-center text-text-muted italic">
                    El estudiante no posee calificaciones registradas en el período activo.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = '';

    materiasNombres.forEach((materiaName, idx) => {
        const periodosObj = todasMateriasData[materiaName];

        // Calcular promedio ponderado por periodo para este estudiante
        const promediosPeriodos = {};
        let sumaDefinitiva = 0;
        let countDefinitiva = 0;

        ['1', '2', '3'].forEach(pNum => {
            const notasList = periodosObj[pNum] || [];
            let sumaPesos = 0;
            let sumaPonderada = 0;

            notasList.forEach(n => {
                let val = null;
                if (n.estudiantesNotas) {
                    if (n.estudiantesNotas[estId] !== undefined) {
                        val = parseFloat(n.estudiantesNotas[estId]);
                    } else if (n.estudiantesNotas[estDoc] !== undefined) {
                        val = parseFloat(n.estudiantesNotas[estDoc]);
                    } else {
                        // Buscar por coincidencia flexible en claves
                        Object.keys(n.estudiantesNotas).forEach(k => {
                            if (k === estId || k === estDoc) {
                                val = parseFloat(n.estudiantesNotas[k]);
                            }
                        });
                    }
                }
                const finalVal = (val !== null && !isNaN(val)) ? val : 0.0;
                const peso = parseFloat(n.peso) || 0;
                sumaPonderada += (finalVal * peso);
                sumaPesos += peso;
            });

            if (notasList.length > 0 && sumaPesos > 0) {
                const prom = sumaPonderada / sumaPesos;
                promediosPeriodos[pNum] = prom;
                sumaDefinitiva += prom;
                countDefinitiva++;
            } else {
                promediosPeriodos[pNum] = null;
            }
        });

        const definitiva = countDefinitiva > 0 ? (sumaDefinitiva / countDefinitiva) : 0.0;

        // Fila Principal de la Asignatura
        const trMain = document.createElement('tr');
        trMain.className = "hover:bg-[#fcfdfb] transition-colors cursor-pointer";
        trMain.setAttribute('data-index', idx);
        trMain.onclick = () => toggleEvaluaciones(idx);

        const p1Str = promediosPeriodos['1'] !== null ? promediosPeriodos['1'].toFixed(2) : '-';
        const p2Str = promediosPeriodos['2'] !== null ? promediosPeriodos['2'].toFixed(2) : '-';
        const p3Str = promediosPeriodos['3'] !== null ? promediosPeriodos['3'].toFixed(2) : '-';
        const defStr = definitiva.toFixed(2);

        let badgeDefClass = "bg-alert-red";
        if (definitiva >= 3.5) badgeDefClass = "bg-sidebar";
        else if (definitiva >= 3.0) badgeDefClass = "bg-amber-500";

        trMain.innerHTML = `
            <td class="p-4 pl-6 font-black text-sidebar text-[13px] flex items-center gap-2.5">
                <i data-lucide="chevron-right" id="icon-chevron-${idx}" class="size-4 stroke-[2.5] text-text-secondary transition-transform duration-200"></i>
                <span>${materiaName}</span>
            </td>
            <td class="p-4 text-center font-bold text-sidebar">${p1Str}</td>
            <td class="p-4 text-center font-bold text-sidebar">${p2Str}</td>
            <td class="p-4 text-center font-bold text-sidebar">${p3Str}</td>
            <td class="p-4 text-center">
                <span class="px-2.5 py-1 rounded-full text-[11px] font-black text-white ${badgeDefClass}">${defStr}</span>
            </td>
            <td class="p-4 pr-6 text-right">
                <span class="text-[11px] font-bold text-sidebar underline underline-offset-2">Ver Detalle</span>
            </td>
        `;
        tbody.appendChild(trMain);

        // Fila Desplegable de Notas por Período
        const trDetail = document.createElement('tr');
        trDetail.id = `eval-row-${idx}`;
        trDetail.className = "hidden bg-[#f7fcf6]/60 border-b border-[#c4eec0]/40";

        let htmlP1 = renderNotasPeriodoHTML(periodosObj['1'], estId, estDoc);
        let htmlP2 = renderNotasPeriodoHTML(periodosObj['2'], estId, estDoc);
        let htmlP3 = renderNotasPeriodoHTML(periodosObj['3'], estId, estDoc);

        trDetail.innerHTML = `
            <td colspan="6" class="p-5 pl-10 pr-8">
                <div class="bg-white rounded-xl p-4 border border-[#c4eec0]/40 shadow-sm space-y-4">
                    <div class="flex items-center justify-between border-b border-[#c4eec0]/30 pb-3">
                        <h5 class="text-[11px] font-black uppercase text-sidebar tracking-wider flex items-center gap-2">
                            <i data-lucide="layers" class="size-4 stroke-2"></i>
                            <span>Evaluaciones Detalladas – ${materiaName}</span>
                        </h5>
                        <div class="flex items-center gap-1 bg-[#f7fcf6] p-1 rounded-lg border border-[#c4eec0]/50">
                            <button type="button" onclick="switchSubPeriodo(${idx}, 1)" id="tab-${idx}-1" class="subtab-btn px-3 py-1 rounded-md text-[11px] font-black bg-sidebar text-white shadow-2xs cursor-pointer">Periodo 1</button>
                            <button type="button" onclick="switchSubPeriodo(${idx}, 2)" id="tab-${idx}-2" class="subtab-btn px-3 py-1 rounded-md text-[11px] font-bold text-text-secondary hover:text-sidebar cursor-pointer">Periodo 2</button>
                            <button type="button" onclick="switchSubPeriodo(${idx}, 3)" id="tab-${idx}-3" class="subtab-btn px-3 py-1 rounded-md text-[11px] font-bold text-text-secondary hover:text-sidebar cursor-pointer">Periodo 3</button>
                        </div>
                    </div>

                    <div id="pane-${idx}-1" class="subpane-window">${htmlP1}</div>
                    <div id="pane-${idx}-2" class="subpane-window hidden">${htmlP2}</div>
                    <div id="pane-${idx}-3" class="subpane-window hidden">${htmlP3}</div>
                </div>
            </td>
        `;
        tbody.appendChild(trDetail);
    });

    if (window.lucide) lucide.createIcons();
}

function renderNotasPeriodoHTML(notasList, estId, estDoc) {
    if (!notasList || notasList.length === 0) {
        return `<p class="text-[11px] text-text-muted italic py-2">Sin evaluaciones registradas en este período.</p>`;
    }

    let itemsHTML = '';
    notasList.forEach(n => {
        let val = null;
        if (n.estudiantesNotas) {
            if (n.estudiantesNotas[estId] !== undefined) {
                val = parseFloat(n.estudiantesNotas[estId]);
            } else if (n.estudiantesNotas[estDoc] !== undefined) {
                val = parseFloat(n.estudiantesNotas[estDoc]);
            } else {
                Object.keys(n.estudiantesNotas).forEach(k => {
                    if (k === estId || k === estDoc) {
                        val = parseFloat(n.estudiantesNotas[k]);
                    }
                });
            }
        }
        const notaStr = (val !== null && !isNaN(val)) ? val.toFixed(2) : '0.00';

        itemsHTML += `
            <div class="p-3 rounded-lg border border-[#c4eec0]/40 bg-[#fbfdfa] flex justify-between items-center">
                <div>
                    <p class="text-[11px] font-bold text-text-primary">${n.nombre} <span class="text-[9px] text-[#3e6837]">(${n.fechaDisplay || ''})</span></p>
                    <span class="text-[10px] font-semibold text-text-secondary">Peso: ${n.peso}%</span>
                </div>
                <span class="px-2 py-0.5 rounded text-[11px] font-black bg-[#eafbe4] text-sidebar border border-[#c4eec0]">${notaStr}</span>
            </div>
        `;
    });

    return `<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">${itemsHTML}</div>`;
}

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
