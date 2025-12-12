function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');
    
    sidebar.classList.toggle('active');
    overlay.classList.toggle('active');
}

// Configuración de la API
const API_URL = 'http://localhost:8080/api/preinscripcion';
const API_ESTUDIANTE_URL = 'http://localhost:8080/api/estudiante';

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
        const query = e.target.value.trim();
        
        if (query.length >= 2) {
            timeoutBusqueda = setTimeout(() => {
                buscarPreinscripciones(query);
            }, 500);
        } else if (query.length === 0) {
            mostrarTabla(preinscripcionesData);
        }
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
        
        console.log('Preinscripciones cargadas:', preinscripciones);
        
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

// Buscar preinscripciones
async function buscarPreinscripciones(query) {
    mostrarLoading();
    
    try {
        const response = await fetch(`${API_URL}/buscar?textoBusqueda=${encodeURIComponent(query)}`);
        
        if (response.status === 404) {
            mostrarSinResultados();
            return;
        }
        
        if (!response.ok) {
            throw new Error('Error al buscar preinscripciones');
        }
        
        const preinscripciones = await response.json();
        mostrarTabla(preinscripciones);
        
    } catch (error) {
        console.error('Error al buscar:', error);
        mostrarError();
    }
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
        
        // Obtener datos del aspirante de forma segura
        const nombreAspirante = obtenerNombreCompleto(preinscripcion.aspirante);
        const nombreAcudiente = obtenerNombreCompleto(preinscripcion.acudiente);
        
        // Fecha de preinscripción
        const fechaPreinscripcion = preinscripcion.fechaEntrevista ? 
            new Date(preinscripcion.fechaEntrevista).toLocaleDateString('es-ES') : 
            'Sin fecha';
        
        // Estado del aspirante
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
                        <i class="fas fa-calendar-alt"></i>
                    </button>
                    <button class="btn btn-success" 
                        onclick="cambiarEstado(${preinscripcion.idPreinscripcion})" 
                        title="Gestionar Estado">
                        <i class="fas fa-toggle-on"></i>
                    </button>
                </div>
            </td>
        `;
        
        tbody.appendChild(tr);
    });
    
    document.getElementById('tabla-container').style.display = 'block';
}

// Función auxiliar para obtener nombre completo de forma segura
function obtenerNombreCompleto(entidad) {
    if (!entidad || !entidad.persona) {
        return 'Sin nombre';
    }
    
    const nombre = entidad.persona.nombre || '';
    const apellido = entidad.persona.apellido || '';
    
    return `${nombre} ${apellido}`.trim() || 'Sin nombre';
}

// Obtener clase CSS según el estado
function getEstadoClass(estado) {
    const estadoLower = (estado || '').toLowerCase();
    if (estadoLower === 'pendiente') return 'estado-pendiente';
    if (estadoLower === 'aprobado') return 'estado-aprobado';
    if (estadoLower === 'rechazado') return 'estado-rechazado';
    if (estadoLower === 'en revisión' || estadoLower === 'en revision') return 'estado-revision';
    if (estadoLower === 'activo') return 'estado-aprobado';
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
    
            // CÓDIGO CORRECTO
        const entrevistaData = {
            // Enviamos el string directo y le agregamos segundos
            fechaEntrevista: fechaEntrevista + ":00", 
            lugarEntrevista: lugarEntrevista
        };
    
    try {
        const response = await fetch(`${API_URL}/${preinscripcionSeleccionada}/programar-entrevista`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(entrevistaData)
        });
        
        const data = await response.json();
        
        if (response.ok) {
            cerrarModal('modal-programar-entrevista');
            mostrarMensaje('Éxito', data.mensaje || 'Entrevista programada exitosamente', 'success');
            cargarPreinscripciones();
        } else {
            mostrarMensaje('Error', data.mensaje || 'Error al programar la entrevista', 'error');
        }
        
    } catch (error) {
        console.error('Error al programar entrevista:', error);
        cerrarModal('modal-programar-entrevista');
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

// NUEVA FUNCIÓN: Controlar visibilidad de campos de hoja de vida
function toggleCamposHojaVida() {
    const estadoSelect = document.getElementById('nuevo-estado');
    const contenedorCampos = document.getElementById('campos-hoja-vida');
    
    if (estadoSelect.value === 'Aprobado') {
        contenedorCampos.style.display = 'block';
    } else {
        contenedorCampos.style.display = 'none';
        // Limpiar campos para evitar envío accidental de datos sucios
        document.getElementById('estudiante-alergias').value = '';
        document.getElementById('estudiante-enfermedades').value = '';
        document.getElementById('estudiante-problemas-aprendizaje').value = '';
    }
}

// Cargar datos en el formulario de cambio de estado
function cargarDatosEnFormulario(preinscripcion) {
    // Información del estudiante
    const nombreAspirante = obtenerNombreCompleto(preinscripcion.aspirante);
    document.getElementById('estudiante-nombre').value = nombreAspirante;
    
    // Fecha de nacimiento
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
    const nombreAcudiente = obtenerNombreCompleto(preinscripcion.acudiente);
    document.getElementById('acudiente-nombre').value = nombreAcudiente;
    document.getElementById('acudiente-telefono').value = preinscripcion.acudiente?.telefono || '';
    
    // Estado
    document.getElementById('nuevo-estado').value = '';
    
    // Asegurarse de que los campos arranquen ocultos al abrir el modal
    document.getElementById('campos-hoja-vida').style.display = 'none';
}

// Confirmar cambio de estado con VALIDACIÓN OBLIGATORIA
async function confirmarCambiarEstado() {
    let nuevoEstadoSeleccionado = document.getElementById('nuevo-estado').value;
    
    if (!nuevoEstadoSeleccionado) {
        mostrarMensaje('Advertencia', 'Por favor seleccione un nuevo estado', 'warning');
        return;
    }

    // LÓGICA DE VALIDACIÓN Y MAPEO
    let estadoParaEnviar = nuevoEstadoSeleccionado;
    let alergias = '';
    let enfermedades = '';
    let problemas = '';

    if (nuevoEstadoSeleccionado === 'Aprobado') {
        // 1. Convertimos visual 'Aprobado' a interno 'Activo'
        estadoParaEnviar = 'Activo';
        
        // 2. Capturamos los datos
        alergias = document.getElementById('estudiante-alergias').value.trim();
        enfermedades = document.getElementById('estudiante-enfermedades').value.trim();
        problemas = document.getElementById('estudiante-problemas-aprendizaje').value.trim();

        // 3. VALIDACIÓN ESTRICTA: Si intenta aprobar sin llenar campos, paramos todo.
        if (alergias === '' || enfermedades === '') {
            mostrarMensaje(
                'Faltan Datos', 
                'Para aprobar al estudiante es OBLIGATORIO llenar la hoja de vida.\n\nSi el estudiante no tiene alergias o enfermedades, por favor escriba "Ninguna".', 
                'warning'
            );
            return; // DETENEMOS LA FUNCIÓN AQUÍ
        }
    } 
    // Si es "Rechazado", se envía "Rechazado" y los campos vacíos

    const hojaVidaData = {
        alergias: alergias,
        enfermedades: enfermedades,
        problemasAprendizaje: problemas,
        telefonoAcudiente: document.getElementById('acudiente-telefono').value.trim(),
        nuevoEstado: estadoParaEnviar
    };
    
    try {
        const response = await fetch(`${API_URL}/${preinscripcionSeleccionada}/cambiar-estado`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(hojaVidaData)
        });
        
        const data = await response.json();
        
        if (response.ok) {
            cerrarModal('modal-cambiar-estado');
            mostrarMensaje('Éxito', data.mensaje || 'Estado actualizado exitosamente', 'success');
            cargarPreinscripciones();
        } else {
            mostrarMensaje('Error', data.mensaje || 'Error al cambiar el estado', 'error');
        }
        
    } catch (error) {
        console.error('Error al cambiar estado:', error);
        cerrarModal('modal-cambiar-estado');
        mostrarMensaje('Error', 'Error en la base de datos', 'error');
    }
}

// Mostrar sin resultados
function mostrarSinResultados() {
    ocultarTodosLosEstados();
    const tbody = document.getElementById('tbody-preinscripciones');
    tbody.innerHTML = `
        <tr>
            <td colspan="5" style="text-align: center; padding: 2rem;">
                <i class="fas fa-search" style="font-size: 3rem; color: var(--text-secondary); margin-bottom: 1rem;"></i>
                <p>No se encontraron preinscripciones con ese criterio</p>
            </td>
        </tr>
    `;
    document.getElementById('tabla-container').style.display = 'block';
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