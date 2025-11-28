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
document.addEventListener('DOMContentLoaded', function() {
    inicializarEventos();
    cargarUsuarios();
});

// Inicializar eventos
function inicializarEventos() {
    document.getElementById('txt-buscar-usuario').addEventListener('input', function(e) {
        clearTimeout(timeoutBusqueda);
        timeoutBusqueda = setTimeout(() => {
            filtrarUsuarios(e.target.value);
        }, 300);
    });

    document.getElementById('btn-crear-usuario').addEventListener('click', abrirModalCrear);
    document.getElementById('btn-confirmar-crear').addEventListener('click', confirmarCrearUsuario);
    document.getElementById('btn-confirmar-editar').addEventListener('click', confirmarEditarUsuario);
    document.getElementById('btn-confirmar-password').addEventListener('click', confirmarCambiarPassword);
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
                    <button class="btn btn-warning btn-icon" onclick="cambiarPassword(${usuario.idTokenUsuario}, '${usuario.nombreUsuario}')" title="Cambiar contraseña">
                        <i class="fas fa-key"></i>
                    </button>
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
    abrirModal('modal-crear-usuario');
}

// Confirmar creación de usuario
async function confirmarCrearUsuario() {
    const nombreCompleto = document.getElementById('crear-nombre-completo').value.trim();
    const nombreUsuario = document.getElementById('crear-nombre-usuario').value.trim();
    const contrasena = document.getElementById('crear-contrasena').value;
    const rol = document.getElementById('crear-rol').value;
    const estado = document.getElementById('crear-estado').value === 'Activo';
    
    if (!nombreCompleto || !nombreUsuario || !contrasena || !rol) {
        mostrarMensaje('Advertencia', 'Por favor complete todos los campos obligatorios', 'warning');
        return;
    }
    
    if (contrasena.length < 6) {
        mostrarMensaje('Advertencia', 'La contraseña debe tener al menos 6 caracteres', 'warning');
        return;
    }
    
    // Dividir nombre completo en nombre y apellido
    const partes = nombreCompleto.split(' ');
    const nombre = partes[0];
    const apellido = partes.slice(1).join(' ') || partes[0];
    
    try {
        // 1. Crear persona primero
        const personaData = {
            nombre: nombre,
            apellido: apellido,
            documento: nombreUsuario // Usar el nombre de usuario como documento temporal
        };
        
        const personaResponse = await fetch('http://localhost:8080/api/persona/crear', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(personaData)
        });
        
        if (!personaResponse.ok) {
            throw new Error('Error al crear la persona');
        }
        
        const personaCreada = await personaResponse.json();
        
        // 2. Crear usuario con el idPersona
        const usuarioData = {
            nombreUsuario: nombreUsuario,
            contrasena: contrasena,
            estado: estado,
            rol: rol,
            persona: {
                idPersona: personaCreada.idPersona
            }
        };
        
        const response = await fetch(`${API_URL}/crear`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(usuarioData)
        });
        
        if (response.ok) {
            cerrarModal('modal-crear-usuario');
            mostrarMensaje('Éxito', 'Usuario creado exitosamente', 'success');
            cargarUsuarios();
        } else {
            const data = await response.json();
            mostrarMensaje('Error', data.mensaje || 'Error al crear el usuario', 'error');
        }
        
    } catch (error) {
        console.error('Error al crear usuario:', error);
        mostrarMensaje('Error', 'Error en la base de datos', 'error');
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
    const nombreCompleto = usuario.persona ? 
        `${usuario.persona.nombre} ${usuario.persona.apellido}` : '';
    
    document.getElementById('editar-nombre-completo').value = nombreCompleto;
    document.getElementById('editar-nombre-usuario').value = usuario.nombreUsuario || '';
    document.getElementById('editar-contrasena').value = '';
    document.getElementById('editar-rol').value = usuario.rol || '';
    document.getElementById('editar-estado').value = usuario.estado ? 'Activo' : 'Inactivo';
}

// Confirmar edición de usuario
async function confirmarEditarUsuario() {
    const nombreCompleto = document.getElementById('editar-nombre-completo').value.trim();
    const nombreUsuario = document.getElementById('editar-nombre-usuario').value.trim();
    const contrasena = document.getElementById('editar-contrasena').value;
    const rol = document.getElementById('editar-rol').value;
    const estado = document.getElementById('editar-estado').value === 'Activo';
    
    if (!nombreCompleto || !nombreUsuario || !rol) {
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

// Cambiar contraseña
function cambiarPassword(idUsuario, nombreUsuario) {
    usuarioSeleccionado = idUsuario;
    document.getElementById('usuario-cambiar-password').textContent = nombreUsuario;
    document.getElementById('nueva-contrasena').value = '';
    abrirModal('modal-cambiar-password');
}

// Confirmar cambio de contraseña
async function confirmarCambiarPassword() {
    const nuevaContrasena = document.getElementById('nueva-contrasena').value;
    
    if (!nuevaContrasena) {
        mostrarMensaje('Advertencia', 'Por favor ingrese una contraseña', 'warning');
        return;
    }
    
    if (nuevaContrasena.length < 6) {
        mostrarMensaje('Advertencia', 'La contraseña debe tener al menos 6 caracteres', 'warning');
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/${usuarioSeleccionado}/cambiar-password`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ contrasena: nuevaContrasena })
        });
        
        if (response.ok) {
            cerrarModal('modal-cambiar-password');
            mostrarMensaje('Éxito', 'Contraseña cambiada exitosamente', 'success');
        } else {
            const data = await response.json();
            mostrarMensaje('Error', data.mensaje || 'Error al cambiar la contraseña', 'error');
        }
        
    } catch (error) {
        console.error('Error al cambiar contraseña:', error);
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