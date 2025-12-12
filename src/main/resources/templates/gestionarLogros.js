// Toggle sidebar
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');
    
    sidebar.classList.toggle('active');
    overlay.classList.toggle('active');
}

// Configuración de la API
const API_URL = 'http://localhost:8080/api';
const API_LOGROS_URL = `${API_URL}/logros`;

// Estado global
let categoriaActual = '';
let logroSeleccionado = null;
let modoEdicion = false;

// Mapeo de nombres de categorías en español
const CATEGORIAS_NOMBRES = {
    'psicosociales': 'Psicosociales',
    'academicos': 'Académicos',
    'deportivos': 'Deportivos',
    'artisticos': 'Artísticos',
    'culturales': 'Culturales'
};

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    inicializarEventos();
});

// Inicializar eventos
function inicializarEventos() {
    // Selector de categoría
    document.getElementById('select-categoria').addEventListener('change', function(e) {
        const categoria = e.target.value;
        if (categoria) {
            categoriaActual = categoria;
            cargarLogrosPorCategoria(categoria);
        } else {
            mostrarEstadoVacio();
        }
    });

    // Botones principales
    document.getElementById('btn-crear-logro').addEventListener('click', abrirModalCrearLogro);
    document.getElementById('btn-guardar-logro').addEventListener('click', guardarLogro);
    document.getElementById('btn-confirmar-eliminar-logro').addEventListener('click', eliminarLogro);
}

// ========== CARGAR LOGROS ==========

// Cargar logros por categoría
async function cargarLogrosPorCategoria(categoria) {
    mostrarLoading();

    try {
        const response = await fetch(`${API_LOGROS_URL}/categoria/${categoria}`);
        
        if (!response.ok) {
            if (response.status === 404) {
                // No hay logros en esta categoría
                mostrarResultadosVacios(categoria);
                return;
            }
            throw new Error('Error al cargar logros');
        }

        const logros = await response.json();
        mostrarResultados(categoria, logros);

    } catch (error) {
        console.error('Error al cargar logros:', error);
        mostrarMensaje('Error', 'Error al cargar los logros', 'error');
        mostrarEstadoVacio();
    }
}

// ========== MOSTRAR RESULTADOS ==========

// Mostrar resultados con logros
function mostrarResultados(categoria, logros) {
    ocultarTodosLosEstados();

    const resultsSection = document.getElementById('results-section');
    const categoriaNombre = document.getElementById('categoria-nombre');
    const tbody = document.getElementById('tbody-logros');
    const emptyLogrosState = document.getElementById('empty-logros-state');
    const tablaLogros = document.getElementById('tabla-logros');

    // Actualizar título
    categoriaNombre.textContent = CATEGORIAS_NOMBRES[categoria] || categoria;

    // Limpiar tabla
    tbody.innerHTML = '';

    if (logros.length === 0) {
        tablaLogros.style.display = 'none';
        emptyLogrosState.style.display = 'block';
    } else {
        tablaLogros.style.display = 'table';
        emptyLogrosState.style.display = 'none';

        logros.forEach(logro => {
            const tr = document.createElement('tr');
            
            tr.innerHTML = `
                <td>${logro.nombreLogro}</td>
                <td>${logro.descripcion}</td>
                <td>
                    <div class="actions-cell">
                        <button class="btn btn-secondary btn-icon" 
                                onclick="editarLogro(${logro.idLogro})" 
                                title="Editar logro">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-danger btn-icon" 
                                onclick="confirmarEliminarLogro(${logro.idLogro}, '${logro.nombreLogro.replace(/'/g, "\\'")}')" 
                                title="Eliminar logro">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </td>
            `;

            tbody.appendChild(tr);
        });
    }

    resultsSection.style.display = 'block';
}

// Mostrar resultados vacíos
function mostrarResultadosVacios(categoria) {
    ocultarTodosLosEstados();

    const resultsSection = document.getElementById('results-section');
    const categoriaNombre = document.getElementById('categoria-nombre');
    const emptyLogrosState = document.getElementById('empty-logros-state');
    const tablaLogros = document.getElementById('tabla-logros');

    categoriaNombre.textContent = CATEGORIAS_NOMBRES[categoria] || categoria;
    
    tablaLogros.style.display = 'none';
    emptyLogrosState.style.display = 'block';
    resultsSection.style.display = 'block';
}

// ========== CREAR LOGRO ==========

// Abrir modal para crear logro
function abrirModalCrearLogro() {
    if (!categoriaActual) {
        mostrarMensaje('Advertencia', 'Por favor seleccione una categoría primero', 'warning');
        return;
    }

    modoEdicion = false;
    logroSeleccionado = null;
    
    document.getElementById('modal-logro-titulo').innerHTML = '<i class="fas fa-plus-circle"></i> Crear Nuevo Logro';
    document.getElementById('form-logro').reset();
    
    abrirModal('modal-logro');
}

// ========== EDITAR LOGRO ==========

// Editar logro existente
async function editarLogro(idLogro) {
    try {
        const response = await fetch(`${API_LOGROS_URL}/${idLogro}`);
        
        if (!response.ok) {
            throw new Error('Error al cargar logro');
        }

        const logro = await response.json();
        logroSeleccionado = logro;
        modoEdicion = true;

        document.getElementById('modal-logro-titulo').innerHTML = '<i class="fas fa-edit"></i> Editar Logro';
        document.getElementById('logro-nombre').value = logro.nombreLogro;
        document.getElementById('logro-descripcion').value = logro.descripcion;

        abrirModal('modal-logro');

    } catch (error) {
        console.error('Error al cargar logro:', error);
        mostrarMensaje('Error', 'Error al cargar los datos del logro', 'error');
    }
}

// ========== GUARDAR LOGRO ==========

// Guardar logro (crear o editar)
async function guardarLogro() {
    const nombreLogro = document.getElementById('logro-nombre').value.trim();
    const descripcion = document.getElementById('logro-descripcion').value.trim();

    // Validaciones
    if (!nombreLogro) {
        mostrarMensaje('Advertencia', 'El nombre del logro es obligatorio', 'warning');
        return;
    }

    if (!descripcion) {
        mostrarMensaje('Advertencia', 'La descripción es obligatoria', 'warning');
        return;
    }

    if (nombreLogro.length > 200) {
        mostrarMensaje('Advertencia', 'El nombre del logro no puede exceder 200 caracteres', 'warning');
        return;
    }

    if (descripcion.length > 500) {
        mostrarMensaje('Advertencia', 'La descripción no puede exceder 500 caracteres', 'warning');
        return;
    }

    const logroData = {
        nombreLogro: nombreLogro,
        descripcion: descripcion,
        categoria: categoriaActual
    };

    try {
        let response;

        if (modoEdicion && logroSeleccionado) {
            // Actualizar logro existente
            response = await fetch(`${API_LOGROS_URL}/${logroSeleccionado.idLogro}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(logroData)
            });
        } else {
            // Crear nuevo logro
            response = await fetch(API_LOGROS_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(logroData)
            });
        }

        if (response.ok) {
            cerrarModal('modal-logro');
            mostrarMensaje('Éxito', 
                modoEdicion ? 'Logro actualizado exitosamente' : 'Logro creado exitosamente', 
                'success');
            
            // Recargar logros de la categoría actual
            cargarLogrosPorCategoria(categoriaActual);
        } else {
            const errorData = await response.json();
            mostrarMensaje('Error', errorData.mensaje || 'Error al guardar el logro', 'error');
        }

    } catch (error) {
        console.error('Error al guardar logro:', error);
        cerrarModal('modal-logro');
        mostrarMensaje('Error', 'Error al comunicarse con el servidor', 'error');
    }
}

// ========== ELIMINAR LOGRO ==========

// Confirmar eliminación de logro
function confirmarEliminarLogro(idLogro, nombreLogro) {
    logroSeleccionado = { idLogro: idLogro };
    document.getElementById('nombre-logro-eliminar').textContent = nombreLogro;
    abrirModal('modal-confirmar-eliminar');
}

// Eliminar logro
async function eliminarLogro() {
    if (!logroSeleccionado) {
        return;
    }

    try {
        const response = await fetch(`${API_LOGROS_URL}/${logroSeleccionado.idLogro}`, {
            method: 'DELETE'
        });

        cerrarModal('modal-confirmar-eliminar');

        if (response.ok || response.status === 204) {
            mostrarMensaje('Éxito', 'Logro eliminado exitosamente', 'success');
            logroSeleccionado = null;
            
            // Recargar logros de la categoría actual
            cargarLogrosPorCategoria(categoriaActual);
        } else {
            const errorData = await response.json();
            mostrarMensaje('Error', errorData.mensaje || 'Error al eliminar el logro', 'error');
        }

    } catch (error) {
        console.error('Error al eliminar logro:', error);
        cerrarModal('modal-confirmar-eliminar');
        mostrarMensaje('Error', 'Error al comunicarse con el servidor', 'error');
    }
}

// ========== FUNCIONES DE UI ==========

// Mostrar estado vacío
function mostrarEstadoVacio() {
    ocultarTodosLosEstados();
    document.getElementById('empty-state').style.display = 'block';
}

// Mostrar loading
function mostrarLoading() {
    ocultarTodosLosEstados();
    document.getElementById('loading-state').style.display = 'block';
}

// Ocultar todos los estados
function ocultarTodosLosEstados() {
    document.getElementById('empty-state').style.display = 'none';
    document.getElementById('loading-state').style.display = 'none';
    document.getElementById('results-section').style.display = 'none';
}

// Abrir modal
function abrirModal(idModal) {
    document.getElementById(idModal).classList.add('show');
}

// Cerrar modal
function cerrarModal(idModal) {
    document.getElementById(idModal).classList.remove('show');
}

// Mostrar mensaje
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