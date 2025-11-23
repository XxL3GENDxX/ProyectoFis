// Configuración de la API
const API_URL = 'http://localhost:8080/api/estudiantes';
const API_GRADOS_URL = 'http://localhost:8080/api/grados';
const API_GRUPOS_URL = 'http://localhost:8080/api/grupos';

// Estado global
let modoGestionGrupo = false;
let estudiantesData = [];
let timeoutBusqueda = null;
let estudianteSeleccionado = null;
let estudianteADesvincular = null;

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    inicializarEventos();
    cargarEstudiantes();
});

// Inicializar eventos
function inicializarEventos() {
    document.getElementById('txt-buscar-estudiante').addEventListener('input', function(e) {
        clearTimeout(timeoutBusqueda);
        timeoutBusqueda = setTimeout(() => {
            buscarEstudiantes(e.target.value);
        }, 300);
    });

    document.getElementById('btn-toggle-filtros').addEventListener('click', toggleFiltros);
    document.getElementById('btn-aplicar-filtros').addEventListener('click', aplicarFiltros);
    document.getElementById('btn-limpiar-filtros').addEventListener('click', limpiarFiltros);
    document.getElementById('btn-gestionar-grupo-mode').addEventListener('click', toggleModoGestionGrupo);
    document.getElementById('select-grado-asignar').addEventListener('change', cargarGruposPorGrado);
    document.getElementById('btn-confirmar-asignar').addEventListener('click', confirmarAsignacion);
    document.getElementById('btn-confirmar-desvincular').addEventListener('click', confirmarDesvinculacion);
    document.getElementById('btn-confirmar-modificar').addEventListener('click', confirmarModificacion);
}

// Cargar todos los estudiantes
async function cargarEstudiantes() {
    mostrarLoading();
    
    try {
        const response = await fetch(API_URL);
        
        if (!response.ok) {
            throw new Error('Error al cargar estudiantes');
        }
        
        const estudiantes = await response.json();
        estudiantesData = estudiantes;
        
        if (estudiantes.length === 0) {
            mostrarEstadoVacio();
        } else {
            mostrarTabla(estudiantes);
        }
    } catch (error) {
        console.error('Error:', error);
        mostrarError();
    }
}

// Búsqueda de estudiantes
function buscarEstudiantes(texto) {
    if (!texto) {
        mostrarTabla(estudiantesData);
        return;
    }
    
    const textoLower = texto.toLowerCase();
    const filtrados = estudiantesData.filter(est => {
        return est.nombre.toLowerCase().includes(textoLower) ||
               est.apellido.toLowerCase().includes(textoLower) ||
               (est.documento && est.documento.toLowerCase().includes(textoLower)) ||
               (est.grupo && est.grupo.grado && est.grupo.grado.nombreGrado.toLowerCase().includes(textoLower)) ||
               (est.grupo && est.grupo.numeroGrupo && est.grupo.numeroGrupo.toString().includes(textoLower));
    });
    
    mostrarTabla(filtrados);
}

// Aplicar filtros avanzados
async function aplicarFiltros() {
    mostrarLoading();
    
    const filtro = {
        genero: document.getElementById('filtro-genero').value || null,
        edadMinima: document.getElementById('filtro-edad-min').value ? 
                    parseInt(document.getElementById('filtro-edad-min').value) : null,
        edadMaxima: document.getElementById('filtro-edad-max').value ? 
                    parseInt(document.getElementById('filtro-edad-max').value) : null,
        ordenAlfabetico: document.getElementById('filtro-orden').value === 'true'
    };
    
}

// Limpiar filtros
function limpiarFiltros() {
    document.getElementById('filtro-genero').value = '';
    document.getElementById('filtro-edad-min').value = '';
    document.getElementById('filtro-edad-max').value = '';
    document.getElementById('filtro-orden').value = 'true';
    cargarEstudiantes();
}

// Toggle panel de filtros
function toggleFiltros() {
    const panel = document.getElementById('panel-filtros');
    if (panel.style.display === 'none') {
        panel.style.display = 'block';
    } else {
        panel.style.display = 'none';
    }
}

// Toggle modo gestión de grupos
function toggleModoGestionGrupo() {
    modoGestionGrupo = !modoGestionGrupo;
    const btn = document.getElementById('btn-gestionar-grupo-mode');
    const info = document.getElementById('modo-gestion-info');
    const thOpciones = document.getElementById('th-opciones');
    
    if (modoGestionGrupo) {
        btn.classList.add('active');
        btn.innerHTML = '<i class="fas fa-check"></i> Modo Grupos Activo';
        info.style.display = 'block';
        thOpciones.textContent = 'Gestión de Grupos';
    } else {
        btn.classList.remove('active');
        btn.innerHTML = '<i class="fas fa-users"></i> Gestionar Grupos';
        info.style.display = 'none';
        thOpciones.textContent = 'Opciones';
    }
    
    mostrarTabla(estudiantesData);
}

// Mostrar tabla de estudiantes
function mostrarTabla(estudiantes) {
    ocultarTodosLosEstados();
    
    const tbody = document.getElementById('tbody-estudiantes');
    tbody.innerHTML = '';
    
    estudiantes.forEach(estudiante => {
        const tr = document.createElement('tr');
        
        const estadoClass = estudiante.estado === 'Activo' ? 'estado-activo' : 'estado-inactivo';
        const grupoTexto = estudiante.grupo ? 
            `${estudiante.grupo.grado.nombreGrado} - Grupo ${estudiante.grupo.numeroGrupo}` : 
            'Sin grupo';
        const gradoTexto = estudiante.grupo ? estudiante.grupo.grado.nombreGrado : 'N/A';
        
        tr.innerHTML = `
            <td>${estudiante.nombre}</td>
            <td>${estudiante.apellido}</td>
            <td>${gradoTexto}</td>
            <td>${grupoTexto}</td>
            <td><span class="estado-badge ${estadoClass}">${estudiante.estado}</span></td>
            <td>
                <div class="options-cell">
                    ${generarBotonesOpciones(estudiante)}
                </div>
            </td>
        `;
        
        tbody.appendChild(tr);
    });
    
    document.getElementById('tabla-container').style.display = 'block';
}

// Generar botones de opciones según el modo
function generarBotonesOpciones(estudiante) {
    const esActivo = estudiante.estado === 'Activo';
    
    if (modoGestionGrupo) {
        const tieneGrupo = estudiante.grupo !== null && estudiante.grupo !== undefined;
        
        const btnAsignarClass = !esActivo || tieneGrupo ? 'btn btn-success btn-icon disabled' : 'btn btn-success btn-icon';
        const btnAsignarDisabled = !esActivo || tieneGrupo ? 'disabled' : '';
        const btnAsignarTitle = !esActivo ? 'Estudiante inactivo' : (tieneGrupo ? 'El estudiante ya tiene grupo asignado' : 'Asignar a grupo');
        const btnAsignarOnclick = !esActivo || tieneGrupo ? '' : `onclick="asignarGrupo(${estudiante.codigoEstudiante})"`;
        
        const btnDesvincularClass = !esActivo || !tieneGrupo ? 'btn btn-danger btn-icon disabled' : 'btn btn-danger btn-icon';
        const btnDesvincularDisabled = !esActivo || !tieneGrupo ? 'disabled' : '';
        const btnDesvincularTitle = !esActivo ? 'Estudiante inactivo' : (!tieneGrupo ? 'El estudiante no tiene grupo asignado' : 'Desvincular de grupo');
        const btnDesvincularOnclick = !esActivo || !tieneGrupo ? '' : `onclick="desvincularGrupo(${estudiante.codigoEstudiante}, '${estudiante.nombre} ${estudiante.apellido}')"`;
        
        return `
            <button class="${btnAsignarClass}" ${btnAsignarOnclick} title="${btnAsignarTitle}" ${btnAsignarDisabled}>
                <i class="fas fa-check"></i>
            </button>
            <button class="${btnDesvincularClass}" ${btnDesvincularOnclick} title="${btnDesvincularTitle}" ${btnDesvincularDisabled}>
                <i class="fas fa-times"></i>
            </button>
        `;
    } else {
        // Modo normal: Editar, Eliminar y Switch de Estado
        const switchChecked = esActivo ? 'checked' : '';
        const btnEditarClass = !esActivo ? 'btn btn-primary btn-icon disabled' : 'btn btn-primary btn-icon';
        const btnEditarDisabled = !esActivo ? 'disabled' : '';
        const btnEditarOnclick = !esActivo ? '' : `onclick="editarEstudiante(${estudiante.codigoEstudiante})"`;
        const btnEditarTitle = !esActivo ? 'Estudiante inactivo' : 'Editar';
        
        return `
            <button class="${btnEditarClass}" ${btnEditarOnclick} title="${btnEditarTitle}" ${btnEditarDisabled}>
                <i class="fas fa-edit"></i>
            </button>
            <label class="switch-estado" title="Gestionar estado del estudiante">
                <input type="checkbox" ${switchChecked} onchange="cambiarEstadoEstudiante(${estudiante.codigoEstudiante}, '${estudiante.nombre} ${estudiante.apellido}')">
                <span class="slider"></span>
            </label>
        `;
    }
}

/**
 * Cambiar estado del estudiante (Activo/Inactivo)
 * Paso 1: El actor selecciona la opción "Gestionar estado del estudiante"
 * Paso 2: El sistema muestra mensaje de confirmación
 */
function cambiarEstadoEstudiante(codigoEstudiante, nombreCompleto) {
    // Guardar datos para la confirmación
    estudianteSeleccionado = codigoEstudiante;
    
    // Actualizar nombre en el modal
    document.getElementById('nombre-estudiante-estado').textContent = nombreCompleto;
    
    // Paso 2: Mostrar mensaje de confirmación
    abrirModal('modal-confirmar-cambio-estado');
}

/**
 * Confirmar cambio de estado
 * Paso 4-14: El actor confirma y el sistema cambia el estado
 */
async function confirmarCambioEstado() {
    if (!estudianteSeleccionado) {
        return;
    }
    
    try {
        // Paso 5-11: Solicitar cambio de estado al backend
        const response = await fetch(`${API_URL}/${estudianteSeleccionado}/cambiar-estado`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        
        // Cerrar modal de confirmación
        cerrarModal('modal-confirmar-cambio-estado');
        
        if (response.ok) {
            // Paso 8/12: Mostrar mensaje de éxito
            mostrarMensaje('Éxito', 'Estado modificado exitosamente', 'success');
            // Recargar tabla
            cargarEstudiantes();
        } else {
            // Flujo alternativo: Error en la base de datos
            mostrarMensaje('Error', data.mensaje || 'Error en la base de datos', 'error');
        }
        
    } catch (error) {
        console.error('Error al cambiar estado:', error);
        cerrarModal('modal-confirmar-cambio-estado');
        mostrarMensaje('Error', 'Error en la base de datos', 'error');
    } finally {
        estudianteSeleccionado = null;
    }
}

/**
 * Cancelar cambio de estado
 * Paso 3 - Flujo alternativo: El actor cancela la operación
 */
function cancelarCambioEstado() {
    // Restaurar el estado del switch
    const estudiante = estudiantesData.find(e => e.codigoEstudiante === estudianteSeleccionado);
    if (estudiante) {
        cargarEstudiantes();
    }
    
    cerrarModal('modal-confirmar-cambio-estado');
    estudianteSeleccionado = null;
}

// Asignar grupo (modo gestión)
async function asignarGrupo(codigoEstudiante) {
    estudianteSeleccionado = codigoEstudiante;
    await cargarGradosEnModal();
    abrirModal('modal-asignar-grupo');
}

// Cargar grados en el modal
async function cargarGradosEnModal() {
    try {
        const response = await fetch(API_GRADOS_URL);
        
        if (!response.ok) {
            throw new Error('Error al cargar grados');
        }
        
        const grados = await response.json();
        const select = document.getElementById('select-grado-asignar');
        
        select.innerHTML = '<option value="">Seleccione un grado</option>';
        
        grados.forEach(grado => {
            const option = document.createElement('option');
            option.value = grado.idGrado;
            option.textContent = grado.nombreGrado;
            select.appendChild(option);
        });
        
        document.getElementById('select-grupo-asignar').innerHTML = '<option value="">Primero seleccione un grado</option>';
        
    } catch (error) {
        console.error('Error al cargar grados:', error);
        mostrarMensaje('Error', 'Error al cargar los grados disponibles', 'error');
    }
}

// Cargar grupos por grado
async function cargarGruposPorGrado() {
    const idGrado = document.getElementById('select-grado-asignar').value;
    const selectGrupo = document.getElementById('select-grupo-asignar');
    
    if (!idGrado) {
        selectGrupo.innerHTML = '<option value="">Primero seleccione un grado</option>';
        return;
    }
    
    try {
        const response = await fetch(`${API_GRUPOS_URL}/grado/${idGrado}`);
        
        if (!response.ok) {
            throw new Error('Error al cargar grupos');
        }
        
        const grupos = await response.json();
        
        selectGrupo.innerHTML = '<option value="">Seleccione un grupo</option>';
        
        grupos.forEach(grupo => {
            const option = document.createElement('option');
            option.value = grupo.idGrupo;
            option.textContent = `Grupo ${grupo.numeroGrupo} (${grupo.numeroEstudiantes || 0}/${grupo.limiteEstudiantes} estudiantes)`;
            selectGrupo.appendChild(option);
        });
        
    } catch (error) {
        console.error('Error al cargar grupos:', error);
        mostrarMensaje('Error', 'Error al cargar los grupos disponibles', 'error');
    }
}

// Confirmar asignación
async function confirmarAsignacion() {
    const idGrado = document.getElementById('select-grado-asignar').value;
    const idGrupo = document.getElementById('select-grupo-asignar').value;
    
    if (!idGrado || !idGrupo) {
        mostrarMensaje('Información', 'Por favor seleccione un grado y un grupo', 'info');
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/${estudianteSeleccionado}/asignar-grupo/${idGrupo}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        
        if (response.ok) {
            cerrarModal('modal-asignar-grupo');
            mostrarMensaje('Éxito', 'Estudiante asignado exitosamente', 'success');
            cargarEstudiantes();
        } else {
            if (data.mensaje === 'Grupo completo') {
                mostrarMensaje('Advertencia', 'Grupo completo', 'warning');
            } else {
                mostrarMensaje('Error', data.mensaje || 'Error en la base de datos', 'error');
            }
        }
        
    } catch (error) {
        console.error('Error al asignar estudiante:', error);
        mostrarMensaje('Error', 'Error en la base de datos', 'error');
    }
}

// Editar estudiante
async function editarEstudiante(codigoEstudiante) {
    try {
        const response = await fetch(`${API_URL}/${codigoEstudiante}`);
        
        if (!response.ok) {
            throw new Error('Error al cargar datos del estudiante');
        }
        
        const estudiante = await response.json();
        cargarDatosEnFormularioModificar(estudiante);
        estudianteSeleccionado = codigoEstudiante;
        abrirModal('modal-modificar-estudiante');
        
    } catch (error) {
        console.error('Error al cargar estudiante:', error);
        mostrarMensaje('Error', 'Error al cargar los datos del estudiante', 'error');
    }
}

function cargarDatosEnFormularioModificar(estudiante) {
    document.getElementById('modificar-nombre').value = estudiante.nombre || '';
    document.getElementById('modificar-apellido').value = estudiante.apellido || '';
    document.getElementById('modificar-documento').value = estudiante.documento || '';
    
    if (estudiante.fechaNacimiento) {
        const fecha = new Date(estudiante.fechaNacimiento);
        const fechaFormateada = fecha.toISOString().split('T')[0];
        document.getElementById('modificar-fecha-nacimiento').value = fechaFormateada;
    } else {
        document.getElementById('modificar-fecha-nacimiento').value = '';
    }
}

async function confirmarModificacion() {
    const nombre = document.getElementById('modificar-nombre').value.trim();
    const apellido = document.getElementById('modificar-apellido').value.trim();
    const documento = document.getElementById('modificar-documento').value.trim();
    const fechaNacimientoStr = document.getElementById('modificar-fecha-nacimiento').value;
    
    if (!nombre || !apellido || !documento) {
        mostrarMensaje('Advertencia', 'Por favor complete todos los campos obligatorios', 'warning');
        return;
    }
    
    const estudianteModificado = {
        nombre: nombre,
        apellido: apellido,
        documento: documento
    };
    
    if (fechaNacimientoStr) {
        const fecha = new Date(fechaNacimientoStr);
        estudianteModificado.fechaNacimiento = fecha.toISOString();
    }
    
    try {
        const response = await fetch(`${API_URL}/${estudianteSeleccionado}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(estudianteModificado)
        });
        
        const data = await response.json();
        cerrarModal('modal-modificar-estudiante');
        
        if (response.ok) {
            mostrarMensaje('Éxito', 'Estudiante modificado exitosamente', 'success');
            cargarEstudiantes();
        } else {
            if (response.status === 400) {
                mostrarMensaje('Advertencia', data.mensaje || 'Datos ingresados no válidos', 'warning');
                setTimeout(() => abrirModal('modal-modificar-estudiante'), 500);
            } else if (response.status === 409) {
                mostrarMensaje('Advertencia', 'Ya existe un registro con este documento', 'warning');
                setTimeout(() => abrirModal('modal-modificar-estudiante'), 500);
            } else {
                mostrarMensaje('Error', data.mensaje || 'Error en la base de datos', 'error');
            }
        }
        
    } catch (error) {
        console.error('Error al modificar estudiante:', error);
        cerrarModal('modal-modificar-estudiante');
        mostrarMensaje('Error', 'Error en la base de datos', 'error');
    }
}

function desvincularGrupo(codigoEstudiante, nombreCompleto) {
    estudianteADesvincular = codigoEstudiante;
    document.getElementById('nombre-estudiante-desvincular').textContent = nombreCompleto;
    abrirModal('modal-desvincular');
}

async function confirmarDesvinculacion() {
    if (!estudianteADesvincular) {
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/${estudianteADesvincular}/desvincular-grupo`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        cerrarModal('modal-desvincular');
        
        if (response.ok) {
            mostrarMensaje('Éxito', 'Estudiante desvinculado satisfactoriamente', 'success');
            cargarEstudiantes();
        } else {
            mostrarMensaje('Error', data.mensaje || 'Error en la base de datos', 'error');
        }
        
    } catch (error) {
        console.error('Error al desvincular estudiante:', error);
        cerrarModal('modal-desvincular');
        mostrarMensaje('Error', 'Error en la base de datos', 'error');
    } finally {
        estudianteADesvincular = null;
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