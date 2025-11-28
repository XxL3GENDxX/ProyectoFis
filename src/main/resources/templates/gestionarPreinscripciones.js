function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');
    
    sidebar.classList.toggle('active');
    overlay.classList.toggle('active');
}

// Configuración de la API
const API_URL = 'http://localhost:8080/api/preinscripcion';

// Estado global
let preinscripcionesData = [];
let timeoutBusqueda = null;
let preinscripcionSeleccionada = null;

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    inicializarEventos();
    cargarPreinscripciones();
});

// Inicializar eventos
function inicializarEventos() {
    document.getElementById('txt-buscar-preinscripcion').addEventListener('input', function(e) {
        clearTimeout(timeoutBusqueda);
        timeoutBusqueda = setTimeout(() => {
            filtrarPreinscripciones(e.target.value);
        }, 300);
    });

    document.getElementById('btn-confirmar-programar').addEventListener('click', confirmarProgramarEntrevista);
    document.getElementById('btn-confirmar-cambiar-estado').addEventListener('click', confirmarCambiarEstado);
}

// Cargar todas las preinscripciones
async function cargarPreinscripciones() {
    mostrarLoading();
    
    try {
        const response = await fetch(API_URL);
        
        if (!response.ok) {
            throw new Error('Error al cargar preinscripciones');
        }
        
        const preinscripciones = await response.json();
        preinscripcionesData = preinscripciones;
        
        if (preinscripciones.length === 0) {
            mostrarEstadoVacio();
        } else {
            mostrarTabla(preinscripciones);
        }
    } catch (error) {
        console.error('Error:', error);
        mostrarError();
    }
}

// Filtrar preinscripciones localmente
function filtrarPreinscripciones(texto) {
    if (!texto || texto.trim() === '') {
        mostrarTabla(preinscripcionesData);
        return;
    }
    
    const textoLower = texto.toLowerCase().trim();
    const preinscripcionesFiltradas = preinscripcionesData.filter(preinscripcion => {
        const nombreAspirante = preinscripcion.aspirante?.persona ? 
            `${preinscripcion.aspirante.persona.nombre} ${preinscripcion.aspirante.persona.apellido}`.toLowerCase() : '';
        const nombreAcudiente = preinscripcion.acudiente?.persona ? 
            `${preinscripcion.acudiente.persona.nombre} ${preinscripcion.acudiente.persona.apellido}`.toLowerCase() : '';
        
        return nombreAspirante.includes(textoLower) || nombreAcudiente.includes(textoLower);
    });
    
    mostrarTabla(preinscripcionesFiltradas);
}

// Mostrar tabla de preinscripciones
function mostrarTabla(preinscripciones) {
    ocultarTodosLosEstados();
    
    const tbody = document.getElementById('tbody-preinscripciones');
    tbody.innerHTML = '';
    
    if (preinscripciones.length === 0) {
        mostrarEstadoVacio();
        return;
    }
    
    preinscripciones.forEach(preinscripcion => {
        const tr = document.createElement('tr');
        
        const nombreAspirante = preinscripcion.aspirante?.persona ? 
            `${preinscripcion.aspirante.persona.nombre} ${preinscripcion.aspirante.persona.apellido}` : 
            'Sin nombre';
        
        const nombreAcudiente = preinscripcion.acudiente?.persona ? 
            `${preinscripcion.acudiente.persona.nombre} ${preinscripcion.acudiente.persona.apellido}` : 
            'Sin nombre';
        
        const fechaPreinscripcion = preinscripcion.fechaEntrevista ? 
            new Date(preinscripcion.fechaEntrevista).toLocaleDateString('es-ES') : 
            'Sin fecha';
        
        const estado = preinscripcion.aspirante?.estado || 'Pendiente';
        const estadoClass = getEstadoClass(estado);
        
        tr.innerHTML = `
            <td>${nombreAspirante}</td>
            <td>${nombreAcudiente}</td>
            <td>${fechaPreinscripcion}</td>
            <td><span class="estado-badge ${estadoClass}">${estado}</span></td>
            <td>
                <div class="options-cell">
                    <button class="btn btn-primary" 
                        onclick="programarEntrevista(${preinscripcion.idPreinscripcion})" 
                        title="Programar Entrevista">
                        <i class="fas fa-calendar-alt"></i> Programar Entrevista
                    </button>
                    <button class="btn btn-success" 
                        onclick="cambiarEstado(${preinscripcion.idPreinscripcion})" 
                        title="Cambiar Estado">
                        <i class="fas fa-toggle-on"></i> Cambiar Estado
                    </button>
                </div>
            </td>
        `;
        
        tbody.appendChild(tr);
    });
    
    document.getElementById('tabla-container').style.display = 'block';
}

// Obtener clase CSS según el estado
function getEstadoClass(estado) {
    const estadoLower = estado.toLowerCase();
    if (estadoLower === 'pendiente') return 'estado-pendiente';
    if (estadoLower === 'aprobado') return 'estado-aprobado';
    if (estadoLower === 'rechazado') return 'estado-rechazado';
    if (estadoLower === 'en revisión' || estadoLower === 'en revision') return 'estado-revision';
    return 'estado-pendiente';
}

// Programar entrevista
async function programarEntrevista(idPreinscripcion) {
    try {
        const response = await fetch(`${API_URL}/${idPreinscripcion}`);
        
        if (!response.ok) {
            throw new Error('Error al cargar datos de la preinscripción');
        }
        
        const preinscripcion = await response.json();
        preinscripcionSeleccionada = idPreinscripcion;
        
        // Limpiar el formulario
        document.getElementById('fecha-entrevista').value = '';
        document.getElementById('lugar-entrevista').value = '';
        
        abrirModal('modal-programar-entrevista');
        
    } catch (error) {
        console.error('Error al cargar preinscripción:', error);
        mostrarMensaje('Error', 'Error al cargar los datos de la preinscripción', 'error');
    }
}

// Confirmar programación de entrevista
async function confirmarProgramarEntrevista() {
    const fechaEntrevista = document.getElementById('fecha-entrevista').value;
    const lugarEntrevista = document.getElementById('lugar-entrevista').value.trim();
    
    if (!fechaEntrevista || !lugarEntrevista) {
        mostrarMensaje('Advertencia', 'Por favor complete todos los campos obligatorios', 'warning');
        return;
    }
    
    const entrevistaData = {
        fechaEntrevista: new Date(fechaEntrevista).toISOString(),
        lugarEntrevista: lugarEntrevista
    };
    
    try {
        const response = await fetch(`${API_URL}/${preinscripcionSeleccionada}/programar-entrevista`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(entrevistaData)
        });
        
        if (response.ok) {
            cerrarModal('modal-programar-entrevista');
            mostrarMensaje('Éxito', 'Entrevista programada exitosamente', 'success');
            cargarPreinscripciones();
        } else {
            const data = await response.json();
            mostrarMensaje('Error', data.mensaje || 'Error al programar la entrevista', 'error');
        }
        
    } catch (error) {
        console.error('Error al programar entrevista:', error);
        mostrarMensaje('Error', 'Error en la base de datos', 'error');
    }
}

// Cambiar estado con hoja de vida
async function cambiarEstado(idPreinscripcion) {
    try {
        const response = await fetch(`${API_URL}/${idPreinscripcion}`);
        
        if (!response.ok) {
            throw new Error('Error al cargar datos de la preinscripción');
        }
        
        const preinscripcion = await response.json();
        preinscripcionSeleccionada = idPreinscripcion;
        
        cargarDatosEnFormulario(preinscripcion);
        abrirModal('modal-cambiar-estado');
        
    } catch (error) {
        console.error('Error al cargar preinscripción:', error);
        mostrarMensaje('Error', 'Error al cargar los datos de la preinscripción', 'error');
    }
}

// Cargar datos en el formulario de cambio de estado
function cargarDatosEnFormulario(preinscripcion) {
    // Información del estudiante
    const nombreAspirante = preinscripcion.aspirante?.persona ? 
        `${preinscripcion.aspirante.persona.nombre} ${preinscripcion.aspirante.persona.apellido}` : '';
    
    document.getElementById('estudiante-nombre').value = nombreAspirante;
    
    if (preinscripcion.aspirante?.persona?.fechaDeNacimiento) {
        const fecha = new Date(preinscripcion.aspirante.persona.fechaDeNacimiento);
        document.getElementById('estudiante-fecha-nacimiento').value = fecha.toISOString().split('T')[0];
    } else {
        document.getElementById('estudiante-fecha-nacimiento').value = '';
    }
    
    // Limpiar campos editables
    document.getElementById('estudiante-alergias').value = '';
    document.getElementById('estudiante-enfermedades').value = '';
    document.getElementById('estudiante-problemas-aprendizaje').value = '';
    
    // Información del acudiente
    const nombreAcudiente = preinscripcion.acudiente?.persona ? 
        `${preinscripcion.acudiente.persona.nombre} ${preinscripcion.acudiente.persona.apellido}` : '';
    
    document.getElementById('acudiente-nombre').value = nombreAcudiente;
    document.getElementById('acudiente-telefono').value = preinscripcion.acudiente?.telefono || '';
    
    // Estado
    document.getElementById('nuevo-estado').value = '';
}

// Confirmar cambio de estado
async function confirmarCambiarEstado() {
    const nuevoEstado = document.getElementById('nuevo-estado').value;
    
    if (!nuevoEstado) {
        mostrarMensaje('Advertencia', 'Por favor seleccione un nuevo estado', 'warning');
        return;
    }
    
    // Recopilar datos de la hoja de vida
    const hojaVidaData = {
        alergias: document.getElementById('estudiante-alergias').value.trim(),
        enfermedades: document.getElementById('estudiante-enfermedades').value.trim(),
        problemasAprendizaje: document.getElementById('estudiante-problemas-aprendizaje').value.trim(),
        telefonoAcudiente: document.getElementById('acudiente-telefono').value.trim(),
        nuevoEstado: nuevoEstado
    };
    
    try {
        const response = await fetch(`${API_URL}/${preinscripcionSeleccionada}/cambiar-estado`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(hojaVidaData)
        });
        
        if (response.ok) {
            cerrarModal('modal-cambiar-estado');
            mostrarMensaje('Éxito', 'Estado actualizado exitosamente', 'success');
            cargarPreinscripciones();
        } else {
            const data = await response.json();
            mostrarMensaje('Error', data.mensaje || 'Error al cambiar el estado', 'error');
        }
        
    } catch (error) {
        console.error('Error al cambiar estado:', error);
        mostrarMensaje('Error', 'Error en la base de datos', 'error');
    }
}

// Funciones de UI
function mostrarLoading() {
    ocultarTodosLosEstados();
    document.getElementById('loading-spinner').style.display = 'block';
}

function mostrarEstadoVacio() {
    ocultarTodosLosEstados();
    document.getElementById('empty-state').style.display = 'block';
}

function mostrarError() {
    ocultarTodosLosEstados();
    document.getElementById('error-state').style.display = 'block';
}

function ocultarTodosLosEstados() {
    document.getElementById('loading-spinner').style.display = 'none';
    document.getElementById('tabla-container').style.display = 'none';
    document.getElementById('empty-state').style.display = 'none';
    document.getElementById('error-state').style.display = 'none';
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

window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
        event.target.classList.remove('show');
    }
}