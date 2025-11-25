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
function handleLogin(event) {
    event.preventDefault();

    const usuario = document.getElementById('usuario').value;
    const contrasena = document.getElementById('contrasena').value;

    // Aquí iría la lógica de autenticación
    console.log('Intentando login con:', { usuario, contrasena });

    // Simulación de login exitoso
    // En producción, esto debería validar contra el backend
    alert('Inicio de sesión exitoso (simulado)');

    // Redirigir al módulo de gestión de estudiantes
    window.location.href = 'gestionarEstudiantes.html';
}