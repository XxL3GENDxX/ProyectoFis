// Variables globales
let todosLosLogros = [];
let categoriaSeleccionada = '';

// Elementos del DOM
const selectCategoria = document.getElementById('select-categoria');
const tablaLogros = document.getElementById('tabla-logros');
const tbody = document.getElementById('tbody-logros');
const modalLogro = document.getElementById('modal-logro');
const btnCrearLogro = document.getElementById('btn-crear-logro');
const btnCerrarModal = document.querySelector('#modal-logro .modal-close');
const btnGuardarLogro = document.getElementById('btn-guardar-logro');
const inputNombre = document.getElementById('logro-nombre');
const inputDescripcion = document.getElementById('logro-descripcion');
const formLogro = document.getElementById('form-logro');
const resultSection = document.getElementById('results-section');
const emptyState = document.getElementById('empty-state');
const loadingState = document.getElementById('loading-state');
const emptyLogrosState = document.getElementById('empty-logros-state');

// Event Listeners
document.addEventListener('DOMContentLoaded', () => {
    selectCategoria.addEventListener('change', () => {
        categoriaSeleccionada = selectCategoria.value;
        if (categoriaSeleccionada === '') {
            emptyState.style.display = 'block';
            resultSection.style.display = 'none';
            loadingState.style.display = 'none';
        } else {
            cargarLogros();
        }
    });

    btnCrearLogro.addEventListener('click', abrirModalCrearLogro);
    btnCerrarModal.addEventListener('click', cerrarModal.bind(null, 'modal-logro'));
    btnGuardarLogro.addEventListener('click', () => {
        const modo = formLogro.dataset.modo;
        if (modo === 'crear') {
            guardarNuevoLogro();
        } else if (modo === 'editar') {
            guardarEdicionLogro();
        }
    });

    // Cerrar modal al hacer clic fuera de él
    window.addEventListener('click', (event) => {
        if (event.target === modalLogro) {
            cerrarModal('modal-logro');
        }
    });
});

// Función para cargar los logros de la categoría seleccionada
async function cargarLogros() {
    try {
        loadingState.style.display = 'block';
        resultSection.style.display = 'none';
        emptyLogrosState.style.display = 'none';

        const response = await fetch(`/api/logro/categoria/${categoriaSeleccionada}`);
        
        if (response.status === 404 || !response.ok) {
            // Categoría sin logros
            todosLosLogros = [];
            mostrarEstadoVacio();
            loadingState.style.display = 'none';
            return;
        }

        const data = await response.json();
        todosLosLogros = Array.isArray(data) ? data : [];

        loadingState.style.display = 'none';

        if (todosLosLogros.length === 0) {
            resultSection.style.display = 'block';
            emptyLogrosState.style.display = 'block';
        } else {
            resultSection.style.display = 'block';
            emptyLogrosState.style.display = 'none';
            renderizarLogros();
        }
    } catch (error) {
        console.error('Error al cargar logros:', error);
        todosLosLogros = [];
        mostrarEstadoVacio();
        loadingState.style.display = 'none';
    }
}

// Función para mostrar estado vacío con mensaje amigable
function mostrarEstadoVacio() {
    emptyState.style.display = 'block';
    resultSection.style.display = 'none';
    loadingState.style.display = 'none';
}

// Función para mostrar la tabla
function mostrarTabla() {
    resultSection.style.display = 'block';
    emptyState.style.display = 'none';
}

// Función para renderizar los logros en la tabla
function renderizarLogros() {
    tbody.innerHTML = '';

    todosLosLogros.forEach(logro => {
        const fila = document.createElement('tr');
        fila.innerHTML = `
            <td>${logro.nombreLogro}</td>
            <td>${logro.descripcion}</td>
            <td>
                <button class="btn btn-sm btn-primary" onclick="abrirModalEditarLogro(${logro.idLogro})">
                    <i class="fas fa-edit"></i> Editar
                </button>
                <button class="btn btn-sm btn-danger" onclick="abrirModalEliminarLogro(${logro.idLogro}, '${logro.nombreLogro}')">
                    <i class="fas fa-trash"></i> Eliminar
                </button>
            </td>
        `;
        tbody.appendChild(fila);
    });
}

// Función para abrir el modal de creación
function abrirModalCrearLogro() {
    formLogro.reset();
    document.getElementById('modal-logro-titulo').textContent = 'Crear Nuevo Logro';
    formLogro.dataset.modo = 'crear';
    delete formLogro.dataset.idLogro;
    modalLogro.style.display = 'block';
    inputNombre.focus();
}

// Función para cerrar el modal de creación
function cerrarModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
    }
    if (modalId === 'modal-logro') {
        formLogro.reset();
        delete formLogro.dataset.idLogro;
    }
}

// Función para guardar un nuevo logro
async function guardarNuevoLogro() {
    const nombre = inputNombre.value.trim();
    const descripcion = inputDescripcion.value.trim();

    if (!nombre) {
        mostrarMensajeModal('warning', 'Advertencia', 'El nombre del logro es requerido');
        return;
    }

    if (!descripcion) {
        mostrarMensajeModal('warning', 'Advertencia', 'La descripción del logro es requerida');
        return;
    }

    if (!categoriaSeleccionada) {
        mostrarMensajeModal('warning', 'Advertencia', 'Debe seleccionar una categoría');
        return;
    }

    try {
        const nuevoLogro = {
            nombreLogro: nombre,
            descripcion: descripcion,
            categoriaLogro: categoriaSeleccionada,
            estado: true
        };

        const response = await fetch('/api/logro/crear', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(nuevoLogro)
        });

        if (!response.ok) {
            const errorData = await response.json();
            mostrarMensajeModal('error', 'Error', errorData.mensaje || 'Error al crear el logro');
            return;
        }

        cerrarModal('modal-logro');
        mostrarMensajeModal('success', 'Éxito', 'Logro creado correctamente');
        cargarLogros();
    } catch (error) {
        console.error('Error al guardar logro:', error);
        mostrarMensajeModal('error', 'Error', 'Error al crear el logro: ' + error.message);
    }
}

// Función para abrir modal de edición de logro
function abrirModalEditarLogro(idLogro) {
    const logro = todosLosLogros.find(l => l.idLogro === idLogro);
    
    if (logro) {
        inputNombre.value = logro.nombreLogro;
        inputDescripcion.value = logro.descripcion;
        document.getElementById('modal-logro-titulo').textContent = 'Editar Logro';
        formLogro.dataset.modo = 'editar';
        formLogro.dataset.idLogro = idLogro;
        modalLogro.style.display = 'block';
        inputNombre.focus();
    }
}

// Función para abrir modal de confirmación de eliminación
function abrirModalEliminarLogro(idLogro, nombreLogro) {
    document.getElementById('nombre-logro-eliminar').textContent = nombreLogro;
    const btnConfirmarEliminar = document.getElementById('btn-confirmar-eliminar-logro');
    btnConfirmarEliminar.onclick = () => eliminarLogro(idLogro);
    document.getElementById('modal-confirmar-eliminar').style.display = 'block';
}

// Función para eliminar un logro
async function eliminarLogro(idLogro) {
    try {
        const response = await fetch(`/api/logro/${idLogro}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorData = await response.json();
            mostrarMensajeModal('error', 'Error', errorData.mensaje || 'Error al eliminar el logro');
            return;
        }

        cerrarModal('modal-confirmar-eliminar');
        mostrarMensajeModal('success', 'Éxito', 'Logro eliminado correctamente');
        cargarLogros();
    } catch (error) {
        console.error('Error al eliminar logro:', error);
        mostrarMensajeModal('error', 'Error', 'Error al eliminar el logro: ' + error.message);
    }
}

// Función para guardar la edición de un logro
async function guardarEdicionLogro() {
    const idLogro = formLogro.dataset.idLogro;
    const nombre = inputNombre.value.trim();
    const descripcion = inputDescripcion.value.trim();

    if (!nombre) {
        mostrarMensajeModal('warning', 'Advertencia', 'El nombre del logro es requerido');
        return;
    }

    if (!descripcion) {
        mostrarMensajeModal('warning', 'Advertencia', 'La descripción del logro es requerida');
        return;
    }

    try {
        const logroActualizado = {
            nombreLogro: nombre,
            descripcion: descripcion,
            categoriaLogro: categoriaSeleccionada
        };

        const response = await fetch(`/api/logro/${idLogro}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(logroActualizado)
        });

        if (!response.ok) {
            const errorData = await response.json();
            mostrarMensajeModal('error', 'Error', errorData.mensaje || 'Error al actualizar el logro');
            return;
        }

        cerrarModal('modal-logro');
        mostrarMensajeModal('success', 'Éxito', 'Logro actualizado correctamente');
        cargarLogros();
    } catch (error) {
        console.error('Error al actualizar logro:', error);
        mostrarMensajeModal('error', 'Error', 'Error al actualizar el logro: ' + error.message);
    }
}
function mostrarMensajeModal(tipo, titulo, mensaje) {
    const modal = document.getElementById('modal-mensaje');
    const icono = document.getElementById('modal-mensaje-icono');
    const tituloEl = document.getElementById('modal-mensaje-titulo');
    const textoEl = document.getElementById('modal-mensaje-texto');

    // Configurar icono según tipo
    icono.className = '';
    if (tipo === 'success') {
        icono.className = 'fas fa-check-circle modal-icon modal-icon-success';
    } else if (tipo === 'error') {
        icono.className = 'fas fa-exclamation-circle modal-icon modal-icon-error';
    } else if (tipo === 'warning') {
        icono.className = 'fas fa-exclamation-triangle modal-icon modal-icon-warning';
    }

    tituloEl.textContent = titulo;
    textoEl.textContent = mensaje;
    modal.style.display = 'block';
}

// Función para mostrar mensajes
function mostrarMensaje(tipo, titulo, mensaje) {
    // Crear elemento de alerta
    const alerta = document.createElement('div');
    alerta.className = `alerta alerta-${tipo}`;
    alerta.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background-color: ${tipo === 'success' ? '#d4edda' : tipo === 'warning' ? '#fff3cd' : '#f8d7da'};
        color: ${tipo === 'success' ? '#155724' : tipo === 'warning' ? '#856404' : '#721c24'};
        border: 1px solid ${tipo === 'success' ? '#c3e6cb' : tipo === 'warning' ? '#ffeaa7' : '#f5c6cb'};
        border-radius: 4px;
        padding: 15px 20px;
        z-index: 10000;
        font-weight: 500;
        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    `;
    alerta.innerHTML = `<strong>${titulo}:</strong> ${mensaje}`;
    document.body.appendChild(alerta);

    // Remover después de 4 segundos
    setTimeout(() => {
        alerta.remove();
    }, 4000);
}

// Función para toggle sidebar (si es necesaria)
function toggleSidebar() {
    const sidebar = document.querySelector('.sidebar');
    if (sidebar) {
        sidebar.classList.toggle('active');
    }
}
