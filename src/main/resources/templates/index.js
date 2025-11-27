// Configuración de la API - ACTUALIZADA
const API_URL = 'http://localhost:8080/api/estudiante';
const API_GRADOS_URL = 'http://localhost:8080/api/grados';
const API_GRUPOS_URL = 'http://localhost:8080/api/grupos';
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

    try {
        // 1. CREAR PERSONA DEL ACUDIENTE
        const personaAcudienteData = {
            documento: document.getElementById('numIdentificacionAcudiente').value,
            nombre: document.getElementById('nombreAcudiente').value,
            apellido: document.getElementById('apellidoAcudiente').value,
            fechaDeNacimiento: document.getElementById('fechaNacimientoAcudiente').value + 'T00:00:00',
            genero: document.getElementById('generoAcudiente').value
        };

        console.log('Creando persona acudiente:', personaAcudienteData);

        const personaAcudienteResponse = await fetch("http://localhost:8080/api/persona/crear", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(personaAcudienteData)
        });

        if (!personaAcudienteResponse.ok) {
            throw new Error('Error al crear persona del acudiente');
        }

        const personaAcudienteCreada = await personaAcudienteResponse.json();
        console.log('Persona acudiente creada:', personaAcudienteCreada);

        // 2. CREAR PERSONA DEL ESTUDIANTE
        const personaEstudianteData = {
            documento: document.getElementById('numIdentificacion').value,
            nombre: document.getElementById('nombre').value,
            apellido: document.getElementById('apellido').value,
            fechaDeNacimiento: document.getElementById('fechaNacimiento').value + 'T00:00:00',
            genero: document.getElementById('generoEstudiante').value
        };

        console.log('Creando persona estudiante:', personaEstudianteData);

        const personaEstudianteResponse = await fetch("http://localhost:8080/api/persona/crear", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(personaEstudianteData)
        });

        if (!personaEstudianteResponse.ok) {
            throw new Error('Error al crear persona del estudiante');
        }

        const personaEstudianteCreada = await personaEstudianteResponse.json();
        console.log('Persona estudiante creada:', personaEstudianteCreada);

        // 3. CREAR ACUDIENTE
        const acudienteData = {
            persona: {
                idPersona: personaAcudienteCreada.idPersona
            },
            correoElectronico: document.getElementById('email').value,
            telefono: document.getElementById('telefono').value,
            estado: 'Activo'
        };

        console.log('Creando acudiente:', acudienteData);

        const acudienteResponse = await fetch("http://localhost:8080/api/acudiente/crear", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(acudienteData)
        });

        if (!acudienteResponse.ok) {
            throw new Error('Error al crear acudiente');
        }

        const acudienteCreado = await acudienteResponse.json();
        console.log('Acudiente creado:', acudienteCreado);

        // 4. CREAR ESTUDIANTE
        const estudianteData = {
            persona: {
                idPersona: personaEstudianteCreada.idPersona
            },
            acudiente: {
                idAcudiente: acudienteCreado.idAcudiente
            },
            estado: 'Pendiente'
        };

        console.log('Creando estudiante:', estudianteData);

        const estudianteResponse = await fetch("http://localhost:8080/api/estudiante/crear", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(estudianteData)
        });

        if (!estudianteResponse.ok) {
            throw new Error('Error al crear estudiante');
        }

        const estudianteCreado = await estudianteResponse.json();
        console.log('Estudiante creado:', estudianteCreado);

        // 5. CREAR PREINSCRIPCIÓN
        const preinscripcionData = {
            aspirante: {
                codigoEstudiante: estudianteCreado.codigoEstudiante
            },
            acudiente: {
                idAcudiente: acudienteCreado.idAcudiente
            },
            fechaEntrevista: new Date().toISOString()
        };

        console.log('Creando preinscripción:', preinscripcionData);

        const preinscripcionResponse = await fetch("http://localhost:8080/api/preinscripcion/crear", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(preinscripcionData)
        });

        if (!preinscripcionResponse.ok) {
            throw new Error('Error al crear preinscripción');
        }

        alert("¡Solicitud registrada con éxito!");
        document.getElementById('formAdmision').reset();

    } catch (error) {
        console.error('Error en el proceso:', error);
        alert("Error al registrar la solicitud: " + error.message);
    }
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