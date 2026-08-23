/* Data de estudiantes del servidor (BD real) */
let estudiantesDataList = (window.estudiantesBDData && Array.isArray(window.estudiantesBDData) && window.estudiantesBDData.length > 0) 
    ? window.estudiantesBDData 
    : [];

/* Horarios del servidor */
let horariosList = (window.horariosBDData && Array.isArray(window.horariosBDData)) 
    ? window.horariosBDData 
    : [];

/* 
  Almacenamiento dinámico por Materia, Fecha y Período
  1. asistenciasStore[materiaKey][fechaKey][estId] = 'Presente' | 'No presente' | 'Excusado'
  2. notasStore[materiaKey][periodoKey] = [ { nombre: 'Nota 1', fecha: '2026-08-03', fechaDisplay: '03/08', peso: 100, estudiantesNotas: { estId: 4.5 } } ]
*/
let asistenciasStore = {};
let notasStore = {};

let periodoActivo = localStorage.getItem('siga_periodo_activo') || '1';

/* Clave de almacenamiento persistente basada en el código de curso actual */
function getCursoCodigo() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('codigo') || 'GENERAL';
}

function guardarEnLocalStorage() {
    try {
        const codigo = getCursoCodigo();
        localStorage.setItem(`siga_asistencias_${codigo}`, JSON.stringify(asistenciasStore));
        localStorage.setItem(`siga_notas_${codigo}`, JSON.stringify(notasStore));
    } catch (e) {
        console.warn("Error guardando en LocalStorage:", e);
    }
}

function cargarDeLocalStorage() {
    try {
        const codigo = getCursoCodigo();
        const asistSaved = localStorage.getItem(`siga_asistencias_${codigo}`);
        const notasSaved = localStorage.getItem(`siga_notas_${codigo}`);

        if (asistSaved) {
            asistenciasStore = JSON.parse(asistSaved);
        }
        if (notasSaved) {
            notasStore = JSON.parse(notasSaved);
        }
    } catch (e) {
        console.warn("Error cargando de LocalStorage:", e);
    }
}

/* Función para obtener la fecha local de hoy YYYY-MM-DD en la zona horaria del usuario */
function getHoyFechaLocalStr() {
    const d = new Date();
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

document.addEventListener('DOMContentLoaded', () => {
    if (window.estudiantesBDData && Array.isArray(window.estudiantesBDData) && window.estudiantesBDData.length > 0) {
        estudiantesDataList = window.estudiantesBDData;
    }
    if (window.horariosBDData && Array.isArray(window.horariosBDData)) {
        horariosList = window.horariosBDData;
    }

    cargarDeLocalStorage();

    // Inicializar Fecha con la fecha local de hoy y restringir fecha máxima a hoy
    const fechaInput = document.getElementById('fecha-evaluacion');
    if (fechaInput) {
        const todayStr = getHoyFechaLocalStr();
        fechaInput.value = todayStr;
        fechaInput.setAttribute('max', todayStr);
        actualizarMateriaPorFecha();
    }

    seleccionarPeriodo(periodoActivo);
});

window.addEventListener('beforeunload', () => {
    guardarEnLocalStorage();
});

// Obtener materia actualmente seleccionada en el dropdown
function getMateriaActiva() {
    const selectMateria = document.getElementById('select-materia');
    return (selectMateria && selectMateria.value) ? selectMateria.value : 'General';
}

// Obtener fecha actualmente seleccionada en el datepicker
function getFechaActiva() {
    const fechaInput = document.getElementById('fecha-evaluacion');
    return (fechaInput && fechaInput.value) ? fechaInput.value : getHoyFechaLocalStr();
}

// Formatear fecha YYYY-MM-DD a DD/MM
function formatearFechaCorta(fechaStr) {
    if (!fechaStr) return '';
    const partes = fechaStr.split('-');
    if (partes.length === 3) {
        return `${partes[2]}/${partes[1]}`;
    }
    return fechaStr;
}

// Mapear fecha a día de la semana y actualizar materias del horario
function actualizarMateriaPorFecha() {
    const fechaInput = document.getElementById('fecha-evaluacion');
    if (!fechaInput || !fechaInput.value) return;

    const todayStr = getHoyFechaLocalStr();
    if (fechaInput.value > todayStr) {
        alert('No es posible gestionar clases en fechas futuras. Se reajustará al día de hoy.');
        fechaInput.value = todayStr;
    }

    const [year, month, day] = fechaInput.value.split('-').map(Number);
    const dateObj = new Date(year, month - 1, day);
    const diasSemana = ['Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'];
    const diaNombre = diasSemana[dateObj.getDay()];

    const diaLabel = document.getElementById('dia-nombre');
    if (diaLabel) diaLabel.innerText = diaNombre;

    // Filtrar materias programadas para ese día en el horario del curso
    const materiasDelDia = horariosList.filter(h => h.dia && h.dia.toLowerCase() === diaNombre.toLowerCase());
    const selectMateria = document.getElementById('select-materia');
    const bannerInfo = document.getElementById('horario-info-text');

    if (!selectMateria) return;
    selectMateria.innerHTML = '';

    if (materiasDelDia.length > 0) {
        materiasDelDia.forEach((m, idx) => {
            const opt = document.createElement('option');
            opt.value = m.materia;
            opt.text = `${m.materia} (${m.hora})${m.docente ? ' – Docente: ' + m.docente : ''}`;
            selectMateria.appendChild(opt);
        });

        selectMateria.disabled = false;
        selectMateria.selectedIndex = 0;
        if (bannerInfo) {
            bannerInfo.innerText = `Horario: ${materiasDelDia[0].hora}${materiasDelDia[0].docente ? ' • Docente: ' + materiasDelDia[0].docente : ''}`;
        }
    } else {
        const opt = document.createElement('option');
        opt.value = "";
        opt.text = "Sin materias programadas en este día";
        selectMateria.appendChild(opt);
        selectMateria.disabled = true;
        if (bannerInfo) bannerInfo.innerText = "No hay clases en el horario para este día.";
    }

    if (window.lucide) lucide.createIcons();
    cargarDatosClaseActual();
}

function cambiarMateriaSeleccionada(materiaNombre) {
    const diaLabel = document.getElementById('dia-nombre');
    const diaNombre = diaLabel ? diaLabel.innerText : 'Lunes';
    const materiaObj = horariosList.find(h => h.dia && h.dia.toLowerCase() === diaNombre.toLowerCase() && h.materia === materiaNombre);

    const bannerInfo = document.getElementById('horario-info-text');
    if (bannerInfo && materiaObj) {
        bannerInfo.innerText = `Horario: ${materiaObj.hora}${materiaObj.docente ? ' • Docente: ' + materiaObj.docente : ''}`;
    }

    cargarDatosClaseActual();
}

// Cargar y renderizar las notas y asistencias según la materia, período y fecha activos
function cargarDatosClaseActual() {
    renderizarTablaNotas();
}

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
    if (lbl) lbl.innerText = `Periodo ${num}`;
    renderizarTablaNotas();
}

// Obtener o inicializar las notas de una materia y período
function getNotasListMateria(materia, periodo) {
    if (!notasStore[materia]) notasStore[materia] = {};
    if (!notasStore[materia][periodo]) notasStore[materia][periodo] = [];
    return notasStore[materia][periodo];
}

// Obtener o inicializar las asistencias de una materia y fecha
function getAsistenciasMateriaFecha(materia, fecha) {
    if (!asistenciasStore[materia]) asistenciasStore[materia] = {};
    if (!asistenciasStore[materia][fecha]) asistenciasStore[materia][fecha] = {};
    return asistenciasStore[materia][fecha];
}

// Renderizar la tabla de notas y asistencias del curso
function renderizarTablaNotas() {
    const materia = getMateriaActiva();
    const fecha = getFechaActiva();
    const notasList = getNotasListMateria(materia, periodoActivo);
    const asistenciasMap = getAsistenciasMateriaFecha(materia, fecha);

    const theadRow = document.getElementById('thead-row');
    const tbody = document.getElementById('tbody-estudiantes');
    if (!theadRow || !tbody) return;

    theadRow.innerHTML = `
        <th class="p-3 pl-5 w-8 text-center">#</th>
        <th class="p-3 min-w-[160px]">Estudiante</th>
    `;

    notasList.forEach((nota, idx) => {
        const th = document.createElement('th');
        th.className = "p-2.5 text-center border-x border-[#c4eec0]/30 bg-[#f4fbf2] px-1";
        th.innerHTML = `
            <div class="flex flex-col items-center gap-1">
                <div class="flex items-center justify-between w-full text-[10px] font-black uppercase text-sidebar px-0.5">
                    <span>${nota.nombre} <span class="text-[9px] text-[#3e6837] font-semibold">(${nota.fechaDisplay || ''})</span></span>
                    <button type="button" onclick="eliminarNota(${idx})" class="text-alert-red hover:bg-red-50 p-0.5 rounded cursor-pointer" title="Eliminar Nota"><i data-lucide="trash-2" class="size-3"></i></button>
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
        <th class="p-3 pr-5 text-center w-36">Asistencia (${formatearFechaCorta(fecha)})</th>
    `;

    tbody.innerHTML = '';

    if (estudiantesDataList.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="${notasList.length + 4}" class="p-8 text-center text-text-muted italic">
                    No hay estudiantes asignados a este curso actualmente. <br>
                    <span class="text-[11px] font-normal text-text-secondary mt-1 inline-block">Haga clic en el botón <strong>'Auto-Mapear Matriculados'</strong> superior para asignar automáticamente estudiantes matriculados en este grado.</span>
                </td>
            </tr>
        `;
        if (typeof lucide !== 'undefined' && lucide.createIcons) lucide.createIcons();
        recalcularPonderacionesYNotas();
        return;
    }

    estudiantesDataList.forEach((est, index) => {
        const tr = document.createElement('tr');
        tr.className = "hover:bg-[#fcfdfb] transition-colors est-row";
        tr.setAttribute('data-id', est.id);

        let tdsNotasHTML = '';
        notasList.forEach((notaObj, idx) => {
            const val = (notaObj.estudiantesNotas && notaObj.estudiantesNotas[est.id] !== undefined) 
                ? notaObj.estudiantesNotas[est.id] 
                : 0.0;
            tdsNotasHTML += `
                <td class="p-2.5 text-center border-x border-[#c4eec0]/20 px-1">
                    <input type="number" step="0.1" min="0" max="5" value="${val}" oninput="recalcularFilaEstudiante(this, '${est.id}', ${idx})" class="w-12 p-1 text-center font-bold border border-[#c4eec0] rounded bg-white text-sidebar focus:ring-1 focus:ring-sidebar n-input text-[12px]">
                </td>
            `;
        });

        const estadoAsistencia = asistenciasMap[est.id] || 'Presente';

        tr.innerHTML = `
            <td class="p-3 pl-5 font-bold text-text-secondary text-center">${est.numIdx || (index + 1)}</td>
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
                        onclick="toggleAsistencia(this, '${est.id}')" 
                        data-estado="${estadoAsistencia}"
                        class="px-2.5 py-1 rounded-full text-[10px] font-black uppercase tracking-wide transition-all cursor-pointer flex items-center justify-center gap-1 mx-auto badge-asistencia ${getClassesAsistencia(estadoAsistencia)}">
                    ${getIconAsistencia(estadoAsistencia)}
                    <span class="ast-text">${estadoAsistencia}</span>
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
    const materia = getMateriaActiva();
    const fecha = getFechaActiva();
    const fechaDisplay = formatearFechaCorta(fecha);

    const notasList = getNotasListMateria(materia, periodoActivo);
    const numNueva = notasList.length + 1;
    const pesoSugerido = Math.round(100 / numNueva);

    const nuevaNota = {
        nombre: 'Nota ' + numNueva,
        fecha: fecha,
        fechaDisplay: fechaDisplay,
        peso: pesoSugerido,
        estudiantesNotas: {}
    };

    estudiantesDataList.forEach(est => {
        nuevaNota.estudiantesNotas[est.id] = 0.0;
    });

    notasList.push(nuevaNota);

    // Rebalancear pesos equitativamente
    notasList.forEach(n => n.peso = pesoSugerido);

    renderizarTablaNotas();
    guardarEnLocalStorage();
}

function eliminarNota(idx) {
    const materia = getMateriaActiva();
    const notasList = getNotasListMateria(materia, periodoActivo);
    
    notasList.splice(idx, 1);

    if (notasList.length > 0) {
        const pesoNuevo = Math.round(100 / notasList.length);
        notasList.forEach(n => n.peso = pesoNuevo);
    }

    renderizarTablaNotas();
    guardarEnLocalStorage();
}

function actualizarPesoNota(idx, valor) {
    const materia = getMateriaActiva();
    const notasList = getNotasListMateria(materia, periodoActivo);
    const valNum = parseFloat(valor) || 0;
    if (notasList[idx]) {
        notasList[idx].peso = valNum;
    }
    recalcularPonderacionesYNotas();
    guardarEnLocalStorage();
}

function recalcularFilaEstudiante(inputElem, estId, notaIdx) {
    const materia = getMateriaActiva();
    const notasList = getNotasListMateria(materia, periodoActivo);
    const valNum = parseFloat(inputElem.value) || 0.0;
    if (notasList[notaIdx]) {
        if (!notasList[notaIdx].estudiantesNotas) notasList[notaIdx].estudiantesNotas = {};
        notasList[notaIdx].estudiantesNotas[estId] = valNum;
    }
    recalcularPonderacionesYNotas();
    guardarEnLocalStorage();
}

function recalcularPonderacionesYNotas() {
    const materia = getMateriaActiva();
    const notasList = getNotasListMateria(materia, periodoActivo);

    let sumaPesos = 0;
    notasList.forEach(n => sumaPesos += (parseFloat(n.peso) || 0));

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
        if (!estId) return;

        let notaFinal = 0.0;

        if (notasList.length > 0 && sumaPesos > 0) {
            let sumaPonderada = 0.0;
            notasList.forEach((n) => {
                const notaVal = (n.estudiantesNotas && n.estudiantesNotas[estId] !== undefined) 
                    ? parseFloat(n.estudiantesNotas[estId]) 
                    : 0.0;
                const pesoVal = (parseFloat(n.peso) || 0);
                sumaPonderada += (notaVal * pesoVal);
            });
            notaFinal = sumaPonderada / sumaPesos;
        }

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

function prepararPromocion(event) {
    if (!confirm('¿Desea ejecutar el proceso de promoción/graduación para los estudiantes aprobados (nota final ≥ 3.0)?')) {
        return false;
    }

    const notasEstudiantesMap = {};
    document.querySelectorAll('#tbody-estudiantes tr').forEach(tr => {
        const estId = tr.getAttribute('data-id');
        const badge = tr.querySelector('.nota-final-badge');
        if (estId && badge) {
            const notaVal = parseFloat(badge.innerText) || 0.0;
            notasEstudiantesMap[estId] = notaVal;
        }
    });

    const hiddenInput = document.getElementById('input-promover-notas-json');
    if (hiddenInput) {
        hiddenInput.value = JSON.stringify(notasEstudiantesMap);
    }
    return true;
}

function guardarCambiosDetalle() {
    localStorage.setItem('siga_periodo_activo', periodoActivo);
    guardarEnLocalStorage();
    const materia = getMateriaActiva();
    alert(`¡Cambios guardados exitosamente para ${materia} en el Periodo ${periodoActivo}!`);
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

function toggleAsistencia(btn, estId) {
    const materia = getMateriaActiva();
    const fecha = getFechaActiva();
    const asistenciasMap = getAsistenciasMateriaFecha(materia, fecha);

    const actual = btn.getAttribute('data-estado') || 'Presente';
    let nuevo = 'Presente';

    if (actual === 'Presente') nuevo = 'No presente';
    else if (actual === 'No presente') nuevo = 'Excusado';

    asistenciasMap[estId] = nuevo;
    guardarEnLocalStorage();

    btn.setAttribute('data-estado', nuevo);
    btn.className = `px-2.5 py-1 rounded-full text-[10px] font-black uppercase tracking-wide transition-all cursor-pointer flex items-center justify-center gap-1 mx-auto badge-asistencia ${getClassesAsistencia(nuevo)}`;
    btn.innerHTML = `${getIconAsistencia(nuevo)}<span class="ast-text">${nuevo}</span>`;

    if (window.lucide) lucide.createIcons();
}
