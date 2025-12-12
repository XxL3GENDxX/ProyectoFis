// Toggle contraseña
function togglePassword() {
    const passwordInput = document.getElementById('contrasena');
    const toggleIcon = document.getElementById('toggleIcon');

    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        toggleIcon.classList.remove('fa-eye');
        toggleIcon.classList.add('fa-eye-slash');
    } else {
        passwordInput.type = 'password';
        toggleIcon.classList.remove('fa-eye-slash');
        toggleIcon.classList.add('fa-eye');
    }
}

// Manejar login
async function handleLogin(event) {

    event.preventDefault();


    const usuario = document.getElementById('usuario').value;
    const contrasena = document.getElementById('contrasena').value;

    // Enviar con los nombres correctos que espera el backend
    const tokenUsuarioData = {
        nombreUsuario: usuario,
        contrasena: contrasena
    };


    try {

        const response = await fetch("http://localhost:8080/api/token_usuario/validarLogin", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(tokenUsuarioData)
        });

        const resultadoLogin = await response.json();

        console.log('Respuesta del servidor:', resultadoLogin, 'Status:', response.status);

        if (response.ok) {

            console.log('Login exitoso para usuario:', usuario);
            localStorage.setItem('nombreUsuario', usuario);
            localStorage.setItem('rolUsuario', resultadoLogin.rol);
            window.location.href = 'panelGestion.html';

        } else {

            const mensajeError = resultadoLogin.mensaje || 'Usuario o Contraseña incorrecta.';
            console.error('Error de autenticación:', mensajeError);
            alert(mensajeError);
        }

    } catch (error) {

        console.error('Error durante la comunicación con el servidor:', error);
        alert('No se pudo conectar con el servicio de autenticación. Verifica tu conexión.');
    }

}
