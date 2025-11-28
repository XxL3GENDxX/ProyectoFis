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
    
    menuItems.forEach(item => {
        item.addEventListener('click', (e) => {
            // Solo remover active si es un enlace interno (no un href real)
            if (item.getAttribute('href').startsWith('http') || 
                item.getAttribute('href').endsWith('.html')) {
                // Dejar que navegue naturalmente
                return;
            }
            
            // Remover clase active de todos los items
            menuItems.forEach(i => i.classList.remove('active'));
            
            // Agregar clase active solo al item clickeado
            item.classList.add('active');
            
            // Cerrar sidebar en móvil
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