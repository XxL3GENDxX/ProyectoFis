// Configuración de la API
const API_URL = 'http://localhost:8080/api/estudiantes';
const API_GRADOS_URL = 'http://localhost:8080/api/grados';
const API_GRUPOS_URL = 'http://localhost:8080/api/grupos';

// Estado global
let modoGestionGrupo = false;
let estudiantesData = [];
let timeoutBusqueda = null;
let estudianteSeleccionado = null;
let estudianteADesvincular = null; // Variable para almacenar el estudiante a desvincular

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    inicializarEventos();
    cargarEstudiantes();
});

// Inicializar eventos
function inicializarEventos() {
    // Búsqueda en tiempo real
    document.getElementById('txt-buscar-estudiante').addEventListener('input', function(e) {
        clearTimeout(timeoutBusqueda);
        timeoutBusqueda = setTimeout(() => {
            buscarEstudiantes(e.target.value);
        }, 300);
    });

    // Toggle filtros
    document.getElementById('btn-toggle-filtros').addEventListener('click', toggleFiltros);

    // Aplicar filtros
    document.getElementById('btn-aplicar-filtros').addEventListener('click', aplicarFiltros);

    // Limpiar filtros
    document.getElementById('btn-limpiar-filtros').addEventListener('click', limpiarFiltros);

    // Modo gestión de grupos
    document.getElementById('btn-gestionar-grupo-mode').addEventListener('click', toggleModoGestionGrupo);

    // Evento para cambio de grado en modal de asignación
    document.getElementById('select-grado-asignar').addEventListener('change', cargarGruposPorGrado);

    // Evento para confirmar asignación
    document.getElementById('btn-confirmar-asignar').addEventListener('click', confirmarAsignacion);
    
    // Evento para confirmar desvinculación - NUEVO
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
    
    // Recargar tabla con las opciones correctas
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
    if (modoGestionGrupo) {
        // Verificar si el estudiante tiene grupo asignado
        const tieneGrupo = estudiante.grupo !== null && estudiante.grupo !== undefined;
        
        // Botón asignar: habilitado solo si NO tiene grupo
        const btnAsignarClass = tieneGrupo ? 'btn btn-success btn-icon disabled' : 'btn btn-success btn-icon';
        const btnAsignarDisabled = tieneGrupo ? 'disabled' : '';
        const btnAsignarTitle = tieneGrupo ? 'El estudiante ya tiene grupo asignado' : 'Asignar a grupo';
        const btnAsignarOnclick = tieneGrupo ? '' : `onclick="asignarGrupo(${estudiante.codigoEstudiante})"`;
        
        // Botón desvincular: habilitado solo si tiene grupo
        const btnDesvincularClass = !tieneGrupo ? 'btn btn-danger btn-icon disabled' : 'btn btn-danger btn-icon';
        const btnDesvincularDisabled = !tieneGrupo ? 'disabled' : '';
        const btnDesvincularTitle = !tieneGrupo ? 'El estudiante no tiene grupo asignado' : 'Desvincular de grupo';
        const btnDesvincularOnclick = !tieneGrupo ? '' : `onclick="desvincularGrupo(${estudiante.codigoEstudiante}, '${estudiante.nombre} ${estudiante.apellido}')"`;
        
        return `
            <button class="${btnAsignarClass}" ${btnAsignarOnclick} title="${btnAsignarTitle}" ${btnAsignarDisabled}>
                <i class="fas fa-check"></i>
            </button>
            <button class="${btnDesvincularClass}" ${btnDesvincularOnclick} title="${btnDesvincularTitle}" ${btnDesvincularDisabled}>
                <i class="fas fa-times"></i>
            </button>
        `;
    } else {
        return `
            <button class="btn btn-primary btn-icon" onclick="editarEstudiante(${estudiante.codigoEstudiante})" title="Editar">
                <i class="fas fa-edit"></i>
            </button>
            <button class="btn btn-danger btn-icon" onclick="confirmarEliminar(${estudiante.codigoEstudiante}, '${estudiante.nombre} ${estudiante.apellido}')" title="Eliminar">
                <i class="fas fa-trash"></i>
            </button>
        `;
    }
}

// Asignar grupo (modo gestión)
async function asignarGrupo(codigoEstudiante) {
    estudianteSeleccionado = codigoEstudiante;
    
    // Cargar grados en el modal
    await cargarGradosEnModal();
    
    // Abrir modal de asignación
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
        
        // Limpiar select de grupos
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
function editarEstudiante(codigoEstudiante) {
    mostrarMensaje('Información', 'La funcionalidad de editar estudiante se implementará en el caso de uso correspondiente', 'info');
}

// Confirmar eliminación
function confirmarEliminar(codigoEstudiante, nombreCompleto) {
    mostrarMensaje('Información', 'La funcionalidad de eliminar estudiante se implementará en el caso de uso correspondiente', 'info');
}

/**
 * NUEVA FUNCIONALIDAD - Desvincular grupo
 * Paso 1-2 del diagrama: El actor selecciona desvincular y el sistema muestra confirmación
 * @param {number} codigoEstudiante - Código del estudiante
 * @param {string} nombreCompleto - Nombre completo del estudiante
 */
function desvincularGrupo(codigoEstudiante, nombreCompleto) {
    // Guardar el código del estudiante para usar en la confirmación
    estudianteADesvincular = codigoEstudiante;
    
    // Actualizar el nombre del estudiante en el modal
    document.getElementById('nombre-estudiante-desvincular').textContent = nombreCompleto;
    
    // Paso 2: Mostrar modal de confirmación
    abrirModal('modal-desvincular');
}

/**
 * NUEVA FUNCIONALIDAD - Confirmar desvinculación
 * Paso 4-5 del diagrama: El actor confirma y el sistema desvincula
 */
async function confirmarDesvinculacion() {
    if (!estudianteADesvincular) {
        return;
    }
    
    try {
        // Paso 5: Desvincular al estudiante del grupo
        const response = await fetch(`${API_URL}/${estudianteADesvincular}/desvincular-grupo`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        
        // Cerrar modal de confirmación
        cerrarModal('modal-desvincular');
        
        if (response.ok) {
            // Paso 6: Mostrar mensaje de éxito
            mostrarMensaje('Éxito', 'Estudiante desvinculado satisfactoriamente', 'success');
            // Recargar la tabla de estudiantes
            cargarEstudiantes();
        } else {
            // Flujo alternativo: Error en la base de datos
            mostrarMensaje('Error', data.mensaje || 'Error en la base de datos', 'error');
        }
        
    } catch (error) {
        console.error('Error al desvincular estudiante:', error);
        cerrarModal('modal-desvincular');
        // Flujo alternativo: Error en la base de datos
        mostrarMensaje('Error', 'Error en la base de datos', 'error');
    } finally {
        // Limpiar variable temporal
        estudianteADesvincular = null;
    }
}

/**
 * NUEVA FUNCIONALIDAD - Modificar estudiante
 * Paso 1: El actor selecciona la opción "Modificar estudiante"
 * @param {number} codigoEstudiante - Código del estudiante a modificar
 */
async function editarEstudiante(codigoEstudiante) {
    try {
        // Obtener los datos actuales del estudiante
        const response = await fetch(`${API_URL}/${codigoEstudiante}`);
        
        if (!response.ok) {
            throw new Error('Error al cargar datos del estudiante');
        }
        
        const estudiante = await response.json();
        
        // Paso 2: El sistema despliega campos con los datos actuales
        cargarDatosEnFormularioModificar(estudiante);
        
        // Guardar el código del estudiante para la actualización
        estudianteSeleccionado = codigoEstudiante;
        
        // Abrir modal de modificación
        abrirModal('modal-modificar-estudiante');
        
    } catch (error) {
        console.error('Error al cargar estudiante:', error);
        mostrarMensaje('Error', 'Error al cargar los datos del estudiante', 'error');
    }
}

/**
 * Cargar los datos del estudiante en el formulario de modificación
 * Paso 2: Despliega campos con opciones para modificar
 */
function cargarDatosEnFormularioModificar(estudiante) {
    document.getElementById('modificar-nombre').value = estudiante.nombre || '';
    document.getElementById('modificar-apellido').value = estudiante.apellido || '';
    document.getElementById('modificar-documento').value = estudiante.documento || '';
    
    // Formatear fecha de nacimiento para el input date (YYYY-MM-DD)
    if (estudiante.fechaNacimiento) {
        const fecha = new Date(estudiante.fechaNacimiento);
        const fechaFormateada = fecha.toISOString().split('T')[0];
        document.getElementById('modificar-fecha-nacimiento').value = fechaFormateada;
    } else {
        document.getElementById('modificar-fecha-nacimiento').value = '';
    }
}

/**
 * Confirmar modificación del estudiante
 * Paso 4-7: El actor confirma y el sistema valida y guarda los cambios
 */
async function confirmarModificacion() {
    // Paso 3: Obtener datos modificados del formulario
    const nombre = document.getElementById('modificar-nombre').value.trim();
    const apellido = document.getElementById('modificar-apellido').value.trim();
    const documento = document.getElementById('modificar-documento').value.trim();
    const fechaNacimientoStr = document.getElementById('modificar-fecha-nacimiento').value;
    
    // Validación básica en el cliente (el servidor también validará)
    if (!nombre || !apellido || !documento) {
        mostrarMensaje('Advertencia', 'Por favor complete todos los campos obligatorios', 'warning');
        return;
    }
    
    // Preparar objeto con datos modificados
    const estudianteModificado = {
        nombre: nombre,
        apellido: apellido,
        documento: documento
    };
    
    // Agregar fecha de nacimiento si se proporcionó
    if (fechaNacimientoStr) {
        // Convertir a formato LocalDateTime para el backend
        const fecha = new Date(fechaNacimientoStr);
        estudianteModificado.fechaNacimiento = fecha.toISOString();
    }
    
    try {
        // Paso 4-7: Enviar datos modificados al servidor
        const response = await fetch(`${API_URL}/${estudianteSeleccionado}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(estudianteModificado)
        });
        
        const data = await response.json();
        
        // Cerrar modal
        cerrarModal('modal-modificar-estudiante');
        
        if (response.ok) {
            // Paso 8: Mensaje de éxito
            mostrarMensaje('Éxito', 'Estudiante modificado exitosamente', 'success');
            // Recargar la tabla de estudiantes
            cargarEstudiantes();
        } else {
            // Flujos alternativos
            if (response.status === 400) {
                // Flujo alternativo A: Datos inválidos
                mostrarMensaje('Advertencia', data.mensaje || 'Datos ingresados no válidos', 'warning');
                // Reabrir el modal para que el usuario corrija
                setTimeout(() => abrirModal('modal-modificar-estudiante'), 500);
            } else if (response.status === 409) {
                // Flujo alternativo B: Documento duplicado
                mostrarMensaje('Advertencia', 'Ya existe un registro con este documento', 'warning');
                // Reabrir el modal para que el usuario corrija
                setTimeout(() => abrirModal('modal-modificar-estudiante'), 500);
            } else {
                // Flujo alternativo C: Error en la base de datos
                mostrarMensaje('Error', data.mensaje || 'Error en la base de datos', 'error');
            }
        }
        
    } catch (error) {
        console.error('Error al modificar estudiante:', error);
        cerrarModal('modal-modificar-estudiante');
        mostrarMensaje('Error', 'Error en la base de datos', 'error');
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

// Funciones de modal
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
    
    // Configurar icono según tipo
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

// Cerrar modal al hacer clic fuera
window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
        event.target.classList.remove('show');
    }
}