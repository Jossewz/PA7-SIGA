function handleRowClick(tr, event) {
    if (event.target.closest('button')) return;
    window.location.href = '/clases/gestion?codigo=' + encodeURIComponent(tr.dataset.codigo);
}

function handleHorarioBtn(btn, event) {
    if (event) event.stopPropagation();
    openHorarioModal(btn.dataset.id, btn.dataset.codigo);
}

function normalizarDia(dia) {
    if (!dia) return '';
    return dia.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase().trim();
}

function getMateriasList() {
    if (window.materiasGlobales && Array.isArray(window.materiasGlobales) && window.materiasGlobales.length > 0) {
        return window.materiasGlobales;
    }
    const selectMat = document.querySelector('select[name*="_materiaId"]');
    if (selectMat) {
        const list = [];
        Array.from(selectMat.options).forEach(opt => {
            if (opt.value) list.push({ id: opt.value, nombre: opt.text });
        });
        if (list.length > 0) return list;
    }
    return [];
}

function getDocentesList() {
    if (window.docentesGlobales && Array.isArray(window.docentesGlobales) && window.docentesGlobales.length > 0) {
        return window.docentesGlobales;
    }
    const selectDoc = document.querySelector('select[name*="_docenteId"]');
    if (selectDoc) {
        const list = [];
        Array.from(selectDoc.options).forEach(opt => {
            if (opt.value) list.push({ id: opt.value, nombreCompleto: opt.text });
        });
        if (list.length > 0) return list;
    }
    return [];
}

function renderFilaBloque(slotIdx, inicioVal = '07:00', finVal = '08:30', diasData = {}) {
    const tr = document.createElement('tr');
    tr.className = 'slot-row hover:bg-slate-50/50';

    const materias = getMateriasList();
    const docentes = getDocentesList();
    const dias = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes'];

    let html = `
        <td class="p-3 text-left bg-[#f7fcf6]/50 border-r border-[#c4eec0]/60 font-bold">
            <div class="flex justify-between items-center mb-1">
                <span class="text-sidebar text-[11px] font-black slot-title">Bloque ${slotIdx + 1}</span>
                <button type="button" onclick="eliminarFilaHorario(this)" class="text-text-secondary hover:text-alert-red p-1 rounded cursor-pointer" title="Eliminar bloque">
                    <i data-lucide="trash-2" class="size-3.5"></i>
                </button>
            </div>
            <div class="flex items-center gap-1">
                <input type="time" name="slot_${slotIdx}_inicio" value="${inicioVal}" class="p-1 border border-[#c4eec0] rounded text-[10px] font-bold bg-white text-center w-20">
                <span class="text-text-secondary font-bold">-</span>
                <input type="time" name="slot_${slotIdx}_fin" value="${finVal}" class="p-1 border border-[#c4eec0] rounded text-[10px] font-bold bg-white text-center w-20">
            </div>
        </td>
    `;

    dias.forEach((dia, dIdx) => {
        const borderClass = dIdx < 4 ? 'border-r border-[#c4eec0]/30' : '';
        
        let diaData = diasData[dia];
        if (!diaData) {
            const normDia = normalizarDia(dia);
            for (const k in diasData) {
                if (normalizarDia(k) === normDia) {
                    diaData = diasData[k];
                    break;
                }
            }
        }
        diaData = diaData || {};

        const selMatId = diaData.materiaId ? String(diaData.materiaId).toLowerCase() : '';
        const selDocId = diaData.docenteId ? String(diaData.docenteId).toLowerCase() : '';

        const matOptionsHtml = materias.map(m => {
            const mId = String(m.id).toLowerCase();
            const isSelected = (selMatId && mId === selMatId) ? 'selected' : '';
            return `<option value="${m.id}" ${isSelected}>${m.nombre}</option>`;
        }).join('');

        const docOptionsHtml = docentes.map(d => {
            const dId = String(d.id).toLowerCase();
            const nombreDoc = d.nombreCompleto || `${d.nombres || ''} ${d.apellidos || ''}`.trim();
            const isSelected = (selDocId && dId === selDocId) ? 'selected' : '';
            return `<option value="${d.id}" ${isSelected}>${nombreDoc}</option>`;
        }).join('');

        html += `
            <td class="p-2 ${borderClass}">
                <select name="slot_${slotIdx}_${dia}_materiaId" class="w-full p-1.5 bg-white border border-[#c4eec0] rounded-lg text-[10px] font-bold text-text-primary mb-1 focus:ring-1 focus:ring-sidebar">
                    <option value="">-- Materia --</option>
                    ${matOptionsHtml}
                </select>
                <select name="slot_${slotIdx}_${dia}_docenteId" class="w-full p-1.5 bg-white border border-[#c4eec0] rounded-lg text-[10px] font-semibold text-sidebar focus:ring-1 focus:ring-sidebar">
                    <option value="">-- Docente --</option>
                    ${docOptionsHtml}
                </select>
            </td>
        `;
    });

    tr.innerHTML = html;
    return tr;
}

// Modal Horario
async function openHorarioModal(cursoId, codigo) {
    const titleElem = document.getElementById('horario-title');
    if (titleElem) titleElem.innerText = 'Asignación de Horario – Curso ' + (codigo || '');

    const cursoIdInput = document.getElementById('h-curso-id');
    if (cursoIdInput) cursoIdInput.value = cursoId || '';

    const tbody = document.getElementById('horario-tbody');
    if (tbody) {
        tbody.innerHTML = `<tr><td colspan="6" class="p-8 text-center text-text-secondary font-bold text-xs"><i data-lucide="loader-2" class="size-4 animate-spin inline mr-2"></i>Cargando horario asignado...</td></tr>`;
        if (window.lucide) lucide.createIcons();
    }

    const modal = document.getElementById('horario-modal');
    const dialog = document.getElementById('horario-modal-dialog');
    modal.classList.remove('hidden');
    setTimeout(() => {
        dialog.classList.remove('scale-95', 'opacity-0');
        dialog.classList.add('scale-100', 'opacity-100');
    }, 10);

    try {
        const resp = await fetch('/clases/horarios/datos?cursoId=' + encodeURIComponent(cursoId));
        if (!resp.ok) {
            throw new Error(`HTTP ${resp.status}`);
        }
        const horarios = await resp.json();

        if (tbody) {
            tbody.innerHTML = '';

            // Definir los 3 bloques regulares con su receso pedagógico
            const baseSlots = [
                { inicio: '07:00', fin: '08:30', dias: {} },
                { inicio: '08:30', fin: '10:00', dias: {} },
                { esReceso: true, label: '☕ Receso Pedagógico (10:00 - 10:30)' },
                { inicio: '10:30', fin: '12:30', dias: {} }
            ];

            // Si hay horarios en BD, colocarlos en los bloques regulares o añadir bloques extras
            if (horarios && Array.isArray(horarios) && horarios.length > 0) {
                horarios.forEach(h => {
                    const hInicio = (h.horaInicio || '07:00').substring(0, 5);
                    const hFin = (h.horaFin || '08:30').substring(0, 5);

                    // Buscar si coincide con alguno de los bloques base (que no sea receso)
                    let slotMatch = baseSlots.find(s => !s.esReceso && s.inicio === hInicio);
                    if (!slotMatch) {
                        // Si es una franja diferente, agregar un bloque adicional
                        slotMatch = { inicio: hInicio, fin: hFin, dias: {} };
                        baseSlots.push(slotMatch);
                    }
                    slotMatch.dias[h.dia] = {
                        materiaId: h.materiaId,
                        docenteId: h.docenteId
                    };
                });
            }

            let slotIdx = 0;
            baseSlots.forEach(slot => {
                if (slot.esReceso) {
                    const trReceso = document.createElement('tr');
                    trReceso.className = 'bg-[#f7fcf6]';
                    trReceso.innerHTML = `<td colspan="6" class="p-2 font-black text-sidebar text-[10px] uppercase tracking-wider text-center">${slot.label}</td>`;
                    tbody.appendChild(trReceso);
                } else {
                    const tr = renderFilaBloque(slotIdx, slot.inicio, slot.fin, slot.dias);
                    tbody.appendChild(tr);
                    slotIdx++;
                }
            });

            renumerarBloquesHorario();
            if (window.lucide) lucide.createIcons();
        }
    } catch (err) {
        console.error('Error al cargar datos del horario:', err);
        if (tbody) {
            tbody.innerHTML = `<tr><td colspan="6" class="p-4 text-center text-red-600 font-bold text-xs">Error al cargar horarios: ${err.message}</td></tr>`;
        }
    }
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

    let horaInicio = '12:30';
    let horaFin = '13:30';
    if (slotRows.length > 0) {
        const lastRow = slotRows[slotRows.length - 1];
        const lastFinInput = lastRow.querySelector('input[name*="_fin"]');
        if (lastFinInput && lastFinInput.value) {
            horaInicio = lastFinInput.value;
            const [h, m] = horaInicio.split(':').map(Number);
            const finH = String((h + 1) % 24).padStart(2, '0');
            horaFin = `${finH}:${String(m || 0).padStart(2, '0')}`;
        }
    }

    const tr = renderFilaBloque(nextIdx, horaInicio, horaFin, {});
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
    const slotRows = tbody.querySelectorAll('tr.slot-row');
    const dias = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes'];

    slotRows.forEach((row, idx) => {
        const titleSpan = row.querySelector('.slot-title');
        if (titleSpan) titleSpan.innerText = `Bloque ${idx + 1}`;

        const inicioInput = row.querySelector('input[name*="_inicio"]');
        if (inicioInput) inicioInput.name = `slot_${idx}_inicio`;

        const finInput = row.querySelector('input[name*="_fin"]');
        if (finInput) finInput.name = `slot_${idx}_fin`;

        dias.forEach(dia => {
            const matSel = row.querySelector(`select[name*="_${dia}_materiaId"], select[name*="_${dia.replace('é', 'e')}_materiaId"]`);
            if (matSel) matSel.name = `slot_${idx}_${dia}_materiaId`;

            const docSel = row.querySelector(`select[name*="_${dia}_docenteId"], select[name*="_${dia.replace('é', 'e')}_docenteId"]`);
            if (docSel) docSel.name = `slot_${idx}_${dia}_docenteId`;
        });
    });
}
