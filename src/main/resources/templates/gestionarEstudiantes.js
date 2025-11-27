// Configuración de la API
const API_URL = 'http://localhost:8080/api/estudiante';
const API_GRADOS_URL = 'http://localhost:8080/api/grados';
const API_GRUPOS_URL = 'http://localhost:8080/api/grupos';

// ... resto del código igual ...

// Mostrar tabla de estudiantes - ACTUALIZADO
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
        
        // CAMBIO CLAVE: Acceder a los datos a través de persona
        const nombre = estudiante.persona ? estudiante.persona.nombre : 'N/A';
        const apellido = estudiante.persona ? estudiante.persona.apellido : 'N/A';
        
        tr.innerHTML = `
            <td>${nombre}</td>
            <td>${apellido}</td>
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

// Generar botones de opciones según el modo - ACTUALIZADO
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
        
        // CAMBIO: Usar datos de persona
        const nombreCompleto = estudiante.persona ? 
            `${estudiante.persona.nombre} ${estudiante.persona.apellido}` : 
            'Estudiante';
        const btnDesvincularOnclick = !esActivo || !tieneGrupo ? '' : 
            `onclick="desvincularGrupo(${estudiante.codigoEstudiante}, '${nombreCompleto}')"`;
        
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
        
        // CAMBIO: Usar datos de persona
        const nombreCompleto = estudiante.persona ? 
            `${estudiante.persona.nombre} ${estudiante.persona.apellido}` : 
            'Estudiante';
        
        return `
            <button class="${btnEditarClass}" ${btnEditarOnclick} title="${btnEditarTitle}" ${btnEditarDisabled}>
                <i class="fas fa-edit"></i>
            </button>
            <label class="switch-estado" title="Gestionar estado del estudiante">
                <input type="checkbox" ${switchChecked} onchange="cambiarEstadoEstudiante(${estudiante.codigoEstudiante}, '${nombreCompleto}')">
                <span class="slider"></span>
            </label>
        `;
    }
}

// ... resto de las funciones igual, solo necesitas asegurarte de:
// 1. Acceder a datos de persona cuando sea necesario
// 2. En cargarDatosEnFormularioModificar también usar estudiante.persona

function cargarDatosEnFormularioModificar(estudiante) {
    // CAMBIO: Acceder a través de persona
    document.getElementById('modificar-nombre').value = estudiante.persona?.nombre || '';
    document.getElementById('modificar-apellido').value = estudiante.persona?.apellido || '';
    document.getElementById('modificar-documento').value = estudiante.persona?.documento || '';
    
    if (estudiante.persona?.fechaDeNacimiento) {
        const fecha = new Date(estudiante.persona.fechaDeNacimiento);
        const fechaFormateada = fecha.toISOString().split('T')[0];
        document.getElementById('modificar-fecha-nacimiento').value = fechaFormateada;
    } else {
        document.getElementById('modificar-fecha-nacimiento').value = '';
    }
}