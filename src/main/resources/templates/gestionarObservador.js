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
const API_OBSERVACIONES_URL = `${API_URL}/observaciones`;

// Estado global
let estudianteActual = null;
let observacionSeleccionada = null;
let modoEdicion = false;
let timeoutBusqueda = null;

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    inicializarEventos();
});

// Inicializar eventos
function inicializarEventos() {
    // Búsqueda con debounce
    document.getElementById('txt-buscar-estudiante').addEventListener('input', function(e) {
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
    document.getElementById('txt-buscar-estudiante').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            buscarEstudiante();
        }
    });

    // Botones principales
    document.getElementById('btn-ver-hoja-vida').addEventListener('click', mostrarHojaVida);
    document.getElementById('btn-anadir-observacion').addEventListener('click', abrirModalNuevaObservacion);
    document.getElementById('btn-guardar-observacion').addEventListener('click', guardarObservacion);
    document.getElementById('btn-confirmar-eliminar-observacion').addEventListener('click', eliminarObservacion);
}

// Buscar estudiante
async function buscarEstudiante() {
    const query = document.getElementById('txt-buscar-estudiante').value.trim();
    
    if (!query) {
        mostrarMensaje('Advertencia', 'Por favor ingrese un nombre o ID para buscar', 'warning');
        return;
    }

    mostrarLoading();

    try {
        // Buscar estudiante por nombre o documento
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
            // Si solo hay un resultado, mostrarlo directamente
            await cargarObservador(estudiantes[0]);
        } else {
            // Si hay múltiples resultados, mostrar el primero
            // (podrías implementar una lista de selección)
            await cargarObservador(estudiantes[0]);
        }

    } catch (error) {
        console.error('Error al buscar estudiante:', error);
        mostrarMensaje('Error', 'Error al buscar el estudiante', 'error');
        mostrarEstadoVacio();
    }
}

// Cargar observador del estudiante
async function cargarObservador(estudiante) {
    estudianteActual = estudiante;
    
    try {
        // Cargar observaciones del estudiante
        const response = await fetch(`${API_OBSERVACIONES_URL}/estudiante/${estudiante.codigoEstudiante}`);
        
        let observaciones = [];
        if (response.ok) {
            observaciones = await response.json();
        }

        mostrarObservador(estudiante, observaciones);

    } catch (error) {
        console.error('Error al cargar observaciones:', error);
        mostrarObservador(estudiante, []);
    }
}

// Mostrar observador con observaciones
function mostrarObservador(estudiante, observaciones) {
    ocultarTodosLosEstados();

    const observadorSection = document.getElementById('observador-section');
    const nombreEstudiante = document.getElementById('nombre-estudiante');
    const observacionesContainer = document.getElementById('observaciones-container');
    const emptyObservacionesState = document.getElementById('empty-observaciones-state');

    // Actualizar nombre del estudiante
    nombreEstudiante.textContent = `${estudiante.persona.nombre} ${estudiante.persona.apellido}`;

    // Limpiar contenedor
    observacionesContainer.innerHTML = '';

    if (observaciones.length === 0) {
        observacionesContainer.style.display = 'none';
        emptyObservacionesState.style.display = 'block';
    } else {
        observacionesContainer.style.display = 'flex';
        emptyObservacionesState.style.display = 'none';

        observaciones.forEach(observacion => {
            const card = document.createElement('div');
            card.className = 'observacion-card';
            
            card.innerHTML = `
                <div class="observacion-header-card">
                    <h3 class="observacion-title">${observacion.titulo}</h3>
                    <div class="observacion-actions">
                        <button class="btn btn-secondary btn-icon" 
                                onclick="editarObservacion(${observacion.idObservacion})" 
                                title="Editar observación">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-danger btn-icon" 
                                onclick="confirmarEliminarObservacion(${observacion.idObservacion}, '${observacion.titulo.replace(/'/g, "\\'")}')" 
                                title="Eliminar observación">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
                <p class="observacion-descripcion">${observacion.descripcion}</p>
            `;

            observacionesContainer.appendChild(card);
        });
    }

    observadorSection.style.display = 'block';
}

// Abrir modal para nueva observación
function abrirModalNuevaObservacion() {
    if (!estudianteActual) {
        mostrarMensaje('Advertencia', 'No hay ningún estudiante seleccionado', 'warning');
        return;
    }

    modoEdicion = false;
    observacionSeleccionada = null;
    
    document.getElementById('modal-observacion-titulo').textContent = 'Nueva Observación';
    document.getElementById('form-observacion').reset();
    
    abrirModal('modal-observacion');
}

// Editar observación existente
async function editarObservacion(idObservacion) {
    try {
        const response = await fetch(`${API_OBSERVACIONES_URL}/${idObservacion}`);
        
        if (!response.ok) {
            throw new Error('Error al cargar observación');
        }

        const observacion = await response.json();
        observacionSeleccionada = observacion;
        modoEdicion = true;

        document.getElementById('modal-observacion-titulo').textContent = 'Editar Observación';
        document.getElementById('observacion-titulo').value = observacion.titulo;
        document.getElementById('observacion-descripcion').value = observacion.descripcion;

        abrirModal('modal-observacion');

    } catch (error) {
        console.error('Error al cargar observación:', error);
        mostrarMensaje('Error', 'Error al cargar los datos de la observación', 'error');
    }
}

// Guardar observación (crear o editar)
async function guardarObservacion() {
    const titulo = document.getElementById('observacion-titulo').value.trim();
    const descripcion = document.getElementById('observacion-descripcion').value.trim();

    // Validaciones
    if (!titulo) {
        mostrarMensaje('Advertencia', 'El título es obligatorio', 'warning');
        return;
    }

    if (!descripcion) {
        mostrarMensaje('Advertencia', 'La descripción es obligatoria', 'warning');
        return;
    }

    if (titulo.length > 200) {
        mostrarMensaje('Advertencia', 'El título no puede exceder 200 caracteres', 'warning');
        return;
    }

    if (descripcion.length > 1000) {
        mostrarMensaje('Advertencia', 'La descripción no puede exceder 1000 caracteres', 'warning');
        return;
    }

    const observacionData = {
        titulo: titulo,
        descripcion: descripcion,
        estudiante: {
            codigoEstudiante: estudianteActual.codigoEstudiante
        }
    };

    try {
        let response;

        if (modoEdicion && observacionSeleccionada) {
            // Actualizar observación existente
            response = await fetch(`${API_OBSERVACIONES_URL}/${observacionSeleccionada.idObservacion}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(observacionData)
            });
        } else {
            // Crear nueva observación
            response = await fetch(API_OBSERVACIONES_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(observacionData)
            });
        }

        if (response.ok) {
            cerrarModal('modal-observacion');
            mostrarMensaje('Éxito', 
                modoEdicion ? 'Observación actualizada exitosamente' : 'Observación creada exitosamente', 
                'success');
            
            // Recargar observaciones del estudiante actual
            await cargarObservador(estudianteActual);
        } else {
            const errorData = await response.json();
            mostrarMensaje('Error', errorData.mensaje || 'Error al guardar la observación', 'error');
        }

    } catch (error) {
        console.error('Error al guardar observación:', error);
        cerrarModal('modal-observacion');
        mostrarMensaje('Error', 'Error al comunicarse con el servidor', 'error');
    }
}

// Confirmar eliminación de observación
function confirmarEliminarObservacion(idObservacion, titulo) {
    observacionSeleccionada = { idObservacion: idObservacion };
    document.getElementById('titulo-observacion-eliminar').textContent = titulo;
    abrirModal('modal-confirmar-eliminar');
}

// Eliminar observación
async function eliminarObservacion() {
    if (!observacionSeleccionada) {
        return;
    }

    try {
        const response = await fetch(`${API_OBSERVACIONES_URL}/${observacionSeleccionada.idObservacion}`, {
            method: 'DELETE'
        });

        cerrarModal('modal-confirmar-eliminar');

        if (response.ok) {
            mostrarMensaje('Éxito', 'Observación eliminada exitosamente', 'success');
            observacionSeleccionada = null;
            
            // Recargar observaciones del estudiante actual
            await cargarObservador(estudianteActual);
        } else {
            const errorData = await response.json();
            mostrarMensaje('Error', errorData.mensaje || 'Error al eliminar la observación', 'error');
        }

    } catch (error) {
        console.error('Error al eliminar observación:', error);
        cerrarModal('modal-confirmar-eliminar');
        mostrarMensaje('Error', 'Error al comunicarse con el servidor', 'error');
    }
}

// Mostrar hoja de vida del estudiante
function mostrarHojaVida() {
    if (!estudianteActual) {
        mostrarMensaje('Advertencia', 'No hay ningún estudiante seleccionado', 'warning');
        return;
    }

    // Llenar información del estudiante
    document.getElementById('hv-nombre').textContent = 
        `${estudianteActual.persona.nombre} ${estudianteActual.persona.apellido}`;
    
    document.getElementById('hv-grado').textContent = 
        estudianteActual.grupo ? estudianteActual.grupo.grado.nombreGrado : 'N/A';
    
    document.getElementById('hv-grupo').textContent = 
        estudianteActual.grupo ? estudianteActual.grupo.numeroGrupo : 'Sin asignar';
    
    document.getElementById('hv-estado').innerHTML = 
        `<span class="estado-badge estado-${estudianteActual.estado.toLowerCase()}">${estudianteActual.estado}</span>`;

    // Información del acudiente
    if (estudianteActual.acudiente) {
        document.getElementById('hv-acudiente').textContent = 
            `${estudianteActual.acudiente.persona.nombre} ${estudianteActual.acudiente.persona.apellido}`;
        
        document.getElementById('hv-telefono').textContent = 
            estudianteActual.acudiente.telefono || 'No registrado';
        
        document.getElementById('hv-correo').textContent = 
            estudianteActual.acudiente.correoElectronico || 'No registrado';
    } else {
        document.getElementById('hv-acudiente').textContent = 'No registrado';
        document.getElementById('hv-telefono').textContent = 'No registrado';
        document.getElementById('hv-correo').textContent = 'No registrado';
    }

    // Información médica (datos de ejemplo - deberías obtenerlos del backend)
    document.getElementById('hv-alergias').textContent = 'Maní, Penicilina';
    document.getElementById('hv-enfermedades').textContent = 'Asma (leve)';
    document.getElementById('hv-problemas-aprendizaje').textContent = 
        'Posible dislexia (en evaluación), requiere apoyo en lectoescritura.';
    document.getElementById('hv-otros-aspectos').textContent = 
        'La estudiante es hija única, vive con madre y abuela.';

    abrirModal('modal-hoja-vida');
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
    document.getElementById('observador-section').style.display = 'none';
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