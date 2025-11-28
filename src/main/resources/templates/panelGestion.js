// Toggle sidebar para móvil
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');
    
    sidebar.classList.toggle('active');
    overlay.classList.toggle('active');
}

// Cerrar sidebar al hacer clic en un enlace en móvil
if (window.innerWidth <= 768) {
    document.querySelectorAll('.menu-item').forEach(item => {
        item.addEventListener('click', () => {
            toggleSidebar();
        });
    });
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
});