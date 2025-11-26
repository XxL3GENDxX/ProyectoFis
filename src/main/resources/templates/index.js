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
async function enviarSolicitud(event) {
    event.preventDefault();

    const personaAcudienteDATA = {
        numeroIdentificacion: document.getElementById('numIdentificacionAcudiente').value,
        nombre: document.getElementById('nombreAcudiente').value,
        apellido: document.getElementById('apellidoAcudiente').value,
        fechaNacimiento: document.getElementById('fechaNacimientoAcudiente').value, 
    };

    const personaAcudienteResponse = await fetch("http://localhost:8080/api/persona/crear", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(personaAcudienteDATA)
    });
    const personaAcudienteCreada = await personaAcudienteResponse.json();

    const personaEstudianteDATA = {
        numeroIdentificacion: document.getElementById('numIdentificacion').value,
        nombre: document.getElementById('nombre').value,
        apellido: document.getElementById('apellido').value,
        fechaNacimiento: document.getElementById('fechaNacimiento').value, 
    };

    const personaEstudianteResponse = await fetch("http://localhost:8080/api/persona/crear", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(personaEstudianteDATA)
    });
    const personaEstudianteCreada = await personaEstudianteResponse.json();

    // 1. CREAR ACUDIENTE
    const acudienteData = {
        idPersona: personaAcudienteCreada.id,
        direccion: document.getElementById('direccion').value,
        telefono: document.getElementById('telefono').value,
        email: document.getElementById('email').value
    };

    const acudienteResponse = await fetch("http://localhost:8080/api/acudiente/crear", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(acudienteData)
    });

    const acudienteCreado = await acudienteResponse.json();

    // 2. CREAR ESTUDIANTE usando id del acudiente
    const estudianteData = {
        idPersona: personaEstudianteCreada.id,
        idAcudiente: acudienteCreado.id
        
    };

    await fetch("http://localhost:8080/api/estudiante/crear", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(estudianteData)
    });

    const estudianteCreado = await estudianteResponse.json();

    // 3. CREAR PREINSCRIPCIÓN usando id del estudiante
    const preinscripcionData = {
        idEstudiante: estudianteCreado.id,
        idAcudiente: acudienteCreado.id,
    };

    await fetch("http://localhost:8080/api/preinscripcion/crear", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(preinscripcionData)
    });

    alert("¡Solicitud registrada con éxito!");

    document.getElementById('formAdmision').reset();
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