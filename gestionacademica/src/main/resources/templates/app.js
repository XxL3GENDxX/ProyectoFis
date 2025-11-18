// Configuración de la API
const API_URL = 'http://localhost:8080/api/estudiantes';

// Estado global
let modoGestionGrupo = false;
let estudiantesData = [];
let timeoutBusqueda = null;

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    inicializarEventos();
    cargarEstudiantes();
    cargarEstadisticas();
});

// Inicializar eventos
function inicializarEventos() {
    // Búsqueda en tiempo real
    document.getElementById('txt-buscar-estudiante').addEventListener('input', function(e) {
        clearTimeout(timeoutBusqueda);
        timeoutBusqueda = setTimeout(() => {
            buscarEstudiantes(e.target.value);
        }, 300);
    });

    // Toggle filtros
    document.getElementById('btn-toggle-filtros').addEventListener('click', toggleFiltros);

    // Aplicar filtros
    document.getElementById('btn-aplicar-filtros').addEventListener('click', aplicarFiltros);

    // Limpiar filtros
    document.getElementById('btn-limpiar-filtros').addEventListener('click', limpiarFiltros);

    // Modo gestión de grupos
    document.getElementById('btn-gestionar-grupo-mode').addEventListener('click', toggleModoGestionGrupo);
}

// Cargar todos los estudiantes
async function cargarEstudiantes() {
    mostrarLoading();
    
    try {
        const response = await fetch(API_URL);
        
        if (!response.ok) {
            throw new Error('Error al cargar estudiantes');
        }
        
        const estudiantes = await response.json();
        estudiantesData = estudiantes;
        
        if (estudiantes.length === 0) {
            mostrarEstadoVacio();
        } else {
            mostrarTabla(estudiantes);
        }
    } catch (error) {
        console.error('Error:', error);
        mostrarError();
    }
}



// Aplicar filtros avanzados
async function aplicarFiltros() {
    mostrarLoading();
    
    const filtro = {
        genero: document.getElementById('filtro-genero').value || null,
        edadMinima: document.getElementById('filtro-edad-min').value ? 
                    parseInt(document.getElementById('filtro-edad-min').value) : null,
        edadMaxima: document.getElementById('filtro-edad-max').value ? 
                    parseInt(document.getElementById('filtro-edad-max').value) : null,
        ordenAlfabetico: document.getElementById('filtro-orden').value === 'true'
    };
    

}

// Limpiar filtros
function limpiarFiltros() {
    document.getElementById('filtro-genero').value = '';
    document.getElementById('filtro-edad-min').value = '';
    document.getElementById('filtro-edad-max').value = '';
    document.getElementById('filtro-orden').value = 'true';
    cargarEstudiantes();
}

// Toggle panel de filtros
function toggleFiltros() {
    const panel = document.getElementById('panel-filtros');
    if (panel.style.display === 'none') {
        panel.style.display = 'block';
    } else {
        panel.style.display = 'none';
    }
}

// Toggle modo gestión de grupos
function toggleModoGestionGrupo() {
    modoGestionGrupo = !modoGestionGrupo;
    const btn = document.getElementById('btn-gestionar-grupo-mode');
    const info = document.getElementById('modo-gestion-info');
    const thOpciones = document.getElementById('th-opciones');
    
    if (modoGestionGrupo) {
        btn.classList.add('active');
        btn.innerHTML = '<i class="fas fa-check"></i> Modo Grupos Activo';
        info.style.display = 'block';
        thOpciones.textContent = 'Gestión de Grupos';
    } else {
        btn.classList.remove('active');
        btn.innerHTML = '<i class="fas fa-users"></i> Gestionar Grupos';
        info.style.display = 'none';
        thOpciones.textContent = 'Opciones';
    }
    
    // Recargar tabla con las opciones correctas
    mostrarTabla(estudiantesData);
}

// Mostrar tabla de estudiantes
function mostrarTabla(estudiantes) {
    ocultarTodosLosEstados();
    
    const tbody = document.getElementById('tbody-estudiantes');
    tbody.innerHTML = '';
    
    estudiantes.forEach(estudiante => {
        const tr = document.createElement('tr');
        
        const estadoClass = estudiante.estado === 'Activo' ? 'estado-activo' : 'estado-inactivo';
        const grupoTexto = estudiante.grupo || 'Sin grupo';
        
        tr.innerHTML = `
            <td>${estudiante.nombre}</td>
            <td>${estudiante.apellido}</td>
            <td>${estudiante.grado || 'N/A'}</td>
            <td>${grupoTexto}</td>
            <td><span class="estado-badge ${estadoClass}">${estudiante.estado}</span></td>
            <td>
                <div class="options-cell">
                    ${generarBotonesOpciones(estudiante)}
                </div>
            </td>
        `;
        
        tbody.appendChild(tr);
    });
    
    document.getElementById('tabla-container').style.display = 'block';
}

// Generar botones de opciones según el modo
function generarBotonesOpciones(estudiante) {
    if (modoGestionGrupo) {
        return `
            <button class="btn btn-success btn-icon" onclick="asignarGrupo(${estudiante.codigoEstudiante})" title="Asignar a grupo">
                <i class="fas fa-check"></i>
            </button>
            <button class="btn btn-danger btn-icon" onclick="desvincularGrupo(${estudiante.codigoEstudiante})" title="Desvincular de grupo">
                <i class="fas fa-times"></i>
            </button>
        `;
    } else {
        return `
            <button class="btn btn-primary btn-icon" onclick="editarEstudiante(${estudiante.codigoEstudiante})" title="Editar">
                <i class="fas fa-edit"></i>
            </button>
            <button class="btn btn-danger btn-icon" onclick="confirmarEliminar(${estudiante.codigoEstudiante}, '${estudiante.nombre} ${estudiante.apellido}')" title="Eliminar">
                <i class="fas fa-trash"></i>
            </button>
        `;
    }
}

// Editar estudiante
function editarEstudiante(codigoEstudiante) {
    // Esta funcionalidad sería implementada en otro caso de uso
    mostrarMensaje('Información', 'La funcionalidad de editar estudiante se implementará en el caso de uso correspondiente', 'info');
}

// Confirmar eliminación
function confirmarEliminar(codigoEstudiante, nombreCompleto) {
        mostrarMensaje('Información', 'La funcionalidad de eliminar estudiante se implementará en el caso de uso correspondiente', 'info');

}

// Eliminar estudiante
async function eliminarEstudiante(codigoEstudiante) {
            mostrarMensaje('Información', 'La funcionalidad de eliminar estudiante se implementará en el caso de uso correspondiente', 'info');
}


// Asignar grupo (modo gestión)
async function asignarGrupo(codigoEstudiante) {
     mostrarMensaje('Información', 'La funcionalidad de asignar grupo se implementará en el caso de uso correspondiente', 'info');
}

// Desvincular grupo
async function desvincularGrupo(codigoEstudiante) {
        mostrarMensaje('Información', 'La funcionalidad de desvincular grupo se implementará en el caso de uso correspondiente', 'info');
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
    
    // Configurar icono según tipo
    icono.className = 'modal-icon';
    if (tipo === 'success') {
        icono.classList.add('success', 'fas', 'fa-check-circle');
    } else if (tipo === 'error') {
        icono.classList.add('error', 'fas', 'fa-exclamation-circle');
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