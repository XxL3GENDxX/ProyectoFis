// Toggle sidebar
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');

    sidebar.classList.toggle('active');
    overlay.classList.toggle('active');
}

// Configuración de la API
const API_URL = 'http://localhost:8080/api';
const API_GRUPOS_URL = `${API_URL}/grupos`;
const API_GRADOS_URL = `${API_URL}/grados`;
const API_ESTUDIANTE_URL = `${API_URL}/estudiante`;
const API_CITACIONES_URL = `${API_URL}/citaciones`;

// Estado global
let grupoSeleccionado = null;
let modoEdicion = false;
let timeoutBusqueda = null;
let estudiantesDelGrupo = []; // Para almacenar estudiantes del grupo actual

// Inicialización
document.addEventListener('DOMContentLoaded', function () {
    inicializarEventos();
    cargarGradosEnModal();
    crearModalCitaciones();
});

// Inicializar eventos
function inicializarEventos() {
    // Búsqueda con debounce
    document.getElementById('txt-buscar-grupo').addEventListener('input', function (e) {
        clearTimeout(timeoutBusqueda);
        timeoutBusqueda = setTimeout(() => {
            buscarGrupos(e.target.value);
        }, 300);
    });

    // Botones principales
    document.getElementById('btn-crear-grupo').addEventListener('click', abrirModalCrearGrupo);
    document.getElementById('btn-editar-grupo').addEventListener('click', editarGrupo);
    document.getElementById('btn-eliminar-grupo').addEventListener('click', confirmarEliminarGrupo);

    // Botones de la tabla
    document.getElementById('btn-generar-listado').addEventListener('click', generarListado);
    document.getElementById('btn-generar-citaciones').addEventListener('click', abrirModalCitaciones);

    // Botones del modal
    document.getElementById('btn-guardar-grupo').addEventListener('click', guardarGrupo);
    document.getElementById('btn-confirmar-eliminar-grupo').addEventListener('click', eliminarGrupo);

    // Ocultar botón de crear grupo si es Profesor
    if (localStorage.getItem('rolUsuario') === 'Profesor') {
        const btnCrear = document.getElementById('btn-crear-grupo');
        if (btnCrear) btnCrear.style.display = 'none';
    }
}

// Buscar grupos
async function buscarGrupos(query) {
    if (!query || query.trim() === '') {
        mostrarEstadoVacio();
        return;
    }

    mostrarLoading();

    try {
        const queryLower = query.toLowerCase().trim();

        const gradosResponse = await fetch(API_GRADOS_URL);
        if (!gradosResponse.ok) {
            throw new Error('Error al buscar grados');
        }

        const grados = await gradosResponse.json();

        const gradosFiltrados = grados.filter(grado =>
            grado.nombreGrado.toLowerCase().includes(queryLower)
        );

        if (gradosFiltrados.length > 0) {
            if (gradosFiltrados.length === 1) {
                await mostrarGruposPorGrado(gradosFiltrados[0].idGrado, query);
            } else {
                await mostrarMultiplesGrados(gradosFiltrados, query);
            }
        } else {
            const numeroGrupo = parseInt(queryLower);
            if (!isNaN(numeroGrupo)) {
                await buscarPorNumeroGrupo(numeroGrupo, query);
            } else {
                mostrarSinResultados();
            }
        }

    } catch (error) {
        console.error('Error al buscar grupos:', error);
        mostrarMensaje('Error', 'Error al buscar grupos', 'error');
        mostrarEstadoVacio();
    }
}

// Mostrar grupos de un grado específico
async function mostrarGruposPorGrado(idGrado, query) {
    try {
        const response = await fetch(`${API_GRUPOS_URL}/grado/${idGrado}`);

        if (!response.ok) {
            throw new Error('Error al cargar grupos');
        }

        const grupos = await response.json();

        if (grupos.length === 0) {
            mostrarSinResultados();
            return;
        }

        if (grupos.length === 1) {
            await mostrarDetalleGrupo(grupos[0], query);
        } else {
            mostrarListaGrupos(grupos, query);
        }

    } catch (error) {
        console.error('Error:', error);
        mostrarMensaje('Error', 'Error al cargar grupos del grado', 'error');
    }
}

// Cargar grados en el modal
async function cargarGradosEnModal() {
    try {
        const response = await fetch(API_GRADOS_URL);

        if (!response.ok) {
            throw new Error('Error al cargar grados');
        }

        const grados = await response.json();
        const select = document.getElementById('grupo-grado');

        select.innerHTML = '<option value="">Seleccione un grado</option>';

        grados.forEach(grado => {
            const option = document.createElement('option');
            option.value = grado.idGrado;
            option.textContent = grado.nombreGrado;
            select.appendChild(option);
        });

    } catch (error) {
        console.error('Error al cargar grados:', error);
        mostrarMensaje('Error', 'Error al cargar los grados disponibles', 'error');
    }
}

// Buscar por número de grupo
async function buscarPorNumeroGrupo(numeroGrupo, query) {
    try {
        const gradosResponse = await fetch(API_GRADOS_URL);
        const grados = await gradosResponse.json();

        const gruposEncontrados = [];

        for (const grado of grados) {
            const response = await fetch(`${API_GRUPOS_URL}/grado/${grado.idGrado}`);
            if (response.ok) {
                const grupos = await response.json();
                const gruposFiltrados = grupos.filter(g => g.numeroGrupo === numeroGrupo);
                gruposEncontrados.push(...gruposFiltrados);
            }
        }

        if (gruposEncontrados.length === 0) {
            mostrarSinResultados();
        } else if (gruposEncontrados.length === 1) {
            await mostrarDetalleGrupo(gruposEncontrados[0], query);
        } else {
            mostrarListaGrupos(gruposEncontrados, query);
        }

    } catch (error) {
        console.error('Error:', error);
        mostrarSinResultados();
    }
}

// Mostrar lista de grupos como tarjetas
function mostrarListaGrupos(grupos, query) {
    ocultarTodosLosEstados();

    const resultsSection = document.getElementById('results-section');
    const searchQuery = document.getElementById('search-query');
    const groupsListContainer = document.getElementById('groups-list-container');
    const groupsGrid = document.getElementById('groups-grid');

    searchQuery.textContent = query;
    groupsGrid.innerHTML = '';

    document.getElementById('btn-editar-grupo').style.display = 'none';
    document.getElementById('btn-eliminar-grupo').style.display = 'none';
    document.getElementById('students-table-container').style.display = 'none';

    grupos.forEach(grupo => {
        const card = document.createElement('div');
        card.className = 'group-card';
        card.onclick = () => mostrarDetalleGrupo(grupo, query);

        const numEstudiantes = grupo.numeroEstudiantes || 0;
        const porcentajeOcupacion = (numEstudiantes / 10) * 100;

        // Get director name or show default message
        const nombreDirector = grupo.directorGrupo && grupo.directorGrupo.persona
            ? `${grupo.directorGrupo.persona.nombre} ${grupo.directorGrupo.persona.apellido}`
            : 'Sin director asignado';

        card.innerHTML = `
            <div class="group-card-header">
                <div class="group-icon">
                    <i class="fas fa-users"></i>
                </div>
                <div class="group-card-title">
                    <h3>${grupo.grado.nombreGrado} - Grupo ${grupo.numeroGrupo}</h3>
                    <p>${nombreDirector}</p>
                </div>
            </div>
            <div class="group-card-body">
                <div class="group-info-item">
                    <span>Estudiantes:</span>
                    <strong>${numEstudiantes} / 10</strong>
                </div>
                <div class="group-info-item">
                    <span>Capacidad:</span>
                    <strong>${porcentajeOcupacion.toFixed(0)}%</strong>
                </div>
            </div>
        `;

        groupsGrid.appendChild(card);
    });

    groupsListContainer.style.display = 'block';
    resultsSection.style.display = 'block';
}

// Mostrar detalle de un grupo con sus estudiantes
async function mostrarDetalleGrupo(grupo, query) {
    grupoSeleccionado = grupo;
    ocultarTodosLosEstados();

    const resultsSection = document.getElementById('results-section');
    const searchQuery = document.getElementById('search-query');
    const studentsTableContainer = document.getElementById('students-table-container');
    const tbody = document.getElementById('tbody-estudiantes-grupo');
    const emptyStudentsState = document.getElementById('empty-students-state');

    searchQuery.textContent = `${grupo.grado.nombreGrado} - Grupo ${grupo.numeroGrupo}`;

    const btnEditar = document.getElementById('btn-editar-grupo');
    const btnEliminar = document.getElementById('btn-eliminar-grupo');

    if (localStorage.getItem('rolUsuario') === 'Profesor') {
        btnEditar.style.display = 'none';
        btnEliminar.style.display = 'none';
    } else {
        btnEditar.style.display = 'inline-flex';
        btnEliminar.style.display = 'inline-flex';
    }

    document.getElementById('groups-list-container').style.display = 'none';

    try {
        const response = await fetch(`${API_ESTUDIANTE_URL}/grupo/${grupo.idGrupo}`);

        let estudiantes = [];

        if (response.ok) {
            estudiantes = await response.json();
        }

        // Guardar estudiantes para usar en citaciones
        estudiantesDelGrupo = estudiantes;

        tbody.innerHTML = '';

        if (estudiantes.length === 0) {
            emptyStudentsState.style.display = 'block';
            document.getElementById('tabla-estudiantes-grupo').style.display = 'none';
        } else {
            emptyStudentsState.style.display = 'none';
            document.getElementById('tabla-estudiantes-grupo').style.display = 'table';

            estudiantes.forEach(estudiante => {
                const tr = document.createElement('tr');
                const estado = estudiante.estado || 'Pendiente';

                const estadoClass = estado === 'Activo' ? 'estado-activo' :
                    estado === 'Inactivo' ? 'estado-inactivo' :
                        'estado-pendiente';

                const nombre = estudiante.persona ? estudiante.persona.nombre : 'Sin Nombre';
                const apellido = estudiante.persona ? estudiante.persona.apellido : 'Sin Apellido';

                tr.innerHTML = `
                    <td>${nombre}</td>
                    <td>${apellido}</td>
                    <td><span class="estado-badge ${estadoClass}">${estado}</span></td>
                `;

                tbody.appendChild(tr);
            });
        }

        studentsTableContainer.style.display = 'block';
        resultsSection.style.display = 'block';

    } catch (error) {
        console.error('Error al cargar estudiantes:', error);
        mostrarMensaje('Error', 'Error al cargar los estudiantes del grupo', 'error');
    }
}

// Abrir modal para crear grupo
function abrirModalCrearGrupo() {
    modoEdicion = false;
    grupoSeleccionado = null;

    document.getElementById('modal-grupo-titulo').innerHTML =
        '<i class="fas fa-plus-circle"></i> Crear Nuevo Grupo';

    document.getElementById('form-grupo').reset();

    abrirModal('modal-grupo');
}

// Editar grupo seleccionado
function editarGrupo() {
    if (!grupoSeleccionado) {
        mostrarMensaje('Advertencia', 'No hay ningún grupo seleccionado', 'warning');
        return;
    }

    modoEdicion = true;

    document.getElementById('modal-grupo-titulo').innerHTML =
        '<i class="fas fa-edit"></i> Editar Grupo';

    document.getElementById('grupo-grado').value = grupoSeleccionado.grado.idGrado;
    document.getElementById('grupo-numero').value = grupoSeleccionado.numeroGrupo;
    // Show director's document or empty if no director
    const documentoDirector = grupoSeleccionado.directorGrupo && grupoSeleccionado.directorGrupo.persona
        ? grupoSeleccionado.directorGrupo.persona.documento : '';
    document.getElementById('grupo-director').value = documentoDirector;

    abrirModal('modal-grupo');
}

// Guardar grupo (crear o editar)
async function guardarGrupo() {
    const idGrado = document.getElementById('grupo-grado').value;
    const numeroGrupo = document.getElementById('grupo-numero').value;
    const documentoDirector = document.getElementById('grupo-director').value.trim();

    if (!idGrado || !numeroGrupo) {
        mostrarMensaje('Advertencia', 'Por favor complete todos los campos obligatorios', 'warning');
        return;
    }

    const grupoData = {
        grado: {
            idGrado: parseInt(idGrado)
        },
        numeroGrupo: parseInt(numeroGrupo),
        documentoDirector: documentoDirector || null
    };

    try {
        let response;

        if (modoEdicion && grupoSeleccionado) {
            response = await fetch(`${API_GRUPOS_URL}/actualizar/${grupoSeleccionado.idGrupo}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(grupoData)
            });
        } else {
            response = await fetch(`${API_GRUPOS_URL}/crear`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(grupoData)
            });
        }

        if (response.ok) {
            cerrarModal('modal-grupo');
            mostrarMensaje('Éxito',
                modoEdicion ? 'Grupo actualizado exitosamente' : 'Grupo creado exitosamente',
                'success');

            const queryActual = document.getElementById('txt-buscar-grupo').value;
            if (queryActual) {
                buscarGrupos(queryActual);
            }
        } else {
            const errorData = await response.json();
            mostrarMensaje('Error', errorData.mensaje || 'Error al guardar el grupo', 'error');
        }

    } catch (error) {
        console.error('Error al guardar grupo:', error);
        cerrarModal('modal-grupo');
        mostrarMensaje('Error', 'Error al comunicarse con el servidor', 'error');
    }
}

// Confirmar eliminación de grupo
function confirmarEliminarGrupo() {
    if (!grupoSeleccionado) {
        mostrarMensaje('Advertencia', 'No hay ningún grupo seleccionado', 'warning');
        return;
    }

    const nombreGrupo = `${grupoSeleccionado.grado.nombreGrado} - Grupo ${grupoSeleccionado.numeroGrupo}`;
    document.getElementById('nombre-grupo-eliminar').textContent = nombreGrupo;
    abrirModal('modal-confirmar-eliminar');
}

// Eliminar grupo
async function eliminarGrupo() {
    if (!grupoSeleccionado) {
        return;
    }

    try {
        const response = await fetch(`${API_GRUPOS_URL}/eliminar/${grupoSeleccionado.idGrupo}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        cerrarModal('modal-confirmar-eliminar');

        if (response.ok) {
            mostrarMensaje('Éxito', 'Grupo eliminado exitosamente', 'success');
            grupoSeleccionado = null;
            mostrarEstadoVacio();
            document.getElementById('txt-buscar-grupo').value = '';
        } else {
            const errorData = await response.json();
            mostrarMensaje('Error', errorData.mensaje || 'Error al eliminar el grupo', 'error');
        }

    } catch (error) {
        console.error('Error al eliminar grupo:', error);
        cerrarModal('modal-confirmar-eliminar');
        mostrarMensaje('Error', 'Error al comunicarse con el servidor', 'error');
    }
}

// Generar listado (exportar a PDF)
async function generarListado() {
    if (!grupoSeleccionado) {
        mostrarMensaje('Advertencia', 'No hay ningún grupo seleccionado', 'warning');
        return;
    }

    try {
        mostrarMensaje('Información', 'Generando PDF...', 'info');

        const response = await fetch(`${API_GRUPOS_URL}/${grupoSeleccionado.idGrupo}/generar-listado-pdf`);

        if (!response.ok) {
            throw new Error('Error al generar el PDF');
        }

        const pdfBlob = await response.blob();
        const pdfUrl = URL.createObjectURL(pdfBlob);

        abrirModalPrevisualizacionPDF(pdfUrl, pdfBlob);

    } catch (error) {
        console.error('Error al generar listado:', error);
        mostrarMensaje('Error', 'Error al generar el listado PDF', 'error');
    }
}

// ==================== FUNCIONES DE CITACIONES ====================

/**
 * Crear el modal de citaciones dinámicamente
 */
function crearModalCitaciones() {
    const modalHTML = `
        <div class="modal" id="modal-citaciones">
            <div class="modal-content">
                <div class="modal-header">
                    <h2><i class="fas fa-envelope"></i> Generar Citaciones</h2>
                </div>
                <div class="modal-body">
                    <div class="citacion-info">
                        <p><strong>Grupo:</strong> <span id="citacion-grupo-nombre"></span></p>
                        <p><strong>Total estudiantes:</strong> <span id="citacion-total-estudiantes"></span></p>
                    </div>

                    <div class="form-group">
                        <label for="citacion-fecha">
                            Fecha y hora de la citación <span class="campo-obligatorio">*</span>
                        </label>
                        <input type="datetime-local" id="citacion-fecha" class="form-input" required>
                    </div>

                    <div class="form-group">
                        <label>
                            <input type="checkbox" id="citacion-seleccionar-todos" onchange="toggleSeleccionarTodos()">
                            Seleccionar todos los estudiantes
                        </label>
                    </div>

                    <div class="estudiantes-lista" id="estudiantes-citacion-lista">
                        <!-- Los estudiantes se cargarán dinámicamente -->
                    </div>

                    <div class="form-note">
                        <i class="fas fa-info-circle"></i>
                        Seleccione los estudiantes para los cuales desea generar citaciones.
                        Se enviará una citación al acudiente de cada estudiante seleccionado.
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-secondary" onclick="cerrarModal('modal-citaciones')">
                        <i class="fas fa-times"></i> Cancelar
                    </button>
                    <button class="btn btn-primary" id="btn-confirmar-citaciones">
                        <i class="fas fa-paper-plane"></i> Generar Citaciones
                    </button>
                </div>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHTML);

    // Agregar evento al botón de confirmar
    document.getElementById('btn-confirmar-citaciones').addEventListener('click', generarCitaciones);
}

/**
 * Abrir modal de citaciones y cargar estudiantes
 */
function abrirModalCitaciones() {
    if (!grupoSeleccionado) {
        mostrarMensaje('Advertencia', 'No hay ningún grupo seleccionado', 'warning');
        return;
    }

    if (estudiantesDelGrupo.length === 0) {
        mostrarMensaje('Advertencia', 'Este grupo no tiene estudiantes asignados', 'warning');
        return;
    }

    // Establecer información del grupo
    document.getElementById('citacion-grupo-nombre').textContent =
        `${grupoSeleccionado.grado.nombreGrado} - Grupo ${grupoSeleccionado.numeroGrupo}`;
    document.getElementById('citacion-total-estudiantes').textContent = estudiantesDelGrupo.length;

    // Establecer fecha mínima (hoy) y sugerida (mañana a las 14:00)
    const ahora = new Date();
    const manana = new Date(ahora);
    manana.setDate(manana.getDate() + 1);
    manana.setHours(14, 0, 0, 0);

    const fechaInput = document.getElementById('citacion-fecha');
    fechaInput.min = ahora.toISOString().slice(0, 16);
    fechaInput.value = manana.toISOString().slice(0, 16);

    // Cargar lista de estudiantes
    cargarListaEstudiantesCitacion();

    // Abrir modal
    abrirModal('modal-citaciones');
}

/**
 * Cargar la lista de estudiantes para selección
 */
function cargarListaEstudiantesCitacion() {
    const lista = document.getElementById('estudiantes-citacion-lista');
    lista.innerHTML = '';

    estudiantesDelGrupo.forEach((estudiante, index) => {
        const nombre = estudiante.persona ? estudiante.persona.nombre : 'Sin Nombre';
        const apellido = estudiante.persona ? estudiante.persona.apellido : 'Sin Apellido';
        const tieneAcudiente = estudiante.acudiente && estudiante.acudiente.idAcudiente;

        const div = document.createElement('div');
        div.className = 'estudiante-item';

        if (!tieneAcudiente) {
            div.innerHTML = `
                <label class="estudiante-label disabled" title="Este estudiante no tiene acudiente asignado">
                    <input type="checkbox" disabled>
                    <span class="estudiante-nombre">${nombre} ${apellido}</span>
                    <span class="estudiante-warning">
                        <i class="fas fa-exclamation-triangle"></i> Sin acudiente
                    </span>
                </label>
            `;
        } else {
            div.innerHTML = `
                <label class="estudiante-label">
                    <input type="checkbox" class="checkbox-estudiante" 
                           data-codigo="${estudiante.codigoEstudiante}"
                           data-acudiente="${estudiante.acudiente.idAcudiente}">
                    <span class="estudiante-nombre">${nombre} ${apellido}</span>
                    <span class="acudiente-info">
                        Acudiente: ${estudiante.acudiente.persona ?
                    estudiante.acudiente.persona.nombre + ' ' + estudiante.acudiente.persona.apellido :
                    'Sin información'}
                    </span>
                </label>
            `;
        }

        lista.appendChild(div);
    });
}

/**
 * Seleccionar/deseleccionar todos los estudiantes
 */
function toggleSeleccionarTodos() {
    const checkboxPrincipal = document.getElementById('citacion-seleccionar-todos');
    const checkboxes = document.querySelectorAll('.checkbox-estudiante');

    checkboxes.forEach(checkbox => {
        checkbox.checked = checkboxPrincipal.checked;
    });
}

/**
 * Generar citaciones para los estudiantes seleccionados
 */
async function generarCitaciones() {
    const fechaCitacion = document.getElementById('citacion-fecha').value;

    if (!fechaCitacion) {
        mostrarMensaje('Advertencia', 'Por favor seleccione una fecha y hora para la citación', 'warning');
        return;
    }

    // Obtener estudiantes seleccionados
    const checkboxes = document.querySelectorAll('.checkbox-estudiante:checked');

    if (checkboxes.length === 0) {
        mostrarMensaje('Advertencia', 'Por favor seleccione al menos un estudiante', 'warning');
        return;
    }

    // Preparar datos para enviar
    const codigosEstudiantes = Array.from(checkboxes).map(cb =>
        parseInt(cb.dataset.codigo)
    );

    const datosEnvio = {
        codigosEstudiantes: codigosEstudiantes,
        fechaCitacion: fechaCitacion
    };

    try {
        // Mostrar indicador de carga
        document.getElementById('btn-confirmar-citaciones').disabled = true;
        document.getElementById('btn-confirmar-citaciones').innerHTML =
            '<i class="fas fa-spinner fa-spin"></i> Generando...';

        // Enviar petición al backend
        const response = await fetch(`${API_CITACIONES_URL}/crear-multiples`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(datosEnvio)
        });

        const resultado = await response.json();

        // Restaurar botón
        document.getElementById('btn-confirmar-citaciones').disabled = false;
        document.getElementById('btn-confirmar-citaciones').innerHTML =
            '<i class="fas fa-paper-plane"></i> Generar Citaciones';

        if (response.ok) {
            cerrarModal('modal-citaciones');
            mostrarMensaje(
                'Éxito',
                `Se generaron ${resultado.citacionesCreadas} citación(es) exitosamente`,
                'success'
            );
        } else {
            mostrarMensaje('Error', resultado.mensaje || 'Error al generar las citaciones', 'error');
        }

    } catch (error) {
        console.error('Error al generar citaciones:', error);

        // Restaurar botón
        document.getElementById('btn-confirmar-citaciones').disabled = false;
        document.getElementById('btn-confirmar-citaciones').innerHTML =
            '<i class="fas fa-paper-plane"></i> Generar Citaciones';

        mostrarMensaje('Error', 'Error al comunicarse con el servidor', 'error');
    }
}

// ==================== FIN FUNCIONES DE CITACIONES ====================

/**
 * Abre un modal con previsualización del PDF y opción de descarga
 */
function abrirModalPrevisualizacionPDF(pdfUrl, pdfBlob) {
    const modalExistente = document.getElementById('modal-previsualizacion-pdf');
    if (modalExistente) {
        modalExistente.remove();
    }

    const modal = document.createElement('div');
    modal.id = 'modal-previsualizacion-pdf';
    modal.className = 'modal show';
    modal.style.display = 'flex';

    modal.innerHTML = `
        <div class="modal-content modal-pdf">
            <div class="modal-header">
                <h2><i class="fas fa-file-pdf"></i> Previsualización del Listado</h2>
                <button class="modal-close" onclick="cerrarModalPDF()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="modal-body modal-body-pdf">
                <div class="pdf-controls">
                    <button class="btn btn-success" onclick="descargarPDF()">
                        <i class="fas fa-download"></i> Descargar PDF
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
    if (!window.currentPdfBlob || !grupoSeleccionado) {
        mostrarMensaje('Error', 'No hay PDF disponible para descargar', 'error');
        return;
    }

    const nombreArchivo = `Listado_${grupoSeleccionado.grado.nombreGrado.replace(/ /g, '_')}_Grupo${grupoSeleccionado.numeroGrupo}.pdf`;

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


// Funciones de estado de UI
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
    document.getElementById('results-section').style.display = 'none';
    document.getElementById('no-results-state').style.display = 'none';
}

// Funciones de modal
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