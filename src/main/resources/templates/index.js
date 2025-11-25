// Navegación entre secciones
function showSection(sectionName) {
    // Ocultar todas las secciones
    const sections = document.querySelectorAll('.content-section');
    sections.forEach(section => {
        section.classList.remove('active');
    });

    // Mostrar la sección seleccionada
    document.getElementById(sectionName).classList.add('active');

    // Actualizar enlaces activos
    const links = document.querySelectorAll('.nav-links a:not(.btn-login)');
    links.forEach(link => {
        link.classList.remove('active');
    });
    event.target.classList.add('active');

    // Cerrar menú móvil si está abierto
    document.getElementById('navLinks').classList.remove('active');
}

// Toggle menú móvil
function toggleMobileMenu() {
    const navLinks = document.getElementById('navLinks');
    navLinks.classList.toggle('active');
}

// Enviar formulario de admisión
function enviarSolicitud(event) {
    event.preventDefault();

    // Recopilar datos del formulario
    const formData = {
        estudiante: {
            numeroIdentificacion: document.getElementById('numIdentificacion').value,
            nombre: document.getElementById('nombre').value,
            apellido: document.getElementById('apellido').value,
            fechaNacimiento: document.getElementById('fechaNacimiento').value
        },
        acudiente: {
            numeroIdentificacion: document.getElementById('numIdentificacionAcudiente').value,
            nombre: document.getElementById('nombreAcudiente').value,
            apellido: document.getElementById('apellidoAcudiente').value,
            fechaNacimiento: document.getElementById('fechaNacimientoAcudiente').value,
            telefono: document.getElementById('telefono').value,
            email: document.getElementById('email').value
        }
    };

    // Mostrar mensaje de confirmación
    alert('¡Solicitud enviada exitosamente! Nos pondremos en contacto con usted pronto.');

    // Limpiar formulario
    document.getElementById('formAdmision').reset();

    console.log('Datos de solicitud:', formData);
}

// Smooth scroll para navegación
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            const href = this.getAttribute('href');
            if (href !== '#') {
                e.preventDefault();
                const target = document.querySelector(href);
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            }
        });
    });
});