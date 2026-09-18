/* =======================================================
   SIGA-IEA: GESTIÓN DE CURSO, HORARIOS Y TABLA HTMX
   Las notas y asistencias son gestionadas por PostgreSQL vía HTMX.
   ======================================================= */

let horariosList = (window.horariosBDData && Array.isArray(window.horariosBDData)) 
    ? window.horariosBDData 
    : [];

let periodoActivo = '1';

function getHoyFechaLocalStr() {
    const d = new Date();
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

document.addEventListener('DOMContentLoaded', () => {
    if (window.horariosBDData && Array.isArray(window.horariosBDData)) {
        horariosList = window.horariosBDData;
    }

    const fechaInput = document.getElementById('fecha-evaluacion');
    if (fechaInput) {
        const todayStr = getHoyFechaLocalStr();
        fechaInput.value = todayStr;
        fechaInput.setAttribute('max', todayStr);
        actualizarMateriaPorFecha(false);
    }

    seleccionarPeriodo('1', false);
    recargarTablaNotas();
});

function actualizarMateriaPorFecha(recargar = true) {
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

    const materiasDelDia = horariosList.filter(h => h.dia && h.dia.toLowerCase() === diaNombre.toLowerCase());
    const selectMateria = document.getElementById('select-materia');
    const bannerInfo = document.getElementById('horario-info-text');

    if (!selectMateria) return;
    selectMateria.innerHTML = '';

    if (materiasDelDia.length > 0) {
        materiasDelDia.forEach((m) => {
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
        opt.value = "General";
        opt.text = "General (Sin horario específico hoy)";
        selectMateria.appendChild(opt);
        selectMateria.disabled = false;
        if (bannerInfo) bannerInfo.innerText = "No hay clases programadas en el horario para este día.";
    }

    if (window.lucide) lucide.createIcons();
    if (recargar) {
        recargarTablaNotas();
    }
}

function cambiarMateriaSeleccionada(materiaNombre) {
    const diaLabel = document.getElementById('dia-nombre');
    const diaNombre = diaLabel ? diaLabel.innerText : 'Lunes';
    const materiaObj = horariosList.find(h => h.dia && h.dia.toLowerCase() === diaNombre.toLowerCase() && h.materia === materiaNombre);

    const bannerInfo = document.getElementById('horario-info-text');
    if (bannerInfo && materiaObj) {
        bannerInfo.innerText = `Horario: ${materiaObj.hora}${materiaObj.docente ? ' • Docente: ' + materiaObj.docente : ''}`;
    }

    recargarTablaNotas();
}

function seleccionarPeriodo(num, recargar = true) {
    periodoActivo = String(num);
    const hiddenInput = document.getElementById('input-periodo-activo');
    if (hiddenInput) hiddenInput.value = periodoActivo;

    ['1', '2', '3'].forEach(p => {
        const btn = document.getElementById('btn-p' + p);
        if (btn) {
            if (p === periodoActivo) {
                btn.className = "px-4 py-2 rounded-md text-[12px] font-black bg-sidebar text-white shadow-2xs cursor-pointer";
            } else {
                btn.className = "px-4 py-2 rounded-md text-[12px] font-bold text-text-secondary hover:text-sidebar hover:bg-[#f7fcf6] cursor-pointer";
            }
        }
    });

    const lbl = document.getElementById('periodo-activo-label');
    if (lbl) lbl.innerText = `Periodo ${num}`;

    if (recargar) {
        recargarTablaNotas();
    }
}

function recargarTablaNotas() {
    const cursoId = document.getElementById('input-curso-id')?.value;
    if (!cursoId || !window.htmx) return;

    const selectMateria = document.getElementById('select-materia');
    const materiaNombre = (selectMateria && selectMateria.value) ? selectMateria.value : 'Matemáticas';
    const fecha = document.getElementById('fecha-evaluacion')?.value || getHoyFechaLocalStr();

    htmx.ajax('GET', '/clases/fragmento/tabla-notas', {
        target: '#contenedor-tabla-notas',
        swap: 'outerHTML',
        values: {
            cursoId: cursoId,
            materiaNombre: materiaNombre,
            periodo: periodoActivo,
            fecha: fecha
        }
    });
}

function prepararPromocion(event) {
    return confirm('¿Desea ejecutar el proceso de promoción/graduación para los estudiantes aprobados (nota final ≥ 3.0)?');
}
