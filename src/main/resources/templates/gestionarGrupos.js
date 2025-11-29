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

// Estado global
let grupoSeleccionado = null;
let modoEdicion = false;
let timeoutBusqueda = null;

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    inicializarEventos();
    cargarGradosEnModal();
});

// Inicializar eventos
function inicializarEventos() {
    // Búsqueda con debounce
    document.getElementById('txt-buscar-grupo').addEventListener('input', function(e) {
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
    document.getElementById('btn-generar-citaciones').addEventListener('click', generarCitaciones);
    
    // Botones del modal
    document.getElementById('btn-guardar-grupo').addEventListener('click', guardarGrupo);
    document.getElementById('btn-confirmar-eliminar-grupo').addEventListener('click', eliminarGrupo);
}

// Buscar grupos
async function buscarGrupos(query) {
    if (!query || query.trim() === '') {
        mostrarEstadoVacio();
        return;
    }

    mostrarLoading();

    try {
        // Buscar por grado o número de grupo
        const queryLower = query.toLowerCase().trim();
        
        // Primero intentar buscar todos los grados
        const gradosResponse = await fetch(API_GRADOS_URL);
        if (!gradosResponse.ok) {
            throw new Error('Error al buscar grados');
        }
        
        const grados = await gradosResponse.json();
        
        // Filtrar grados que coincidan con la búsqueda
        const gradosFiltrados = grados.filter(grado => 
            grado.nombreGrado.toLowerCase().includes(queryLower)
        );

        if (gradosFiltrados.length > 0) {
            // Si encontramos grados, mostrar todos sus grupos
            if (gradosFiltrados.length === 1) {
                // Si es solo un grado, mostrar sus grupos
                await mostrarGruposPorGrado(gradosFiltrados[0].idGrado, query);
            } else {
                // Si son múltiples grados, mostrar tarjetas de todos los grupos
                await mostrarMultiplesGrados(gradosFiltrados, query);
            }
        } else {
            // Intentar buscar por número de grupo en todos los grados
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
            // Si solo hay un grupo, mostrar directamente sus estudiantes
            await mostrarDetalleGrupo(grupos[0], query);
        } else {
            // Si hay múltiples grupos, mostrar tarjetas
            mostrarListaGrupos(grupos, query);
        }

    } catch (error) {
        console.error('Error:', error);
        mostrarMensaje('Error', 'Error al cargar grupos del grado', 'error');
    }
}

// Mostrar múltiples grados
async function mostrarMultiplesGrados(grados, query) {
    try {
        const todosLosGrupos = [];
        
        for (const grado of grados) {
            const response = await fetch(`${API_GRUPOS_URL}/grado/${grado.idGrado}`);
            if (response.ok) {
                const grupos = await response.json();
                todosLosGrupos.push(...grupos);
            }
        }

        if (todosLosGrupos.length === 0) {
            mostrarSinResultados();
        } else {
            mostrarListaGrupos(todosLosGrupos, query);
        }

    } catch (error) {
        console.error('Error:', error);
        mostrarMensaje('Error', 'Error al cargar grupos', 'error');
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

    // Ocultar botones de edición y tabla
    document.getElementById('btn-editar-grupo').style.display = 'none';
    document.getElementById('btn-eliminar-grupo').style.display = 'none';
    document.getElementById('students-table-container').style.display = 'none';

    grupos.forEach(grupo => {
        const card = document.createElement('div');
        card.className = 'group-card';
        card.onclick = () => mostrarDetalleGrupo(grupo, query);

        const numEstudiantes = grupo.numeroEstudiantes || 0;
        const porcentajeOcupacion = (numEstudiantes / grupo.limiteEstudiantes) * 100;

        card.innerHTML = `
            <div class="group-card-header">
                <div class="group-icon">
                    <i class="fas fa-users"></i>
                </div>
                <div class="group-card-title">
                    <h3>${grupo.grado.nombreGrado} - Grupo ${grupo.numeroGrupo}</h3>
                    <p>${grupo.directorGrupo || 'Sin director asignado'}</p>
                </div>
            </div>
            <div class="group-card-body">
                <div class="group-info-item">
                    <span>Estudiantes:</span>
                    <strong>${numEstudiantes} / ${grupo.limiteEstudiantes}</strong>
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

    // Mostrar botones de edición
    document.getElementById('btn-editar-grupo').style.display = 'inline-flex';
    document.getElementById('btn-eliminar-grupo').style.display = 'inline-flex';

    // Ocultar lista de grupos
    document.getElementById('groups-list-container').style.display = 'none';

    // Cargar estudiantes del grupo
    try {
        // Aquí deberías hacer una petición al backend para obtener los estudiantes del grupo
        // Por ahora simularemos con los datos del grupo
        
        const estudiantes = grupo.estudiantes ? Object.values(grupo.estudiantes) : [];

        tbody.innerHTML = '';

        if (estudiantes.length === 0) {
            emptyStudentsState.style.display = 'block';
            document.getElementById('tabla-estudiantes-grupo').style.display = 'none';
        } else {
            emptyStudentsState.style.display = 'none';
            document.getElementById('tabla-estudiantes-grupo').style.display = 'table';

            estudiantes.forEach(estudiante => {
                const tr = document.createElement('tr');
                const estadoClass = estudiante.estado === 'Activo' ? 'estado-activo' : 
                                  estudiante.estado === 'Inactivo' ? 'estado-inactivo' : 
                                  'estado-pendiente';

                tr.innerHTML = `
                    <td>${estudiante.persona.nombre}</td>
                    <td>${estudiante.persona.apellido}</td>
                    <td><span class="estado-badge ${estadoClass}">${estudiante.estado}</span></td>
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
    document.getElementById('grupo-limite').value = '30';
    
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
    document.getElementById('grupo-director').value = grupoSeleccionado.directorGrupo || '';
    document.getElementById('grupo-limite').value = grupoSeleccionado.limiteEstudiantes;
    
    abrirModal('modal-grupo');
}

// Guardar grupo (crear o editar)
async function guardarGrupo() {
    const idGrado = document.getElementById('grupo-grado').value;
    const numeroGrupo = document.getElementById('grupo-numero').value;
    const directorGrupo = document.getElementById('grupo-director').value.trim();
    const limiteEstudiantes = document.getElementById('grupo-limite').value;

    if (!idGrado || !numeroGrupo || !limiteEstudiantes) {
        mostrarMensaje('Advertencia', 'Por favor complete todos los campos obligatorios', 'warning');
        return;
    }

    const grupoData = {
        grado: { idGrado: parseInt(idGrado) },
        numeroGrupo: parseInt(numeroGrupo),
        directorGrupo: directorGrupo || null,
        limiteEstudiantes: parseInt(limiteEstudiantes)
    };

    try {
        let response;
        
        if (modoEdicion && grupoSeleccionado) {
            // Actualizar grupo existente
            response = await fetch(`${API_GRUPOS_URL}/${grupoSeleccionado.idGrupo}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(grupoData)
            });
        } else {
            // Crear nuevo grupo
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
            
            // Recargar búsqueda si había una activa
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
        const response = await fetch(`${API_GRUPOS_URL}/${grupoSeleccionado.idGrupo}`, {
            method: 'DELETE'
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

// Generar listado (exportar a Excel)
function generarListado() {
    if (!grupoSeleccionado) {
        mostrarMensaje('Advertencia', 'No hay ningún grupo seleccionado', 'warning');
        return;
    }

    mostrarMensaje('Información', 'Funcionalidad de exportación en desarrollo', 'info');
}

// Generar citaciones
function generarCitaciones() {
    if (!grupoSeleccionado) {
        mostrarMensaje('Advertencia', 'No hay ningún grupo seleccionado', 'warning');
        return;
    }

    mostrarMensaje('Información', 'Funcionalidad de citaciones en desarrollo', 'info');
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
window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
        event.target.classList.remove('show');
    }
}