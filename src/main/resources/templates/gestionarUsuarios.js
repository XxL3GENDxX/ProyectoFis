function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');

    sidebar.classList.toggle('active');
    overlay.classList.toggle('active');
}

// Configuración de la API
const API_URL = 'http://localhost:8080/api/token_usuario';

// Estado global
let usuariosData = [];
let timeoutBusqueda = null;
let usuarioSeleccionado = null;

// Inicialización
document.addEventListener('DOMContentLoaded', function () {
    inicializarEventos();
    cargarUsuarios();
    // Cargar datos del usuario después de inicializar los eventos
    setTimeout(() => {
        if (typeof cargarDatosUsuario === 'function') {
            cargarDatosUsuario();
        }
    }, 100);
});

// Inicializar eventos
function inicializarEventos() {
    document.getElementById('txt-buscar-usuario').addEventListener('input', function (e) {
        clearTimeout(timeoutBusqueda);
        timeoutBusqueda = setTimeout(() => {
            filtrarUsuarios(e.target.value);
        }, 300);
    });

    document.getElementById('btn-crear-usuario').addEventListener('click', abrirModalCrear);
    document.getElementById('btn-confirmar-crear').addEventListener('click', confirmarCrearUsuario);
    document.getElementById('btn-confirmar-editar').addEventListener('click', confirmarEditarUsuario);

    // Listener para mostrar campos según el rol
    document.getElementById('sel-rol-crear').addEventListener('change', function (e) {
        const rol = e.target.value;
        const contenedorPersonaExtra = document.getElementById('datos-persona-extra');
        const grupoDocumento = document.getElementById('grupo-documento');
        const camposUsuario = document.getElementById('campos-usuario-crear');
        const helpDocumento = document.getElementById('help-documento');

        // Reset display
        grupoDocumento.style.display = 'none';
        contenedorPersonaExtra.style.display = 'none';
        camposUsuario.style.display = 'none';

        if (!rol) return;

        // Mostrar campos comunes
        grupoDocumento.style.display = 'block';
        camposUsuario.style.display = 'block';

        if (rol === 'Acudiente') {
            // Caso Acudiente: Solo linkear por documento
            helpDocumento.textContent = 'El documento debe corresponder a un acudiente existente.';
        } else if (rol === 'Profesor' || rol === 'Administrador') {
            // Caso Profesor/Admin: Permitir crear persona
            contenedorPersonaExtra.style.display = 'block';
            helpDocumento.textContent = 'Si la persona no existe, se creará con los datos ingresados a continuación.';
        }
    });
}

// Cargar todos los usuarios
async function cargarUsuarios() {
    mostrarLoading();

    try {
        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error('Error al cargar usuarios');
        }

        const usuarios = await response.json();
        usuariosData = usuarios;

        if (usuarios.length === 0) {
            mostrarEstadoVacio();
        } else {
            mostrarTabla(usuarios);
        }
    } catch (error) {
        console.error('Error:', error);
        mostrarError();
    }
}

// Filtrar usuarios localmente
function filtrarUsuarios(texto) {
    if (!texto || texto.trim() === '') {
        mostrarTabla(usuariosData);
        return;
    }

    const textoLower = texto.toLowerCase().trim();
    const usuariosFiltrados = usuariosData.filter(usuario => {
        const nombreCompleto = `${usuario.persona?.nombre || ''} ${usuario.persona?.apellido || ''}`.toLowerCase();
        const nombreUsuario = (usuario.nombreUsuario || '').toLowerCase();
        const rol = (usuario.rol || '').toLowerCase();

        return nombreCompleto.includes(textoLower) ||
            nombreUsuario.includes(textoLower) ||
            rol.includes(textoLower);
    });

    mostrarTabla(usuariosFiltrados);
}

// Mostrar tabla de usuarios
function mostrarTabla(usuarios) {
    ocultarTodosLosEstados();

    const tbody = document.getElementById('tbody-usuarios');
    tbody.innerHTML = '';

    if (usuarios.length === 0) {
        mostrarEstadoVacio();
        return;
    }

    usuarios.forEach(usuario => {
        const tr = document.createElement('tr');

        const estadoClass = usuario.estado ? 'estado-activo' : 'estado-inactivo';
        const estadoTexto = usuario.estado ? 'Activo' : 'Inactivo';
        const nombreCompleto = usuario.persona ?
            `${usuario.persona.nombre} ${usuario.persona.apellido}` :
            'Sin nombre';

        tr.innerHTML = `
            <td>${nombreCompleto}</td>
            <td>${usuario.nombreUsuario}</td>
            <td><span class="estado-badge ${estadoClass}">${estadoTexto}</span></td>
            <td>${usuario.rol}</td>
            <td>
                <div class="options-cell">
                    <button class="btn btn-primary btn-icon" onclick="editarUsuario(${usuario.idTokenUsuario})" title="Editar">
                        <i class="fas fa-edit"></i>
                    </button>
                    <label class="switch-estado" title="Cambiar estado">
                        <input type="checkbox" ${usuario.estado ? 'checked' : ''} 
                            onchange="cambiarEstadoUsuario(${usuario.idTokenUsuario})">
                        <span class="slider"></span>
                    </label>
                </div>
            </td>
        `;

        tbody.appendChild(tr);
    });

    document.getElementById('tabla-container').style.display = 'block';
}

// Abrir modal de crear usuario
function abrirModalCrear() {
    document.getElementById('form-crear-usuario').reset();

    // Resetear visibilidad
    document.getElementById('grupo-documento').style.display = 'none';
    document.getElementById('datos-persona-extra').style.display = 'none';
    document.getElementById('campos-usuario-crear').style.display = 'none';
    document.getElementById('help-documento').textContent = '';

    abrirModal('modal-crear-usuario');
}

async function confirmarCrearUsuario() {
    // 1. Obtener referencias a los elementos del DOM
    const inputDocumento = document.getElementById('txt-documento-crear');
    const inputUsuario = document.getElementById('txt-usuario-crear');
    const inputPassword = document.getElementById('txt-password-crear');
    const selectRol = document.getElementById('sel-rol-crear');

    // 2. Extraer valores
    const documento = inputDocumento.value.trim();
    const nombreUsuario = inputUsuario.value.trim();
    const contrasena = inputPassword.value.trim();
    const rol = selectRol.value;

    // 3. Validar que no estén vacíos
    if (!documento) {
        mostrarMensaje('Advertencia', 'Por favor ingrese el documento de la persona', 'warning');
        return;
    }
    if (!nombreUsuario) {
        mostrarMensaje('Advertencia', 'Por favor ingrese un nombre de usuario', 'warning');
        return;
    }
    if (!contrasena) {
        mostrarMensaje('Advertencia', 'Por favor ingrese una contraseña', 'warning');
        return;
    }

    // Recoger datos de persona extra
    const nombre = document.getElementById('txt-nombre-crear').value.trim();
    const apellido = document.getElementById('txt-apellido-crear').value.trim();
    const fechaNacimiento = document.getElementById('txt-fecha-nacimiento-crear').value;
    const genero = document.getElementById('sel-genero-crear').value;

    // Validación condicional para Profesor y Administrador
    if (rol === 'Profesor' || rol === 'Administrador') {
        if (!nombre || !apellido) {
            mostrarMensaje('Advertencia', 'Para crear un ' + rol + ', Nombre y Apellido son obligatorios.', 'warning');
            return;
        }
    }

    // Formatear fecha si existe
    let fechaNacimientoISO = null;
    if (fechaNacimiento) {
        fechaNacimientoISO = fechaNacimiento + "T00:00:00";
    }

    // 4. Construir el objeto JSON (Estructura anidada para evitar DTOs en Java)
    const datos = {
        nombreUsuario: nombreUsuario,
        contrasena: contrasena,
        rol: rol,
        estado: true,
        persona: {
            documento: documento,
            // Solo enviar datos extra si el rol lo requiere (Admin/Profesor)
            // Si es Acudiente, estos campos irán vacíos o null, y el backend buscará solo por documento
            nombre: (rol !== 'Acudiente') ? nombre : null,
            apellido: (rol !== 'Acudiente') ? apellido : null,
            fechaDeNacimiento: (rol !== 'Acudiente') ? fechaNacimientoISO : null,
            genero: (rol !== 'Acudiente') ? genero : null
        }
    };

    try {
        // Mostrar estado de carga
        const btnConfirmar = document.getElementById('btn-confirmar-crear');
        const textoOriginal = btnConfirmar.innerHTML;
        btnConfirmar.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creando...';
        btnConfirmar.disabled = true;

        // 5. Enviar petición al Backend
        const respuesta = await fetch(API_URL + '/crear', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(datos)
        });

        const resultado = await respuesta.json();

        // 6. Procesar respuesta
        if (respuesta.ok) {
            mostrarMensaje('Éxito', 'Usuario creado y asociado correctamente', 'success');
            cerrarModal('modal-crear-usuario');

            // Limpiar formulario
            inputDocumento.value = '';
            inputUsuario.value = '';
            inputPassword.value = '';
            selectRol.value = ''; // Resetear select

            // Limpiar campos extra y ocultar secciones
            document.getElementById('txt-nombre-crear').value = '';
            document.getElementById('txt-apellido-crear').value = '';
            document.getElementById('txt-fecha-nacimiento-crear').value = '';
            document.getElementById('sel-genero-crear').value = '';

            // Ocultar todo de nuevo
            document.getElementById('datos-persona-extra').style.display = 'none';
            document.getElementById('grupo-documento').style.display = 'none';
            document.getElementById('campos-usuario-crear').style.display = 'none';

            cargarUsuarios(); // Recargar la tabla
        } else {
            mostrarMensaje('Error', resultado.mensaje || 'No se pudo crear el usuario', 'error');
        }

    } catch (error) {
        console.error('Error:', error);
        mostrarMensaje('Error', 'Error de conexión con el servidor', 'error');
    } finally {
        // Restaurar botón
        const btnConfirmar = document.getElementById('btn-confirmar-crear');
        btnConfirmar.innerHTML = '<i class="fas fa-save"></i> Guardar';
        btnConfirmar.disabled = false;
    }
}

// Editar usuario
async function editarUsuario(idUsuario) {
    try {
        const response = await fetch(`${API_URL}/${idUsuario}`);

        if (!response.ok) {
            throw new Error('Error al cargar datos del usuario');
        }

        const usuario = await response.json();
        cargarDatosEnFormularioEditar(usuario);
        usuarioSeleccionado = idUsuario;
        abrirModal('modal-editar-usuario');

    } catch (error) {
        console.error('Error al cargar usuario:', error);
        mostrarMensaje('Error', 'Error al cargar los datos del usuario', 'error');
    }
}

function cargarDatosEnFormularioEditar(usuario) {
    document.getElementById('editar-nombre-usuario').value = usuario.nombreUsuario || '';
    document.getElementById('editar-contrasena').value = '';
    document.getElementById('editar-rol').value = usuario.rol || '';
    document.getElementById('editar-estado').value = usuario.estado ? 'Activo' : 'Inactivo';
}

// Confirmar edición de usuario
async function confirmarEditarUsuario() {
    const nombreUsuario = document.getElementById('editar-nombre-usuario').value.trim();
    const contrasena = document.getElementById('editar-contrasena').value;
    const rol = document.getElementById('editar-rol').value;
    const estado = document.getElementById('editar-estado').value === 'Activo';

    if (!nombreUsuario || !rol) {
        mostrarMensaje('Advertencia', 'Por favor complete todos los campos obligatorios', 'warning');
        return;
    }

    if (contrasena && contrasena.length < 6) {
        mostrarMensaje('Advertencia', 'La contraseña debe tener al menos 6 caracteres', 'warning');
        return;
    }

    const usuarioData = {
        nombreUsuario: nombreUsuario,
        estado: estado,
        rol: rol
    };

    // Solo incluir contraseña si se ingresó una nueva
    if (contrasena) {
        usuarioData.contrasena = contrasena;
    }

    try {
        const response = await fetch(`${API_URL}/${usuarioSeleccionado}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(usuarioData)
        });

        if (response.ok) {
            cerrarModal('modal-editar-usuario');
            mostrarMensaje('Éxito', 'Usuario modificado exitosamente', 'success');
            cargarUsuarios();
        } else {
            const data = await response.json();
            mostrarMensaje('Error', data.mensaje || 'Error al modificar el usuario', 'error');
        }

    } catch (error) {
        console.error('Error al modificar usuario:', error);
        mostrarMensaje('Error', 'Error en la base de datos', 'error');
    }
}

// Cambiar estado del usuario
async function cambiarEstadoUsuario(idUsuario) {
    try {
        const response = await fetch(`${API_URL}/${idUsuario}/cambiar-estado`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' }
        });

        if (response.ok) {
            mostrarMensaje('Éxito', 'Estado modificado exitosamente', 'success');
            cargarUsuarios();
        } else {
            const data = await response.json();
            mostrarMensaje('Error', data.mensaje || 'Error al cambiar el estado', 'error');
            cargarUsuarios(); // Recargar para revertir el switch
        }

    } catch (error) {
        console.error('Error al cambiar estado:', error);
        mostrarMensaje('Error', 'Error en la base de datos', 'error');
        cargarUsuarios(); // Recargar para revertir el switch
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

window.onclick = function (event) {
    if (event.target.classList.contains('modal')) {
        event.target.classList.remove('show');
    }
}