// Toggle sidebar
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');

    sidebar.classList.toggle('active');
    overlay.classList.toggle('active');
}

// Configuración de la API
const API_URL = 'http://localhost:8080/api';
const API_ESTUDIANTES_URL = `${API_URL}/estudiante`;
const API_CALIFICACIONES_URL = `${API_URL}/calificaciones`;
const API_LOGROS_URL = `${API_URL}/logros`;
const API_PERIODOS_URL = `${API_URL}/periodos`;

// Estado global
let estudianteActual = null;
let calificacionSeleccionada = null;
let timeoutBusqueda = null;
let calificacionesCargadas = [];
let periodoSeleccionadoFiltro = null;

// Mapeo de categorías
const CATEGORIAS_NOMBRES = {
    'psicosociales': 'Psicosociales',
    'academicos': 'Académicos',
    'deportivos': 'Deportivos',
    'artisticos': 'Artísticos',
    'culturales': 'Culturales'
};

// Inicialización
document.addEventListener('DOMContentLoaded', function () {
    inicializarEventos();
    cargarPeriodos();
    verificarRolUsuario();
});

// Verificar rol de usuario y ajustar interfaz
async function verificarRolUsuario() {
    const usuario = localStorage.getItem('nombreUsuario');
    const rolUsuario = localStorage.getItem('rolUsuario');

    if (rolUsuario === 'Acudiente') {
        console.log('Usuario es Acudiente, cargando estudiantes asignados');
        await cargarEstudiantesAcudiente();
    } else {
        console.log('Usuario es Administrador o Profesor, cargando todos los estudiantes');
        // Para otros roles, mostrar la interfaz de búsqueda normal
        document.querySelector('.search-section').style.display = 'block';
        document.getElementById('empty-search-state').style.display = 'block';
    }
}

// Cargar estudiantes asignados al Acudiente
async function cargarEstudiantesAcudiente() {
    try {
        const nombreUsuario = localStorage.getItem('nombreUsuario');

        if (!nombreUsuario) {
            mostrarMensaje('Error', 'No se pudo identificar el usuario', 'error');
            return;
        }

        const response = await fetch(`${API_URL}/estudiante/mis-estudiantes/${nombreUsuario}`);

        if (!response.ok) {
            throw new Error('Error al cargar estudiantes');
        }

        const estudiantes = await response.json();

        if (estudiantes.length === 0) {
            mostrarMensaje('Información', 'No tiene estudiantes asignados', 'info');
            return;
        }

        // Ocultar búsqueda y mostrar selector
        document.querySelector('.search-section').style.display = 'none';
        document.getElementById('acudiente-selector-section').style.display = 'block';

        // Llenar el selector con los estudiantes
        const select = document.getElementById('select-estudiante-acudiente');
        select.innerHTML = '<option value="">Seleccione un estudiante...</option>';

        estudiantes.forEach(estudiante => {
            const option = document.createElement('option');
            option.value = estudiante.codigoEstudiante;
            option.textContent = `${estudiante.persona.nombre} ${estudiante.persona.apellido}`;
            option.dataset.estudiante = JSON.stringify(estudiante);
            select.appendChild(option);
        });

        // Auto-cargar el primer estudiante
        if (estudiantes.length > 0) {
            select.value = estudiantes[0].codigoEstudiante;
            await cargarCalificaciones(estudiantes[0]);
        }

    } catch (error) {
        console.error('Error al cargar estudiantes del acudiente:', error);
        mostrarMensaje('Error', 'Error al cargar estudiantes asignados', 'error');
    }
}

// Manejar selección de estudiante del selector de Acudiente
async function seleccionarEstudianteAcudiente() {
    const select = document.getElementById('select-estudiante-acudiente');
    const selectedOption = select.options[select.selectedIndex];

    if (!selectedOption || !selectedOption.value) {
        return;
    }

    const estudiante = JSON.parse(selectedOption.dataset.estudiante);
    await cargarCalificaciones(estudiante);
}

// Inicializar eventos
function inicializarEventos() {
    // Búsqueda con debounce
    document.getElementById('txt-buscar-estudiante').addEventListener('input', function (e) {
        clearTimeout(timeoutBusqueda);
        const query = e.target.value.trim();

        if (query.length >= 2) {
            timeoutBusqueda = setTimeout(() => {
                buscarEstudiante();
            }, 500);
        } else if (query.length === 0) {
            mostrarEstadoVacio();
        }
    });

    // Enter para buscar
    document.getElementById('txt-buscar-estudiante').addEventListener('keypress', function (e) {
        if (e.key === 'Enter') {
            buscarEstudiante();
        }
    });

    // Botones principales
    document.getElementById('btn-asignar-logro').addEventListener('click', abrirModalAsignarLogro);
    document.getElementById('btn-consultar-historial').addEventListener('click', consultarHistorial);
    document.getElementById('btn-generar-boletin').addEventListener('click', generarBoletin);

    // Botón confirmar asignar
    document.getElementById('btn-confirmar-asignar').addEventListener('click', confirmarAsignarLogro);

    // Botón confirmar modificar
    document.getElementById('btn-confirmar-modificar').addEventListener('click', confirmarModificarLogro);

    // Botón confirmar eliminar
    document.getElementById('btn-confirmar-eliminar-calificacion').addEventListener('click', confirmarEliminarCalificacion);

    // Selector de categoría en modal
    document.getElementById('select-categoria-logro').addEventListener('change', cargarLogrosPorCategoria);
}

// ========== CARGAR PERÍODOS ==========

async function cargarPeriodos() {
    try {
        const response = await fetch(`${API_PERIODOS_URL}`);

        if (!response.ok) {
            console.warn('No se pudieron cargar los períodos');
            return;
        }

        const periodos = await response.json();

        // Llenar selector de filtro
        const selectFiltro = document.getElementById('select-periodo-filtro');
        selectFiltro.innerHTML = '<option value="">Todos los períodos</option>';

        periodos.forEach(periodo => {
            const option = document.createElement('option');
            option.value = periodo.idPeriodo;
            option.textContent = periodo.nombrePeriodo;
            selectFiltro.appendChild(option);
        });

        // Llenar selector de asignación
        const selectAsignacion = document.getElementById('select-periodo-asignar');
        selectAsignacion.innerHTML = '<option value="">Seleccione un período</option>';

        periodos.forEach(periodo => {
            const option = document.createElement('option');
            option.value = periodo.idPeriodo;
            option.textContent = periodo.nombrePeriodo;
            selectAsignacion.appendChild(option);
        });

    } catch (error) {
        console.error('Error al cargar períodos:', error);
    }
}

// ========== BUSCAR ESTUDIANTE ==========

async function buscarEstudiante() {
    const query = document.getElementById('txt-buscar-estudiante').value.trim();

    if (!query) {
        mostrarMensaje('Advertencia', 'Por favor ingrese un nombre o documento para buscar', 'warning');
        return;
    }

    mostrarLoading();

    try {
        const response = await fetch(`${API_ESTUDIANTES_URL}/buscar?textoBusqueda=${encodeURIComponent(query)}`);

        if (!response.ok) {
            if (response.status === 404) {
                mostrarSinResultados();
                return;
            }
            throw new Error('Error al buscar estudiante');
        }

        const estudiantes = await response.json();

        if (estudiantes.length === 0) {
            mostrarSinResultados();
        } else if (estudiantes.length === 1) {
            await cargarCalificaciones(estudiantes[0]);
        } else {
            // Si hay múltiples resultados, cargar el primero
            await cargarCalificaciones(estudiantes[0]);
        }

    } catch (error) {
        console.error('Error al buscar estudiante:', error);
        mostrarMensaje('Error', 'Error al buscar el estudiante', 'error');
        mostrarEstadoVacio();
    }
}

// ========== CARGAR CALIFICACIONES ==========

async function cargarCalificaciones(estudiante) {
    estudianteActual = estudiante;

    try {
        const response = await fetch(`${API_CALIFICACIONES_URL}/estudiante/${estudiante.codigoEstudiante}`);

        let calificaciones = [];
        if (response.ok) {
            calificaciones = await response.json();
        }

        calificacionesCargadas = calificaciones;
        mostrarCalificaciones(estudiante, calificaciones);

    } catch (error) {
        console.error('Error al cargar calificaciones:', error);
        mostrarCalificaciones(estudiante, []);
    }
}

// ========== MOSTRAR CALIFICACIONES ==========

function mostrarCalificaciones(estudiante, calificaciones) {
    ocultarTodosLosEstados();

    const calificacionesSection = document.getElementById('calificaciones-section');
    const nombreEstudiante = document.getElementById('nombre-estudiante');
    const totalLogros = document.getElementById('total-logros');
    const tbody = document.getElementById('tbody-calificaciones');
    const emptyCalificacionesState = document.getElementById('empty-calificaciones-state');
    const tablaCalificaciones = document.getElementById('tabla-calificaciones');

    // Actualizar nombre del estudiante
    nombreEstudiante.textContent = `${estudiante.persona.nombre} ${estudiante.persona.apellido}`;
    totalLogros.textContent = calificaciones.length;

    // Verificar si el usuario es Acudiente
    const rolUsuario = localStorage.getItem('rolUsuario');
    const esAcudiente = rolUsuario === 'Acudiente';

    // Ocultar/mostrar botones de acción según el rol
    if (esAcudiente) {
        // Ocultar botones de acción para Acudiente
        document.getElementById('btn-asignar-logro').style.display = 'none';
        document.getElementById('btn-consultar-historial').style.display = 'none';
        document.getElementById('btn-generar-boletin').style.display = 'none';
    } else {
        // Mostrar botones para otros roles
        document.getElementById('btn-asignar-logro').style.display = 'inline-block';
        document.getElementById('btn-consultar-historial').style.display = 'inline-block';
        document.getElementById('btn-generar-boletin').style.display = 'inline-block';
    }

    // Limpiar contenedor
    tbody.innerHTML = '';

    if (calificaciones.length === 0) {
        tablaCalificaciones.style.display = 'none';
        emptyCalificacionesState.style.display = 'block';
    } else {
        tablaCalificaciones.style.display = 'table';
        emptyCalificacionesState.style.display = 'none';

        calificaciones.forEach(calificacion => {
            const tr = document.createElement('tr');

            const categoriaClass = `categoria-${calificacion.logro.categoria.toLowerCase()}`;
            const fechaAsignacion = new Date(calificacion.fechaAsignacion).toLocaleDateString('es-ES');

            // Construir HTML de la fila
            let accionesHTML = '';
            if (!esAcudiente) {
                // Solo mostrar acciones si NO es Acudiente
                accionesHTML = `
                    <td>
                        <div class="actions-cell">
                            <button class="btn btn-secondary btn-icon" 
                                    onclick="editarCalificacion(${calificacion.idCalificacion})" 
                                    title="Editar">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-danger btn-icon" 
                                    onclick="abrirDialogoEliminar(${calificacion.idCalificacion}, '${calificacion.logro.nombreLogro.replace(/'/g, "\\'")}')" 
                                    title="Eliminar">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </td>
                `;
            } else {
                // Para Acudiente, mostrar celda vacía o un guión
                accionesHTML = '<td style="text-align: center;">-</td>';
            }

            tr.innerHTML = `
                <td>
                    <span class="periodo-badge">
                        ${calificacion.periodo.nombrePeriodo}
                    </span>
                </td>
                <td>
                    <span class="categoria-badge ${categoriaClass}">
                        ${CATEGORIAS_NOMBRES[calificacion.logro.categoria] || calificacion.logro.categoria}
                    </span>
                </td>
                <td>${calificacion.logro.nombreLogro}</td>
                <td>${fechaAsignacion}</td>
                ${accionesHTML}
            `;

            tbody.appendChild(tr);
        });
    }

    calificacionesSection.style.display = 'block';
}

// ========== APLICAR FILTRO DE PERÍODO ==========

function aplicarFiltrosPeriodo() {
    const idPeriodo = document.getElementById('select-periodo-filtro').value;
    periodoSeleccionadoFiltro = idPeriodo ? parseInt(idPeriodo) : null;

    let calificacionesFiltradas = calificacionesCargadas;

    if (periodoSeleccionadoFiltro) {
        calificacionesFiltradas = calificacionesCargadas.filter(c => c.periodo.idPeriodo === periodoSeleccionadoFiltro);
    }

    mostrarCalificaciones(estudianteActual, calificacionesFiltradas);
}

// ========== ASIGNAR LOGRO ==========

function abrirModalAsignarLogro() {
    if (!estudianteActual) {
        mostrarMensaje('Advertencia', 'No hay ningún estudiante seleccionado', 'warning');
        return;
    }

    // Limpiar formulario
    document.getElementById('form-asignar-logro').reset();
    document.getElementById('select-logro').disabled = true;
    document.getElementById('select-logro').innerHTML = '<option value="">Primero seleccione una categoría</option>';

    abrirModal('modal-asignar-logro');
}

async function cargarLogrosPorCategoria() {
    const categoria = document.getElementById('select-categoria-logro').value;
    const selectLogro = document.getElementById('select-logro');

    if (!categoria) {
        selectLogro.disabled = true;
        selectLogro.innerHTML = '<option value="">Primero seleccione una categoría</option>';
        return;
    }

    try {
        const response = await fetch(`${API_LOGROS_URL}/categoria/${categoria}`);

        if (!response.ok) {
            selectLogro.disabled = true;
            selectLogro.innerHTML = '<option value="">No hay logros en esta categoría</option>';
            return;
        }

        const logros = await response.json();

        selectLogro.innerHTML = '<option value="">Seleccione un logro</option>';

        logros.forEach(logro => {
            const option = document.createElement('option');
            option.value = logro.idLogro;
            option.textContent = logro.nombreLogro;
            selectLogro.appendChild(option);
        });

        selectLogro.disabled = false;

    } catch (error) {
        console.error('Error al cargar logros:', error);
        selectLogro.disabled = true;
        selectLogro.innerHTML = '<option value="">Error al cargar logros</option>';
    }
}

async function confirmarAsignarLogro() {
    const idPeriodo = document.getElementById('select-periodo-asignar').value;
    const idLogro = document.getElementById('select-logro').value;

    if (!idPeriodo) {
        mostrarMensaje('Advertencia', 'Por favor seleccione un período', 'warning');
        return;
    }

    if (!idLogro) {
        mostrarMensaje('Advertencia', 'Por favor seleccione un logro', 'warning');
        return;
    }

    try {
        const response = await fetch(`${API_CALIFICACIONES_URL}/asignar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                codigoEstudiante: estudianteActual.codigoEstudiante,
                idLogro: parseInt(idLogro),
                idPeriodo: parseInt(idPeriodo),
                nombreUsuario: localStorage.getItem('nombreUsuario')
            })
        });

        if (response.ok) {
            cerrarModal('modal-asignar-logro');
            mostrarMensaje('Éxito', 'Logro asignado exitosamente', 'success');
            await cargarCalificaciones(estudianteActual);
        } else {
            const errorData = await response.json();
            mostrarMensaje('Error', errorData.mensaje || 'Error al asignar el logro', 'error');
        }

    } catch (error) {
        console.error('Error al asignar logro:', error);
        mostrarMensaje('Error', 'Error al comunicarse con el servidor', 'error');
    }
}

// ========== EDITAR CALIFICACIÓN ==========

async function editarCalificacion(idCalificacion) {
    try {
        const response = await fetch(`${API_CALIFICACIONES_URL}/estudiante/${estudianteActual.codigoEstudiante}`);

        if (!response.ok) {
            throw new Error('Error al cargar calificación');
        }

        const calificaciones = await response.json();
        const calificacion = calificaciones.find(c => c.idCalificacion === idCalificacion);

        if (!calificacion) {
            throw new Error('Calificación no encontrada');
        }

        calificacionSeleccionada = calificacion;

        // Mostrar información actual
        document.getElementById('modificar-periodo').value = calificacion.periodo.nombrePeriodo;
        document.getElementById('modificar-categoria-actual').value =
            CATEGORIAS_NOMBRES[calificacion.logro.categoria] || calificacion.logro.categoria;
        document.getElementById('modificar-logro-actual').value = calificacion.logro.nombreLogro;

        // Limpiar selectores para nueva selección
        document.getElementById('modificar-nueva-categoria').value = '';
        document.getElementById('modificar-nuevo-logro').disabled = true;
        document.getElementById('modificar-nuevo-logro').innerHTML = '<option value="">Seleccione una categoría</option>';

        abrirModal('modal-modificar-asignacion');

    } catch (error) {
        console.error('Error al cargar calificación:', error);
        mostrarMensaje('Error', 'Error al cargar los datos de la calificación', 'error');
    }
}

// ========== CARGAR NUEVOS LOGROS EN MODAL MODIFICAR ==========

async function cargarNuevosLogrosPorCategoria() {
    const categoria = document.getElementById('modificar-nueva-categoria').value;
    const selectLogro = document.getElementById('modificar-nuevo-logro');

    if (!categoria) {
        selectLogro.disabled = true;
        selectLogro.innerHTML = '<option value="">Seleccione una categoría</option>';
        return;
    }

    try {
        const response = await fetch(`${API_LOGROS_URL}/categoria/${categoria}`);

        if (!response.ok) {
            selectLogro.disabled = true;
            selectLogro.innerHTML = '<option value="">No hay logros en esta categoría</option>';
            return;
        }

        const logros = await response.json();

        selectLogro.innerHTML = '<option value="">Seleccione un logro</option>';

        logros.forEach(logro => {
            const option = document.createElement('option');
            option.value = logro.idLogro;
            option.textContent = logro.nombreLogro;
            selectLogro.appendChild(option);
        });

        selectLogro.disabled = false;

    } catch (error) {
        console.error('Error al cargar logros:', error);
        selectLogro.disabled = true;
        selectLogro.innerHTML = '<option value="">Error al cargar logros</option>';
    }
}

// ========== CONFIRMAR MODIFICACIÓN DE LOGRO ==========

async function confirmarModificarLogro() {
    const idNuevoLogro = document.getElementById('modificar-nuevo-logro').value;

    if (!idNuevoLogro) {
        mostrarMensaje('Advertencia', 'Por favor seleccione un nuevo logro', 'warning');
        return;
    }

    if (!calificacionSeleccionada || !calificacionSeleccionada.idCalificacion) {
        mostrarMensaje('Error', 'Error: No se puede identificar la calificación', 'error');
        return;
    }

    try {
        // Primero eliminar la calificación antigua
        const responseDelete = await fetch(`${API_CALIFICACIONES_URL}/${calificacionSeleccionada.idCalificacion}`, {
            method: 'DELETE'
        });

        if (!responseDelete.ok && responseDelete.status !== 204) {
            throw new Error('Error al eliminar el logro anterior');
        }

        // Luego crear la nueva asignación
        const responseCreate = await fetch(`${API_CALIFICACIONES_URL}/asignar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                codigoEstudiante: estudianteActual.codigoEstudiante,
                idLogro: parseInt(idNuevoLogro),
                idPeriodo: calificacionSeleccionada.periodo.idPeriodo,
                nombreUsuario: localStorage.getItem('nombreUsuario')
            })
        });

        if (responseCreate.ok) {
            cerrarModal('modal-modificar-asignacion');
            mostrarMensaje('Éxito', 'Logro modificado exitosamente', 'success');
            calificacionSeleccionada = null;
            await cargarCalificaciones(estudianteActual);
        } else {
            const errorData = await responseCreate.json();
            mostrarMensaje('Error', errorData.mensaje || 'Error al asignar el nuevo logro', 'error');
        }

    } catch (error) {
        console.error('Error al modificar logro:', error);
        mostrarMensaje('Error', 'Error al modificar el logro: ' + error.message, 'error');
    }
}

// ========== ELIMINAR CALIFICACIÓN ==========

function abrirDialogoEliminar(idCalificacion, nombreLogro) {
    calificacionSeleccionada = { idCalificacion: idCalificacion };
    document.getElementById('nombre-logro-eliminar').textContent = nombreLogro;
    abrirModal('modal-confirmar-eliminar');
}

async function confirmarEliminarCalificacion() {
    if (!calificacionSeleccionada) {
        return;
    }

    try {
        const response = await fetch(`${API_CALIFICACIONES_URL}/${calificacionSeleccionada.idCalificacion}`, {
            method: 'DELETE'
        });

        cerrarModal('modal-confirmar-eliminar');

        if (response.ok || response.status === 204) {
            mostrarMensaje('Éxito', 'Logro eliminado exitosamente', 'success');
            calificacionSeleccionada = null;
            await cargarCalificaciones(estudianteActual);
        } else {
            const errorData = await response.json();
            mostrarMensaje('Error', errorData.mensaje || 'Error al eliminar el logro', 'error');
        }

    } catch (error) {
        console.error('Error al eliminar calificación:', error);
        cerrarModal('modal-confirmar-eliminar');
        mostrarMensaje('Error', 'Error al comunicarse con el servidor', 'error');
    }
}

// ========== HISTORIAL Y BOLETÍN ==========

async function consultarHistorial() {
    if (!estudianteActual) {
        mostrarMensaje('Advertencia', 'No hay ningún estudiante seleccionado', 'warning');
        return;
    }

    try {
        const response = await fetch(`${API_CALIFICACIONES_URL}/historial/${estudianteActual.codigoEstudiante}`);

        if (response.ok) {
            const historial = await response.json();
            mostrarReporteHistorial(historial);
        } else {
            throw new Error('Error al consultar historial');
        }

    } catch (error) {
        console.error('Error al consultar historial:', error);
        mostrarMensaje('Error', 'Error al consultar el historial', 'error');
    }
}

function mostrarReporteHistorial(historial) {
    let mensaje = `<strong>Estudiante:</strong> ${historial.estudiante.persona.nombre} ${historial.estudiante.persona.apellido}<br><br>`;
    mensaje += `<strong>Total de logros:</strong> ${historial.totalLogros}<br><br>`;

    if (historial.calificaciones.length > 0) {
        mensaje += '<strong>Logros por período:</strong><br>';

        const porPeriodo = {};
        historial.calificaciones.forEach(cal => {
            const periodo = cal.periodo.nombrePeriodo;
            if (!porPeriodo[periodo]) {
                porPeriodo[periodo] = [];
            }
            porPeriodo[periodo].push(cal.logro.nombreLogro);
        });

        Object.keys(porPeriodo).forEach(periodo => {
            mensaje += `<br><strong>${periodo}:</strong><br>`;
            porPeriodo[periodo].forEach(logro => {
                mensaje += `• ${logro}<br>`;
            });
        });
    }

    mostrarMensajeHTML('Histórico de Logros', mensaje);
}

function mostrarMensajeHTML(titulo, html) {
    const modal = document.getElementById('modal-mensaje');
    const tituloEl = document.getElementById('modal-mensaje-titulo');
    const textoEl = document.getElementById('modal-mensaje-texto');

    tituloEl.textContent = titulo;
    textoEl.innerHTML = html;
    textoEl.style.textAlign = 'left';

    abrirModal('modal-mensaje');
}

// ========== BOLETÍN ==========

/**
 * Abre el modal para seleccionar el periodo del boletín
 */
function generarBoletin() {
    if (!estudianteActual) {
        mostrarMensaje('Advertencia', 'No hay ningún estudiante seleccionado', 'warning');
        return;
    }

    // Llenar el selector de periodos del modal. Usamos los mismos periodos ya cargados
    const selectBoletin = document.getElementById('select-periodo-boletin');
    const selectFiltro = document.getElementById('select-periodo-filtro');

    // Copiar opciones (excepto la primera de "Todos los periodos")
    selectBoletin.innerHTML = '<option value="">Seleccione...</option>';

    Array.from(selectFiltro.options).forEach(opt => {
        if (opt.value) { // Ignorar la opción vacía del filtro
            const newOpt = document.createElement('option');
            newOpt.value = opt.value;
            newOpt.textContent = opt.textContent;
            selectBoletin.appendChild(newOpt);
        }
    });

    abrirModal('modal-seleccionar-periodo-boletin');
}

/**
 * Confirma la selección y llama al backend para generar el boletín
 */
async function confirmarGeneracionBoletin() {
    const idPeriodo = document.getElementById('select-periodo-boletin').value;

    if (!idPeriodo) {
        mostrarMensaje('Advertencia', 'Por favor seleccione un periodo', 'warning');
        return;
    }

    cerrarModal('modal-seleccionar-periodo-boletin');

    try {
        mostrarMensaje('Información', 'Validando y generando boletín...', 'info');

        const response = await fetch(`${API_URL}/boletin/generar/${estudianteActual.codigoEstudiante}/${idPeriodo}`);

        if (response.ok) {
            const pdfBlob = await response.blob();
            const pdfUrl = URL.createObjectURL(pdfBlob);

            // Cerrar mensaje de carga
            cerrarModal('modal-mensaje');

            // Abrir modal de previsualización
            abrirModalPrevisualizacionPDF(pdfUrl, pdfBlob, idPeriodo);

        } else {
            // Manejar errores
            const text = await response.text();
            let mensajeError = 'Error al generar el boletín';
            try {
                const json = JSON.parse(text);
                mensajeError = json.mensaje || json.message || mensajeError;
            } catch (e) {
                if (text && text.length < 200) mensajeError = text;
            }

            // Cerrar mensaje de carga primero
            cerrarModal('modal-mensaje');

            // Mostrar error específico (ej: "No hay calificaciones")
            setTimeout(() => {
                mostrarMensaje('No se pudo generar', mensajeError, 'warning');
            }, 300); // Pequeño delay para que no choque con el cierre del modal anterior
        }
    } catch (error) {
        console.error('Error:', error);
        cerrarModal('modal-mensaje');
        mostrarMensaje('Error', 'Error de conexión al generar el boletín', 'error');
    }
}


/**
 * Abre un modal con previsualización del PDF y opción de descarga
 */
function abrirModalPrevisualizacionPDF(pdfUrl, pdfBlob, idPeriodo) {
    const modalExistente = document.getElementById('modal-previsualizacion-pdf');
    if (modalExistente) {
        modalExistente.remove();
    }

    const modal = document.createElement('div');
    modal.id = 'modal-previsualizacion-pdf';
    modal.className = 'modal show';
    modal.style.display = 'flex';

    // Nombre sugerido para descarga
    const nombreEstudiante = `${estudianteActual.persona.nombre} ${estudianteActual.persona.apellido}`.replace(/ /g, '_');
    const periodoNombre = document.getElementById('select-periodo-filtro').options[document.getElementById('select-periodo-filtro').selectedIndex].text.replace(/ /g, '_');

    modal.dataset.filename = `Boletin_${nombreEstudiante}_${periodoNombre}.pdf`;

    modal.innerHTML = `
        <div class="modal-content modal-pdf">
            <div class="modal-header">
                <h2><i class="fas fa-file-pdf"></i> Previsualización del Boletín</h2>
                <button class="modal-close" onclick="cerrarModalPDF()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="modal-body modal-body-pdf">
                <div class="pdf-controls">
                    <button class="btn btn-success" onclick="descargarPDF()">
                        <i class="fas fa-download"></i> Descargar PDF
                    </button>
                    <button class="btn btn-secondary" onclick="imprimirPDF()">
                        <i class="fas fa-print"></i> Imprimir
                    </button>
                </div>
                <div class="pdf-viewer">
                    <iframe id="pdf-iframe" src="${pdfUrl}" type="application/pdf" width="100%" height="100%">
                        <p>Su navegador no puede mostrar PDFs. 
                           <a href="${pdfUrl}" download>Haga clic aquí para descargar el PDF</a>
                        </p>
                    </iframe>
                </div>
            </div>
        </div>
    `;

    document.body.appendChild(modal);

    window.currentPdfBlob = pdfBlob;
    window.currentPdfUrl = pdfUrl;
}

/**
 * Cierra el modal de previsualización de PDF
 */
function cerrarModalPDF() {
    const modal = document.getElementById('modal-previsualizacion-pdf');
    if (modal) {
        if (window.currentPdfUrl) {
            URL.revokeObjectURL(window.currentPdfUrl);
        }
        modal.remove();
    }
}

/**
 * Descarga el PDF generado
 */
function descargarPDF() {
    if (!window.currentPdfBlob) {
        mostrarMensaje('Error', 'No hay PDF disponible para descargar', 'error');
        return;
    }

    const modal = document.getElementById('modal-previsualizacion-pdf');
    const nombreArchivo = modal.dataset.filename || 'boletin.pdf';

    const link = document.createElement('a');
    link.href = window.currentPdfUrl;
    link.download = nombreArchivo;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    mostrarMensaje('Éxito', 'PDF descargado exitosamente', 'success');
}

/**
 * Imprime el PDF actual
 */
function imprimirPDF() {
    const iframe = document.getElementById('pdf-iframe');
    if (iframe && iframe.contentWindow) {
        iframe.contentWindow.print();
    } else {
        window.open(window.currentPdfUrl, '_blank');
    }
}

// ========== FUNCIONES DE UI ==========

function mostrarEstadoVacio() {
    ocultarTodosLosEstados();
    document.getElementById('empty-search-state').style.display = 'block';
}

function mostrarLoading() {
    ocultarTodosLosEstados();
    document.getElementById('loading-state').style.display = 'block';
}

function mostrarSinResultados() {
    ocultarTodosLosEstados();
    document.getElementById('no-results-state').style.display = 'block';
}

function ocultarTodosLosEstados() {
    document.getElementById('empty-search-state').style.display = 'none';
    document.getElementById('loading-state').style.display = 'none';
    document.getElementById('calificaciones-section').style.display = 'none';
    document.getElementById('no-results-state').style.display = 'none';
}

function abrirModal(idModal) {
    document.getElementById(idModal).classList.add('show');
}

function cerrarModal(idModal) {
    document.getElementById(idModal).classList.remove('show');
}

function mostrarMensaje(titulo, mensaje, tipo) {
    const modal = document.getElementById('modal-mensaje');
    const icono = document.getElementById('modal-mensaje-icono');
    const tituloEl = document.getElementById('modal-mensaje-titulo');
    const textoEl = document.getElementById('modal-mensaje-texto');

    tituloEl.textContent = titulo;
    textoEl.textContent = mensaje;

    icono.className = 'modal-icon';
    if (tipo === 'success') {
        icono.classList.add('success', 'fas', 'fa-check-circle');
    } else if (tipo === 'error') {
        icono.classList.add('error', 'fas', 'fa-exclamation-circle');
    } else if (tipo === 'warning') {
        icono.classList.add('warning', 'fas', 'fa-exclamation-triangle');
    } else {
        icono.classList.add('fas', 'fa-info-circle');
    }

    abrirModal('modal-mensaje');
}

// Cerrar modal al hacer clic fuera
window.onclick = function (event) {
    if (event.target.classList.contains('modal')) {
        event.target.classList.remove('show');
    }
}