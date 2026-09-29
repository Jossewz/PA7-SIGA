// =========================================================================
// SIGA - GESTIÓN DE CURSOS Y HORARIO ESTILO GOOGLE CALENDAR / SEGI
// =========================================================================

let vistaActualHorario = 'semana';
window.horariosActuales = [];

function handleRowClick(tr, event) {
    if (event.target.closest('button') || event.target.closest('form')) return;
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
    const selectMat = document.getElementById('fc-materia');
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
    const selectDoc = document.getElementById('fc-docente');
    if (selectDoc) {
        const list = [];
        Array.from(selectDoc.options).forEach(opt => {
            if (opt.value) list.push({ id: opt.value, nombreCompleto: opt.text });
        });
        if (list.length > 0) return list;
    }
    return [];
}

// -------------------------------------------------------------------------
// MODAL HORARIO (GOOGLE CALENDAR / SEGI STYLE)
// -------------------------------------------------------------------------
async function openHorarioModal(cursoId, codigo) {
    const titleElem = document.getElementById('horario-title');
    if (titleElem) titleElem.innerText = 'Horario Semanal — ' + (codigo ? 'Curso ' + codigo : 'Curso');

    const cursoIdInput = document.getElementById('h-curso-id');
    if (cursoIdInput) cursoIdInput.value = cursoId || '';

    const modal = document.getElementById('horario-modal');
    const dialog = document.getElementById('horario-modal-dialog');
    const loading = document.getElementById('horario-loading');
    const semanaContainer = document.getElementById('horario-semana-container');
    const agendaContainer = document.getElementById('horario-agenda-container');

    if (loading) loading.classList.remove('hidden');
    if (semanaContainer) semanaContainer.classList.add('hidden');
    if (agendaContainer) agendaContainer.classList.add('hidden');

    modal.classList.remove('hidden');
    setTimeout(() => {
        dialog.classList.remove('scale-95', 'opacity-0');
        dialog.classList.add('scale-100', 'opacity-100');
    }, 10);

    vistaActualHorario = 'semana';
    actualizarBotonesVista();

    try {
        const resp = await fetch('/clases/horarios/datos?cursoId=' + encodeURIComponent(cursoId));
        if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
        const horarios = await resp.json();
        window.horariosActuales = Array.isArray(horarios) ? horarios : [];
        renderHorarioCompleto();
    } catch (err) {
        console.error('Error al cargar datos del horario:', err);
        if (loading) {
            loading.innerHTML = `<span class="text-alert-red font-bold">Error al cargar horarios: ${err.message}</span>`;
        }
    }
}

function closeHorarioModal() {
    const modal = document.getElementById('horario-modal');
    const dialog = document.getElementById('horario-modal-dialog');
    dialog.classList.remove('scale-100', 'opacity-100');
    dialog.classList.add('scale-95', 'opacity-0');
    setTimeout(() => {
        modal.classList.add('hidden');
        cerrarModalFormClase();
    }, 250);
}

function cambiarVistaHorario(vista) {
    vistaActualHorario = vista;
    actualizarBotonesVista();
    renderHorarioCompleto();
}

function actualizarBotonesVista() {
    const btnSemana = document.getElementById('btn-vista-semana');
    const btnAgenda = document.getElementById('btn-vista-agenda');
    if (!btnSemana || !btnAgenda) return;

    if (vistaActualHorario === 'semana') {
        btnSemana.className = "inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-black transition-all bg-sidebar text-white shadow-2xs";
        btnAgenda.className = "inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-black transition-all text-text-secondary hover:text-sidebar";
    } else {
        btnAgenda.className = "inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-black transition-all bg-sidebar text-white shadow-2xs";
        btnSemana.className = "inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-black transition-all text-text-secondary hover:text-sidebar";
    }
}

function calcularHorasTotales(horarios) {
    let minutosTotales = 0;
    horarios.forEach(h => {
        if (!h.horaInicio || !h.horaFin) return;
        const [h1, m1] = h.horaInicio.split(':').map(Number);
        const [h2, m2] = h.horaFin.split(':').map(Number);
        const min1 = h1 * 60 + m1;
        const min2 = h2 * 60 + m2;
        if (min2 > min1) {
            minutosTotales += (min2 - min1);
        }
    });
    const horas = Math.floor(minutosTotales / 60);
    const mins = minutosTotales % 60;
    if (mins === 0) return `${horas} horas lectivas semanales`;
    return `${horas}h ${mins}m semanales`;
}

function renderHorarioCompleto() {
    const loading = document.getElementById('horario-loading');
    const semanaContainer = document.getElementById('horario-semana-container');
    const agendaContainer = document.getElementById('horario-agenda-container');
    const badgeConteo = document.getElementById('horario-conteo-badge');
    const labelHorasTotales = document.getElementById('horario-horas-totales');
    const labelMateriasTotales = document.getElementById('horario-materias-totales');

    if (loading) loading.classList.add('hidden');

    const totalClases = window.horariosActuales.length;
    if (badgeConteo) {
        badgeConteo.innerText = `${totalClases} ${totalClases === 1 ? 'clase' : 'clases'}`;
    }

    if (labelHorasTotales) {
        const textoHoras = calcularHorasTotales(window.horariosActuales);
        labelHorasTotales.innerHTML = `<i data-lucide="clock" class="size-4 stroke-[2.5]"></i><span>${textoHoras}</span>`;
    }

    const materiasSet = new Set(window.horariosActuales.map(h => h.materiaNombre).filter(Boolean));
    if (labelMateriasTotales) {
        labelMateriasTotales.innerText = `${materiasSet.size} asignaturas distintas`;
    }

    if (vistaActualHorario === 'semana') {
        if (semanaContainer) semanaContainer.classList.remove('hidden');
        if (agendaContainer) agendaContainer.classList.add('hidden');
        renderVistaSemanalGoogleCalendar();
    } else {
        if (agendaContainer) agendaContainer.classList.remove('hidden');
        if (semanaContainer) semanaContainer.classList.add('hidden');
        renderVistaAgendaSEGI();
    }

    if (window.lucide) lucide.createIcons();
}

// -------------------------------------------------------------------------
// VISTA SEMANAL TIPO GOOGLE CALENDAR
// -------------------------------------------------------------------------
function renderVistaSemanalGoogleCalendar() {
    const dias = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes'];

    dias.forEach(dia => {
        const colContainer = document.getElementById(`cards-${dia}`);
        const diaCol = document.querySelector(`.dia-col[data-dia="${dia}"]`);
        if (!colContainer) return;

        colContainer.innerHTML = '';

        const clasesDelDia = window.horariosActuales.filter(h => {
            return normalizarDia(h.dia) === normalizarDia(dia);
        });

        // Ordenar cronológicamente por hora de inicio
        clasesDelDia.sort((a, b) => (a.horaInicio || '').localeCompare(b.horaInicio || ''));

        if (diaCol) {
            const countSpan = diaCol.querySelector('.dia-count');
            if (countSpan) countSpan.innerText = clasesDelDia.length;
        }

        if (clasesDelDia.length === 0) {
            colContainer.innerHTML = `
                <div class="h-32 border-2 border-dashed border-[#c4eec0]/50 rounded-xl flex flex-col items-center justify-center p-3 text-center text-[#9db89a] text-[11px] font-semibold">
                    <i data-lucide="calendar" class="size-5 mb-1 opacity-50"></i>
                    <span>Sin clases asignadas</span>
                </div>
            `;
            return;
        }

        clasesDelDia.forEach(c => {
            const card = document.createElement('div');
            const color = c.color || '#0d4117';
            card.className = "group relative bg-white border border-[#c4eec0] hover:border-sidebar/60 rounded-xl p-2.5 shadow-2xs hover:shadow-md transition-all flex flex-col gap-1.5 cursor-pointer";
            card.style.borderLeftWidth = "4px";
            card.style.borderLeftColor = color;

            const horaFormat = `${formatearHora12(c.horaInicio)} - ${formatearHora12(c.horaFin)}`;

            card.innerHTML = `
                <div class="flex items-start justify-between gap-1">
                    <span class="text-[12px] font-black text-sidebar leading-tight line-clamp-2">
                        ${c.materiaNombre || 'Sin Asignatura'}
                    </span>
                    <div class="opacity-0 group-hover:opacity-100 transition-opacity flex items-center gap-1 shrink-0">
                        <button type="button" onclick="event.stopPropagation(); abrirModalEditarClase('${c.id}')"
                                class="p-1 hover:bg-[#eafbe4] text-sidebar rounded-md transition-colors" title="Editar horario">
                            <i data-lucide="pencil" class="size-3 stroke-[2.2]"></i>
                        </button>
                        <button type="button" onclick="event.stopPropagation(); eliminarClaseItem('${c.id}')"
                                class="p-1 hover:bg-red-50 text-alert-red rounded-md transition-colors" title="Eliminar clase">
                            <i data-lucide="trash-2" class="size-3 stroke-[2.2]"></i>
                        </button>
                    </div>
                </div>

                <div class="flex items-center gap-1.5 text-[10px] font-bold text-text-secondary">
                    <i data-lucide="clock-3" class="size-3 stroke-2 text-sidebar"></i>
                    <span class="tabular-nums font-black text-brand-900">${horaFormat}</span>
                </div>

                <div class="flex items-center justify-between gap-1 pt-1 border-t border-[#f0f9ef]">
                    <span class="text-[10px] font-semibold text-text-secondary truncate flex items-center gap-1" title="${c.docenteNombre || 'Sin Docente'}">
                        <i data-lucide="user-tie" class="size-3 stroke-2 text-text-muted shrink-0"></i>
                        <span class="truncate">${c.docenteNombre ? c.docenteNombre.split(' ')[0] : 'Sin Docente'}</span>
                    </span>
                    <span class="px-1.5 py-0.5 rounded bg-[#eafbe4] text-sidebar border border-[#c4eec0] text-[9px] font-black uppercase tracking-wider shrink-0">
                        ${c.salon || 'Aula 101'}
                    </span>
                </div>
            `;

            card.onclick = () => abrirModalEditarClase(c.id);
            colContainer.appendChild(card);
        });
    });
}

// -------------------------------------------------------------------------
// VISTA AGENDA TIPO SEGI (LISTA CRONOLÓGICA)
// -------------------------------------------------------------------------
function renderVistaAgendaSEGI() {
    const container = document.getElementById('horario-agenda-container');
    if (!container) return;
    container.innerHTML = '';

    const dias = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes'];

    if (window.horariosActuales.length === 0) {
        container.innerHTML = `
            <div class="p-12 text-center text-text-secondary font-bold text-xs bg-[#f7fcf6] border border-[#c4eec0] rounded-2xl">
                <i data-lucide="calendar-x" class="size-8 mx-auto text-[#a5cca0] mb-2"></i>
                <p class="text-sidebar font-black text-sm mb-1">No hay clases programadas</p>
                <p>Haga clic en "+ Programar Clase" para comenzar a estructurar el horario del curso.</p>
            </div>
        `;
        return;
    }

    dias.forEach(dia => {
        const clasesDelDia = window.horariosActuales.filter(h => {
            return normalizarDia(h.dia) === normalizarDia(dia);
        });

        if (clasesDelDia.length === 0) return;

        clasesDelDia.sort((a, b) => (a.horaInicio || '').localeCompare(b.horaInicio || ''));

        const bloqueDia = document.createElement('div');
        bloqueDia.className = "bg-white border border-[#c4eec0] rounded-2xl overflow-hidden shadow-2xs";

        let tarjetasHtml = '';
        clasesDelDia.forEach(c => {
            const color = c.color || '#0d4117';
            const horaFormat = `${formatearHora12(c.horaInicio)} — ${formatearHora12(c.horaFin)}`;

            tarjetasHtml += `
                <div class="flex items-center justify-between p-3.5 hover:bg-[#f7fcf6] transition-colors border-b last:border-b-0 border-[#c4eec0]/40 group">
                    <div class="flex items-center gap-3.5 min-w-0">
                        <span class="w-3 h-3 rounded-full shrink-0 ring-2 ring-white shadow-2xs" style="background-color: ${color}"></span>
                        <div class="flex flex-col">
                            <div class="flex items-center gap-2">
                                <span class="px-2 py-0.5 rounded-lg bg-[#eafbe4] text-sidebar border border-[#c4eec0] text-[11px] font-black tabular-nums">
                                    ${horaFormat}
                                </span>
                                <strong class="text-sm font-black text-sidebar truncate">${c.materiaNombre || 'Sin Asignatura'}</strong>
                            </div>
                            <div class="flex items-center gap-3 text-xs font-semibold text-text-secondary mt-1">
                                <span class="flex items-center gap-1">
                                    <i data-lucide="user-tie" class="size-3.5 stroke-2"></i>
                                    <span>${c.docenteNombre || 'Sin docente asignado'}</span>
                                </span>
                                <span>•</span>
                                <span class="flex items-center gap-1 text-sidebar font-bold">
                                    <i data-lucide="map-pin" class="size-3.5 stroke-2"></i>
                                    <span>${c.salon || 'Aula 101'}</span>
                                </span>
                            </div>
                        </div>
                    </div>

                    <div class="flex items-center gap-1.5 shrink-0 opacity-80 group-hover:opacity-100 transition-opacity">
                        <button type="button" onclick="abrirModalEditarClase('${c.id}')"
                                class="px-3 py-1.5 rounded-lg border border-[#c4eec0] bg-white hover:bg-[#eafbe4] text-sidebar text-xs font-bold transition-all flex items-center gap-1">
                            <i data-lucide="pencil" class="size-3 stroke-[2.2]"></i>
                            <span>Editar</span>
                        </button>
                        <button type="button" onclick="eliminarClaseItem('${c.id}')"
                                class="px-2.5 py-1.5 rounded-lg border border-red-200 bg-white hover:bg-red-50 text-alert-red text-xs font-bold transition-all" title="Eliminar clase">
                            <i data-lucide="trash-2" class="size-3.5 stroke-[2.2]"></i>
                        </button>
                    </div>
                </div>
            `;
        });

        bloqueDia.innerHTML = `
            <div class="px-5 py-3 bg-[#eafbe4]/60 border-b border-[#c4eec0] flex items-center justify-between">
                <div class="flex items-center gap-2">
                    <span class="w-3 h-3 rounded-full bg-sidebar"></span>
                    <h4 class="text-sidebar text-sm font-black uppercase tracking-wider">${dia}</h4>
                </div>
                <span class="text-xs font-black text-sidebar">${clasesDelDia.length} ${clasesDelDia.length === 1 ? 'clase' : 'clases'}</span>
            </div>
            <div class="divide-y divide-[#c4eec0]/40">
                ${tarjetasHtml}
            </div>
        `;

        container.appendChild(bloqueDia);
    });
}

// -------------------------------------------------------------------------
// FORMULARIO DINÁMICO DE CREAR / EDITAR CLASE (HORAS TOTALMENTE FLEXIBLES)
// -------------------------------------------------------------------------
function abrirModalCrearClase(diaSugerido) {
    const modalForm = document.getElementById('modal-form-clase');
    const formTitle = document.getElementById('form-clase-titulo');
    const btnText = document.getElementById('fc-btn-text');

    if (formTitle) formTitle.innerText = "Programar Nueva Clase";
    if (btnText) btnText.innerText = "Guardar Clase";

    const idInput = document.getElementById('fc-horario-id');
    const diaSelect = document.getElementById('fc-dia');
    const horaInicioInput = document.getElementById('fc-hora-inicio');
    const horaFinInput = document.getElementById('fc-hora-fin');
    const materiaSelect = document.getElementById('fc-materia');
    const docenteSelect = document.getElementById('fc-docente');
    const salonSelect = document.getElementById('fc-salon');
    const salonCustom = document.getElementById('fc-salon-custom');

    if (idInput) idInput.value = '';
    if (diaSelect && diaSugerido) diaSelect.value = diaSugerido;
    if (horaInicioInput) horaInicioInput.value = '07:00';
    if (horaFinInput) horaFinInput.value = '08:30';
    if (materiaSelect) materiaSelect.selectedIndex = 0;
    if (docenteSelect) docenteSelect.selectedIndex = 0;
    if (salonSelect) salonSelect.value = 'Aula 101';
    if (salonCustom) salonCustom.value = '';

    if (modalForm) modalForm.classList.remove('hidden');
    if (window.lucide) lucide.createIcons();
}

function abrirModalEditarClase(horarioId) {
    const clase = window.horariosActuales.find(h => String(h.id) === String(horarioId));
    if (!clase) return;

    const modalForm = document.getElementById('modal-form-clase');
    const formTitle = document.getElementById('form-clase-titulo');
    const btnText = document.getElementById('fc-btn-text');

    if (formTitle) formTitle.innerText = "Editar Clase";
    if (btnText) btnText.innerText = "Actualizar Cambios";

    const idInput = document.getElementById('fc-horario-id');
    const diaSelect = document.getElementById('fc-dia');
    const horaInicioInput = document.getElementById('fc-hora-inicio');
    const horaFinInput = document.getElementById('fc-hora-fin');
    const materiaSelect = document.getElementById('fc-materia');
    const docenteSelect = document.getElementById('fc-docente');
    const salonSelect = document.getElementById('fc-salon');
    const salonCustom = document.getElementById('fc-salon-custom');

    if (idInput) idInput.value = clase.id;
    if (diaSelect) diaSelect.value = clase.dia;
    if (horaInicioInput) horaInicioInput.value = (clase.horaInicio || '07:00').substring(0, 5);
    if (horaFinInput) horaFinInput.value = (clase.horaFin || '08:30').substring(0, 5);
    if (materiaSelect) materiaSelect.value = clase.materiaId || '';
    if (docenteSelect) docenteSelect.value = clase.docenteId || '';
    
    if (salonSelect) {
        salonSelect.value = clase.salon || 'Aula 101';
    }
    if (salonCustom) {
        salonCustom.value = clase.salon || '';
    }

    if (modalForm) modalForm.classList.remove('hidden');
    if (window.lucide) lucide.createIcons();
}

function cerrarModalFormClase() {
    const modalForm = document.getElementById('modal-form-clase');
    if (modalForm) modalForm.classList.add('hidden');
}

function aplicarPresetHoras(inicio, fin) {
    const horaInicioInput = document.getElementById('fc-hora-inicio');
    const horaFinInput = document.getElementById('fc-hora-fin');
    if (horaInicioInput) horaInicioInput.value = inicio;
    if (horaFinInput) horaFinInput.value = fin;
}

function sincronizarSalonInput(val) {
    const salonCustom = document.getElementById('fc-salon-custom');
    if (salonCustom) salonCustom.value = val;
}

function obtenerCsrfInfo() {
    const metaToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const metaHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-XSRF-TOKEN';
    const formInput = document.getElementById('fc-csrf-input')?.value;
    
    // Cookie fallback
    let cookieVal = null;
    const cookieMatch = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
    if (cookieMatch) {
        cookieVal = decodeURIComponent(cookieMatch[1]);
    }
    
    const token = metaToken || formInput || cookieVal || '';
    return { token, header: metaHeader };
}

async function guardarClaseItem(event) {
    event.preventDefault();

    const cursoId = document.getElementById('h-curso-id')?.value;
    const horarioId = document.getElementById('fc-horario-id')?.value;
    const dia = document.getElementById('fc-dia')?.value;
    const horaInicio = document.getElementById('fc-hora-inicio')?.value;
    const horaFin = document.getElementById('fc-hora-fin')?.value;
    const materiaId = document.getElementById('fc-materia')?.value;
    const docenteId = document.getElementById('fc-docente')?.value;
    const salonCustom = document.getElementById('fc-salon-custom')?.value;
    const salonSelect = document.getElementById('fc-salon')?.value;
    const salon = (salonCustom && salonCustom.trim()) ? salonCustom.trim() : (salonSelect || 'Aula 101');

    if (!cursoId) {
        alert('Error: No se ha identificado el curso.');
        return;
    }
    if (!materiaId) {
        alert('Por favor seleccione una materia.');
        return;
    }
    if (!horaInicio || !horaFin) {
        alert('Por favor especifique la hora de inicio y de fin.');
        return;
    }
    if (horaInicio >= horaFin) {
        alert('La hora de inicio debe ser anterior a la hora de fin.');
        return;
    }

    const csrf = obtenerCsrfInfo();
    const formData = new URLSearchParams();
    formData.append('cursoId', cursoId);
    formData.append('diaSemana', dia);
    if (horarioId) formData.append('horarioId', horarioId);
    formData.append('materiaId', materiaId);
    if (docenteId) formData.append('docenteId', docenteId);
    formData.append('horaInicio', horaInicio);
    formData.append('horaFin', horaFin);
    formData.append('salon', salon);
    if (csrf.token) {
        formData.append('_csrf', csrf.token);
    }

    const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
    if (csrf.token) {
        headers[csrf.header] = csrf.token;
    }

    const submitBtn = document.getElementById('fc-btn-submit');
    if (submitBtn) submitBtn.disabled = true;

    try {
        const resp = await fetch('/clases/horarios/guardar-item', {
            method: 'POST',
            headers: headers,
            body: formData.toString()
        });

        const data = await resp.json();
        if (!data.success) {
            throw new Error(data.error || 'Error al guardar la clase');
        }

        // Recargar datos actualizados
        const refreshResp = await fetch('/clases/horarios/datos?cursoId=' + encodeURIComponent(cursoId));
        if (refreshResp.ok) {
            window.horariosActuales = await refreshResp.json();
        }

        cerrarModalFormClase();
        renderHorarioCompleto();

        // Actualizar botón de horario en la tabla principal si existe
        const tableBtn = document.querySelector(`button[data-id="${cursoId}"]`);
        if (tableBtn) {
            tableBtn.className = "px-2.5 py-1 rounded-full text-[10px] font-black uppercase tracking-wide leading-none transition-all inline-flex items-center gap-1.5 cursor-pointer shadow-2xs bg-pill-green text-sidebar border border-[#8ce383] hover:brightness-95";
            tableBtn.querySelector('span').innerText = 'Asignado';
        }
    } catch (err) {
        alert('Error al guardar la clase: ' + err.message);
    } finally {
        if (submitBtn) submitBtn.disabled = false;
    }
}

async function eliminarClaseItem(horarioId) {
    if (!confirm('¿Está seguro de eliminar esta clase del horario?')) return;

    const cursoId = document.getElementById('h-curso-id')?.value;
    const csrf = obtenerCsrfInfo();
    const formData = new URLSearchParams();
    formData.append('horarioId', horarioId);
    if (csrf.token) {
        formData.append('_csrf', csrf.token);
    }

    const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
    if (csrf.token) {
        headers[csrf.header] = csrf.token;
    }

    try {
        const resp = await fetch('/clases/horarios/eliminar-item', {
            method: 'POST',
            headers: headers,
            body: formData.toString()
        });
        const data = await resp.json();
        if (!data.success) {
            throw new Error(data.error || 'Error al eliminar');
        }

        window.horariosActuales = window.horariosActuales.filter(h => String(h.id) !== String(horarioId));
        renderHorarioCompleto();

        if (cursoId && window.horariosActuales.length === 0) {
            const tableBtn = document.querySelector(`button[data-id="${cursoId}"]`);
            if (tableBtn) {
                tableBtn.className = "px-2.5 py-1 rounded-full text-[10px] font-black uppercase tracking-wide leading-none transition-all inline-flex items-center gap-1.5 cursor-pointer shadow-2xs bg-amber-100 text-amber-800 border border-amber-300 hover:bg-amber-200";
                tableBtn.querySelector('span').innerText = 'Sin Horario';
            }
        }
    } catch (err) {
        alert('Error al eliminar la clase: ' + err.message);
    }
}

async function confirmarLimpiarHorario() {
    const cursoId = document.getElementById('h-curso-id')?.value;
    if (!cursoId) return;

    if (!confirm('¿Está seguro de que desea eliminar todas las clases asignadas a este curso? Esta acción no se puede deshacer.')) {
        return;
    }

    const csrf = obtenerCsrfInfo();
    const formData = new URLSearchParams();
    formData.append('cursoId', cursoId);
    if (csrf.token) {
        formData.append('_csrf', csrf.token);
    }

    const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
    if (csrf.token) {
        headers[csrf.header] = csrf.token;
    }

    try {
        const resp = await fetch('/clases/horarios/limpiar', {
            method: 'POST',
            headers: headers,
            body: formData.toString()
        });
        const data = await resp.json();
        if (!data.success) throw new Error(data.error || 'Error al limpiar horario');

        window.horariosActuales = [];
        renderHorarioCompleto();

        const tableBtn = document.querySelector(`button[data-id="${cursoId}"]`);
        if (tableBtn) {
            tableBtn.className = "px-2.5 py-1 rounded-full text-[10px] font-black uppercase tracking-wide leading-none transition-all inline-flex items-center gap-1.5 cursor-pointer shadow-2xs bg-amber-100 text-amber-800 border border-amber-300 hover:bg-amber-200";
            tableBtn.querySelector('span').innerText = 'Sin Horario';
        }
    } catch (err) {
        alert('Error al limpiar el horario: ' + err.message);
    }
}

function formatearHora12(horaStr) {
    if (!horaStr) return '07:00';
    try {
        const [h, m] = horaStr.split(':').map(Number);
        const ampm = h >= 12 ? 'PM' : 'AM';
        const h12 = h % 12 || 12;
        return `${String(h12).padStart(2, '0')}:${String(m).padStart(2, '0')} ${ampm}`;
    } catch (e) {
        return horaStr;
    }
}

// -------------------------------------------------------------------------
// MODAL DE GESTIÓN Y CURSOS
// -------------------------------------------------------------------------
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
    }, 250);
}
