/**
 * sidebarCommon.js
 * Lógica común para el manejo del sidebar en todos los módulos
 * Debe ser incluido en TODOS los archivos HTML que tengan sidebar
 */

// Definición de módulos por rol
const MODULOS_POR_ROL = {
    'Administrador': [
        'gestionarEstudiantes.html',
        'gestionarGrupos.html',
        'gestionarLogros.html',
        'gestionarUsuarios.html',
        'gestionarPreinscripciones.html'
    ],
    'Profesor': [
        'gestionarGrupos.html',
        'gestionarCalificaciones.html'
    ],
    'Acudiente': [
        'gestionarCalificaciones.html'
    ]
};

/**
 * Toggle sidebar para móvil
 */
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');

    if (sidebar && overlay) {
        sidebar.classList.toggle('active');
        overlay.classList.toggle('active');
    }
}

/**
 * Filtrar y mostrar solo los módulos permitidos según el rol del usuario
 */
function filtrarModulosPorRol() {
    const rolUsuario = localStorage.getItem('rolUsuario');

    // Si no hay rol definido, redirigir a login
    if (!rolUsuario) {
        console.warn('No se encontró rol de usuario, redirigiendo a login...');
        window.location.href = 'inicioSesion.html';
        return;
    }

    const modulosPermitidos = MODULOS_POR_ROL[rolUsuario] || [];
    const menuItems = document.querySelectorAll('.menu-item');

    menuItems.forEach(item => {
        const href = item.getAttribute('href');

        // No filtrar el enlace "Inicio" ni "Cerrar Sesión"
        if (!href || href === '#' || href.includes('panelGestion.html') || href.includes('inicioSesion.html')) {
            return;
        }

        // Verificar si el módulo está permitido para este rol
        const tienePermiso = modulosPermitidos.some(modulo => href.includes(modulo));

        if (tienePermiso) {
            item.style.display = 'flex';
        } else {
            item.style.display = 'none';
        }
    });

    console.log(`Módulos filtrados para rol: ${rolUsuario}`);
}

/**
 * Actualizar el item activo del menú según la página actual
 */
function actualizarMenuActivo() {
    const currentUrl = window.location.href;
    const menuItems = document.querySelectorAll('.menu-item');

    // Remover active de todos
    menuItems.forEach(item => item.classList.remove('active'));

    // Agregar active al que coincida con la URL actual
    menuItems.forEach(item => {
        const href = item.getAttribute('href');

        if (href && href !== '#' && href !== 'inicioSesion.html' && currentUrl.includes(href)) {
            item.classList.add('active');
        }
    });

    // Si estamos en panelGestion y ninguno está activo, activar "Inicio"
    if (!document.querySelector('.menu-item.active') && currentUrl.includes('panelGestion.html')) {
        const inicioItem = document.querySelector('.menu-item[href="#"]') || menuItems[0];
        if (inicioItem) {
            inicioItem.classList.add('active');
        }
    }
}

/**
 * Cargar datos del usuario en el sidebar
 */
function cargarDatosUsuario() {
    const nombreUsuario = localStorage.getItem('nombreUsuario') || 'Usuario';
    const rolUsuario = localStorage.getItem('rolUsuario') || 'Sin rol';

    const userNameElement = document.querySelector('.user-name');
    const userRoleElement = document.querySelector('.user-role');

    if (userNameElement) {
        userNameElement.textContent = nombreUsuario;
    }

    if (userRoleElement) {
        userRoleElement.textContent = rolUsuario;
    }
}

/**
 * Configurar eventos de los items del menú
 */
function configurarEventosMenu() {
    const menuItems = document.querySelectorAll('.menu-item');

    menuItems.forEach(item => {
        item.addEventListener('click', (e) => {
            const href = item.getAttribute('href');

            // Si es un enlace real a otra página, dejar que navegue normalmente
            if (href && (href.startsWith('http') || href.endsWith('.html'))) {
                return;
            }

            // Para enlaces internos (#), actualizar el estado activo
            menuItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');

            // Cerrar sidebar en móvil después de hacer click
            if (window.innerWidth <= 768) {
                toggleSidebar();
            }
        });
    });
}

/**
 * Ajustar sidebar al redimensionar ventana
 */
function configurarResponsive() {
    window.addEventListener('resize', () => {
        if (window.innerWidth > 768) {
            const sidebar = document.getElementById('sidebar');
            const overlay = document.getElementById('overlay');

            if (sidebar) sidebar.classList.remove('active');
            if (overlay) overlay.classList.remove('active');
        }
    });
}

/**
 * Inicializar el sidebar
 * Esta función debe ser llamada cuando el DOM esté listo
 */
function inicializarSidebar() {
    cargarDatosUsuario();
    filtrarModulosPorRol();
    actualizarMenuActivo();
    configurarEventosMenu();
    configurarResponsive();

    console.log('Sidebar inicializado correctamente');
}

// Auto-inicializar cuando el DOM esté listo
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', inicializarSidebar);
} else {
    // El DOM ya está listo
    inicializarSidebar();
}