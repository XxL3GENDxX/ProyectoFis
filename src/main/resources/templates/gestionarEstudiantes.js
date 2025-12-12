function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');

    sidebar.classList.toggle('active');
    overlay.classList.toggle('active');
}

// Configuración de la API
const API_URL = 'http://localhost:8080/api/estudiante';
const API_GRADOS_URL = 'http://localhost:8080/api/grados';
const API_GRUPOS_URL = 'http://localhost:8080/api/grupos';

// Estado global
let modoGestionGrupo = false;
let estudiantesData = [];
let timeoutBusqueda = null;
let estudianteSeleccionado = null;
let estudianteADesvincular = null;

// Inicialización
document.addEventListener('DOMContentLoaded', function () {
    inicializarEventos();
    cargarEstudiantes();
});

// Inicializar eventos
function inicializarEventos() {
    // Paso 1: El actor diligencia la barra de búsqueda
    document.getElementById('txt-buscar-estudiante').addEventListener('input', function (e) {
        clearTimeout(timeoutBusqueda);
        timeoutBusqueda = setTimeout(() => {
            aplicarBusquedaYFiltros();
        }, 300);
    });

    document.getElementById('btn-toggle-filtros').addEventListener('click', toggleFiltros);
    // Paso 6: El actor presiona la opción "Buscar" (aplicar filtros)
    document.getElementById('btn-aplicar-filtros').addEventListener('click', aplicarBusquedaYFiltros);
    document.getElementById('btn-limpiar-filtros').addEventListener('click', limpiarFiltros);
    document.getElementById('btn-gestionar-grupo-mode').addEventListener('click', toggleModoGestionGrupo);
    document.getElementById('select-grado-asignar').addEventListener('change', cargarGruposPorGrado);
    document.getElementById('btn-confirmar-asignar').addEventListener('click', confirmarAsignacion);
    document.getElementById('btn-confirmar-desvincular').addEventListener('click', confirmarDesvinculacion);
    document.getElementById('btn-confirmar-modificar').addEventListener('click', confirmarModificacion);
}

// Cargar todos los estudiantes (sin filtros)
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

/**
 * Aplicar búsqueda y filtros
 * Implementa el flujo completo del caso de uso "Mostrar Estudiantes por Filtro"
 * 
 * Paso 1: Obtiene el texto de la barra de búsqueda
 * Paso 3-5: Obtiene los valores de los filtros seleccionados
 * Paso 6: Realiza la búsqueda cuando el actor presiona "Buscar"
 * Paso 7-9: Muestra los resultados o mensaje de error
 */
async function aplicarBusquedaYFiltros() {
    mostrarLoading();

    // Paso 1: Obtener texto de búsqueda
    const textoBusqueda = document.getElementById('txt-buscar-estudiante').value.trim();

    // Paso 3-5: Obtener valores de filtros opcionales seleccionados
    const genero = document.getElementById('filtro-genero').value || null;
    const edadMinima = document.getElementById('filtro-edad-min').value ?
        parseInt(document.getElementById('filtro-edad-min').value) : null;
    const edadMaxima = document.getElementById('filtro-edad-max').value ?
        parseInt(document.getElementById('filtro-edad-max').value) : null;
    const ordenAlfabetico = document.getElementById('filtro-orden').value === 'true';

    // Validar rango de edad (Regla de negocio)
    if (edadMinima !== null && edadMaxima !== null && edadMinima > edadMaxima) {
        mostrarMensaje('Advertencia',
            'La edad mínima no puede ser mayor que la edad máxima',
            'warning');
        return;
    }

    try {
        // Construir URL con parámetros
        const params = new URLSearchParams();

        if (textoBusqueda) {
            params.append('textoBusqueda', textoBusqueda);
        }
        if (genero) {
            params.append('genero', genero);
        }
        if (edadMinima !== null) {
            params.append('edadMinima', edadMinima);
        }
        if (edadMaxima !== null) {
            params.append('edadMaxima', edadMaxima);
        }
        params.append('ordenAlfabetico', ordenAlfabetico);

        // Paso 6-7: Realizar búsqueda en el servidor
        const url = `${API_URL}/buscar?${params.toString()}`;
        console.log('Buscando con URL:', url);

        const response = await fetch(url);

        // Paso 8: Verificar si se encontraron resultados
        if (response.status === 404) {
            // Flujo Alternativo: No se encontraron resultados
            // Paso 2 del flujo alternativo: Mostrar mensaje
            const data = await response.json();
            mostrarMensaje('Información',
                data.mensaje || 'No se encontró ningún estudiante con los criterios especificados',
                'info');
            mostrarTabla([]);
            return;
        }

        if (!response.ok) {
            // Flujo Alternativo: Error al conectar con la base de datos
            const data = await response.json();
            mostrarMensaje('Error',
                data.mensaje || 'Error en la base de datos',
                'error');
            mostrarError();
            return;
        }

        // Paso 9: Mostrar los estudiantes encontrados
        const estudiantes = await response.json();
        estudiantesData = estudiantes;
        mostrarTabla(estudiantes);

        console.log(`Búsqueda completada. Se encontraron ${estudiantes.length} estudiantes`);

    } catch (error) {
        // Flujo Alternativo: Error al conectar con la base de datos
        console.error('Error en búsqueda y filtros:', error);
        mostrarMensaje('Error', 'Error en la base de datos', 'error');
        mostrarError();
    }
}

// Limpiar filtros y volver a cargar todos los estudiantes
function limpiarFiltros() {
    document.getElementById('txt-buscar-estudiante').value = '';
    document.getElementById('filtro-genero').value = '';
    document.getElementById('filtro-edad-min').value = '';
    document.getElementById('filtro-edad-max').value = '';
    document.getElementById('filtro-orden').value = 'true';
    cargarEstudiantes();
}

// Toggle panel de filtros (Paso 2: El sistema muestra opciones de filtros)
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
            <td>${estudiante.persona.nombre}</td>
            <td>${estudiante.persona.apellido}</td>
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
        const btnDesvincularOnclick = !esActivo || !tieneGrupo ? '' : `onclick="desvincularGrupo(${estudiante.codigoEstudiante}, '${estudiante.persona.nombre} ${estudiante.persona.apellido}')"`;

        return `
            <button class="${btnAsignarClass}" ${btnAsignarOnclick} title="${btnAsignarTitle}" ${btnAsignarDisabled}>
                <i class="fas fa-check"></i>
            </button>
            <button class="${btnDesvincularClass}" ${btnDesvincularOnclick} title="${btnDesvincularTitle}" ${btnDesvincularDisabled}>
                <i class="fas fa-times"></i>
            </button>
        `;
    } else {
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
                <input type="checkbox" ${switchChecked} onchange="cambiarEstadoEstudiante(${estudiante.codigoEstudiante}, '${estudiante.persona.nombre} ${estudiante.persona.apellido}')">
                <span class="slider"></span>
            </label>
        `;
    }
}

function cambiarEstadoEstudiante(codigoEstudiante, nombreCompleto) {
    estudianteSeleccionado = codigoEstudiante;
    document.getElementById('nombre-estudiante-estado').textContent = nombreCompleto;
    abrirModal('modal-confirmar-cambio-estado');
}

async function confirmarCambioEstado() {
    if (!estudianteSeleccionado) {
        return;
    }

    try {
        const response = await fetch(`${API_URL}/${estudianteSeleccionado}/cambiar-estado`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        const data = await response.json();
        cerrarModal('modal-confirmar-cambio-estado');

        if (response.ok) {
            mostrarMensaje('Éxito', 'Estado modificado exitosamente', 'success');
            aplicarBusquedaYFiltros();
        } else {
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

function cancelarCambioEstado() {
    cerrarModal('modal-confirmar-cambio-estado');
    aplicarBusquedaYFiltros();
    estudianteSeleccionado = null;
}

async function asignarGrupo(codigoEstudiante) {
    estudianteSeleccionado = codigoEstudiante;
    await cargarGradosEnModal();
    abrirModal('modal-asignar-grupo');
}

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
            option.textContent = `Grupo ${grupo.numeroGrupo} (${grupo.numeroEstudiantes || 0}/10 estudiantes)`;
            selectGrupo.appendChild(option);
        });

    } catch (error) {
        console.error('Error al cargar grupos:', error);
        mostrarMensaje('Error', 'Error al cargar los grupos disponibles', 'error');
    }
}

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
            aplicarBusquedaYFiltros();
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
    document.getElementById('modificar-nombre').value = estudiante.persona.nombre || '';
    document.getElementById('modificar-apellido').value = estudiante.persona.apellido || '';
    document.getElementById('modificar-documento').value = estudiante.persona.documento || '';

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
            aplicarBusquedaYFiltros();
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
            aplicarBusquedaYFiltros();
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

window.onclick = function (event) {
    if (event.target.classList.contains('modal')) {
        event.target.classList.remove('show');
    }
}