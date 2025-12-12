
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
// Toggle sidebar para móvil
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');

    sidebar.classList.toggle('active');
    overlay.classList.toggle('active');
}

// Manejar activación de items del menú
function setupMenuItems() {
    const menuItems = document.querySelectorAll('.menu-item');

    const rolUsuario = localStorage.getItem('rolUsuario');
    const modulosPermitidos = MODULOS_POR_ROL[rolUsuario] || [];

    menuItems.forEach(item => {
        // 1. PRIMERO: Debemos obtener el href antes de usarlo
        const href = item.getAttribute('href');

        // 2. Validación de seguridad: Si no hay href o es '#', no filtramos (o lo manejamos distinto)
        if (href && href !== '#' && !href.includes('inicioSesion.html')) {

            // 3. Ahora sí podemos usar la variable 'href'
            const tienePermiso = modulosPermitidos.some(modulo => href.includes(modulo));

            if (!tienePermiso) {
                item.style.display = 'none';
                // Si lo ocultamos, no tiene sentido agregarle el evento click, así que retornamos
                return;
            } else {
                item.style.display = 'flex';
            }
        }

        // 4. Configuración del evento Click (solo para los visibles)
        item.addEventListener('click', (e) => {
            // Aquí 'href' ya es accesible porque está en el cierre (closure) de la función superior

            if (href && (href.startsWith('http') || href.endsWith('.html'))) {
                return;
            }

            menuItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');

            if (window.innerWidth <= 768) {
                toggleSidebar();
            }
        });
    });
}




// Actualizar menú activo cuando se carga una página
function updateActiveMenuItem() {
    const currentUrl = window.location.href;
    const menuItems = document.querySelectorAll('.menu-item');

    // Remover active de todos primero
    menuItems.forEach(item => item.classList.remove('active'));

    // Agregar active solo al que coincida con la URL actual
    menuItems.forEach(item => {
        const href = item.getAttribute('href');
        // Comparar si la href actual está en la URL
        if (currentUrl.includes(href) && href !== '#' && href !== 'inicioSesion.html') {
            item.classList.add('active');
        }
    });

    // Si no hay ninguno activo y estamos en panelGestion, activar "Inicio"
    if (!document.querySelector('.menu-item.active') && currentUrl.includes('panelGestion.html')) {
        menuItems[0].classList.add('active');
    }
}

// Ajustar al redimensionar la ventana
window.addEventListener('resize', () => {
    if (window.innerWidth > 768) {
        document.getElementById('sidebar').classList.remove('active');
        document.getElementById('overlay').classList.remove('active');
    }
});

// Opcional: Función para cargar dinámicamente el nombre y rol del usuario
function cargarDatosUsuario() {
    // Aquí puedes obtener los datos del usuario desde localStorage o una API
    const nombreUsuario = localStorage.getItem('nombreUsuario') || 'Nombre Usuario';
    const rolUsuario = localStorage.getItem('rolUsuario') || 'Administrador';

    document.querySelector('.user-name').textContent = nombreUsuario;
    document.querySelector('.user-role').textContent = rolUsuario;
}

// Cargar datos del usuario al cargar la página
document.addEventListener('DOMContentLoaded', () => {
    cargarDatosUsuario();
    setupMenuItems();
    updateActiveMenuItem();
});