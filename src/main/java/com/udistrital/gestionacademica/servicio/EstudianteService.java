package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.Estudiante;
import com.udistrital.gestionacademica.modelo.Grupo;
import com.udistrital.gestionacademica.modelo.Acudiente;

import com.udistrital.gestionacademica.repositorio.EstudianteRepository;
import com.udistrital.gestionacademica.repositorio.GrupoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final GrupoRepository grupoRepository;
    @Autowired
    private final AcudienteService acudienteService;

    // Límite de estudiantes por grupo - Regla de negocio
    private static final int LIMITE_ESTUDIANTES_POR_GRUPO = 10;

    // Agrega este método corregido a tu EstudianteService.java
    // Agrega este método corregido a tu EstudianteService.java
    public Estudiante crearEstudiante(Estudiante estudiante, Long idAcudiente) {
        try {
            // Validar que el estudiante tenga persona asociada
            if (estudiante.getPersona() == null || estudiante.getPersona().getIdPersona() == null) {
                throw new RuntimeException("DATOS_INVALIDOS: La persona del estudiante es obligatoria");
            }

            // Validar datos del estudiante (ya no necesitamos validar nombre/apellido aquí
            // porque la persona ya fue creada previamente)
            // Verificar documento duplicado
            Optional<Estudiante> estudianteExistente = estudianteRepository
                    .findByDocumentoEstudiante(estudiante.getPersona().getDocumento());

            if (estudianteExistente.isPresent()) {
                throw new RuntimeException("DOCUMENTO_DUPLICADO");
            }

            // Obtener el acudiente
            Acudiente acudiente = acudienteService.obtenerAcudientePorId(idAcudiente);
            estudiante.setAcudiente(acudiente);

            // Establecer estado por defecto si no viene
            if (estudiante.getEstado() == null || estudiante.getEstado().isEmpty()) {
                estudiante.setEstado("Pendiente");
            }

            // Guardar el estudiante
            Estudiante nuevoEstudiante = estudianteRepository.save(estudiante);
            log.info("Estudiante creado exitosamente con código: {}", nuevoEstudiante.getCodigoEstudiante());

            return nuevoEstudiante;

        } catch (RuntimeException e) {
            if ("DATOS_INVALIDOS".equals(e.getMessage())
                    || "DOCUMENTO_DUPLICADO".equals(e.getMessage())) {
                throw e;
            }
            log.error("Error al crear estudiante: {}", e.getMessage(), e);
            throw new RuntimeException("Error en la base de datos: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<Estudiante> obtenerTodosLosEstudiantes() {
        log.info("Obteniendo todos los estudiantes");
        try {
            return estudianteRepository.findAllOrdenados();
        } catch (Exception e) {
            log.error("Error al obtener estudiantes: {}", e.getMessage());
            throw new RuntimeException("Error en la base de datos al obtener estudiantes", e);
        }
    }

    /**
     * Buscar y filtrar estudiantes según los criterios especificados Implementa
     * el caso de uso "Mostrar Estudiantes por Filtro"
     *
     * Paso 1: Barra de búsqueda - búsqueda por nombre, apellido, documento Paso
     * 3-5: Filtros opcionales - género, rango de edad, orden alfabético Paso 7:
     * Consulta de estudiantes en el datastore Paso 8-9: Retorna los resultados
     * encontrados
     *
     * Reglas de negocio: - Los filtros son opcionales y pueden combinarse - La
     * búsqueda es insensible a mayúsculas/minúsculas - El orden alfabético se
     * aplica sobre nombre completo (nombre + apellido) - Si se diligencia la
     * barra de búsqueda, el filtro se realiza sobre los resultados de búsqueda
     * - Si no se diligencia, el filtro se realiza sobre todos los estudiantes
     *
     * @param textoBusqueda Texto de búsqueda (puede ser nombre, apellido o
     * documento)
     * @param genero Filtro por género (opcional)
     * @param edadMinima Edad mínima del rango (opcional)
     * @param edadMaxima Edad máxima del rango (opcional)
     * @param ordenAlfabetico Aplicar orden alfabético A-Z (opcional, por
     * defecto true)
     * @return Lista de estudiantes que cumplen los criterios
     * @throws RuntimeException si ocurre un error en la base de datos
     */
    @Transactional(readOnly = true)
    public List<Estudiante> buscarYFiltrarEstudiantes(
            String textoBusqueda,
            String genero,
            Integer edadMinima,
            Integer edadMaxima,
            Boolean ordenAlfabetico) {

        try {
            // Paso 7: Consultar estudiantes del datastore
            List<Estudiante> estudiantes;

            // Regla de negocio: Si se diligencia la barra de búsqueda, filtrar sobre la búsqueda
            // Si no, filtrar sobre todos los estudiantes
            if (textoBusqueda != null && !textoBusqueda.trim().isEmpty()) {
                log.info("Aplicando búsqueda con texto: {}", textoBusqueda);
                estudiantes = buscarPorTexto(textoBusqueda);
            } else {
                log.info("Obteniendo todos los estudiantes para aplicar filtros");
                estudiantes = estudianteRepository.findAll();
            }

            // Paso 4-5: Aplicar filtros opcionales seleccionados por el actor
            // Filtro por género (opcional)
            if (genero != null && !genero.trim().isEmpty()) {
                log.info("Aplicando filtro de género: {}", genero);
                estudiantes = filtrarPorGenero(estudiantes, genero);
            }

            // Filtro por rango de edad (opcional)
            if (edadMinima != null || edadMaxima != null) {
                log.info("Aplicando filtro de rango de edad: {} - {}", edadMinima, edadMaxima);
                estudiantes = filtrarPorRangoEdad(estudiantes, edadMinima, edadMaxima);
            }

            // Orden alfabético (opcional, por defecto aplicado)
            // Regla de negocio: El orden alfabético se aplica sobre el nombre completo
            if (ordenAlfabetico != null && ordenAlfabetico) {
                log.info("Aplicando orden alfabético A-Z");
                estudiantes = ordenarAlfabeticamente(estudiantes);
            }

            log.info("Búsqueda y filtrado completado. Estudiantes encontrados: {}", estudiantes.size());
            return estudiantes;

        } catch (Exception e) {
            // Flujo alternativo: Error al conectar con la base de datos
            log.error("Error al buscar y filtrar estudiantes: {}", e.getMessage());
            throw new RuntimeException("Error en la base de datos", e);
        }
    }

    /**
     * Búsqueda por texto en nombre, apellido o documento Regla de negocio: La
     * búsqueda es insensible a mayúsculas/minúsculas
     */
    private List<Estudiante> buscarPorTexto(String texto) {
        String textoLower = texto.toLowerCase().trim();
        return estudianteRepository.findAll().stream()
                .filter(est
                        -> est.getPersona().getNombre().toLowerCase().contains(textoLower)
                || est.getPersona().getApellido().toLowerCase().contains(textoLower)
                || (est.getPersona().getDocumento() != null && est.getPersona().getDocumento().toLowerCase().contains(textoLower))
                )
                .collect(Collectors.toList());
    }

    /**
     * Filtrar por género
     */
    private List<Estudiante> filtrarPorGenero(List<Estudiante> estudiantes, String genero) {
        return estudiantes.stream()
                .filter(est -> est.getPersona().getGenero() != null
                && est.getPersona().getGenero().equalsIgnoreCase(genero.trim()))
                .collect(Collectors.toList());
    }

    /**
     * Filtrar por rango de edad Regla de negocio: La edad máxima debe ser mayor
     * o igual a la edad mínima
     */
    private List<Estudiante> filtrarPorRangoEdad(List<Estudiante> estudiantes, Integer edadMinima, Integer edadMaxima) {
        return estudiantes.stream()
                .filter(est -> {
                    Integer edad = est.getPersona().calcularEdad();
                    if (edad == null) {
                        return false;
                    }

                    boolean cumpleMinima = edadMinima == null || edad >= edadMinima;
                    boolean cumpleMaxima = edadMaxima == null || edad <= edadMaxima;

                    return cumpleMinima && cumpleMaxima;
                })
                .collect(Collectors.toList());
    }

    /**
     * Ordenar alfabéticamente A-Z Regla de negocio: El orden se aplica sobre el
     * nombre completo (nombre + apellido)
     */
    private List<Estudiante> ordenarAlfabeticamente(List<Estudiante> estudiantes) {
        return estudiantes.stream()
                .sorted(Comparator
                        // Cambio clave: Accedemos a getPersona() y luego a getApellido()
                        .comparing((Estudiante e) -> e.getPersona().getApellido(), String.CASE_INSENSITIVE_ORDER)
                        // Hacemos lo mismo para el nombre
                        .thenComparing(e -> e.getPersona().getNombre(), String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public Estudiante asignarEstudianteAGrupo(Long codigoEstudiante, Long idGrupo) {
        log.info("Asignando estudiante {} al grupo {}", codigoEstudiante, idGrupo);

        try {
            Estudiante estudiante = estudianteRepository.findById(codigoEstudiante)
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

            Grupo grupo = grupoRepository.findById(idGrupo)
                    .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

            // Validar que el grupo no esté completo usando la constante
            if (grupo.getNumeroEstudiantes() >= LIMITE_ESTUDIANTES_POR_GRUPO) { // Updated validation
                log.warn("El grupo {} está completo (límite: {} estudiantes)", idGrupo, LIMITE_ESTUDIANTES_POR_GRUPO); // Updated log
                throw new RuntimeException("GRUPO_COMPLETO");
            }

            estudiante.setGrupo(grupo);
            Estudiante estudianteActualizado = estudianteRepository.save(estudiante);

            log.info("Estudiante {} asignado exitosamente al grupo {}", codigoEstudiante, idGrupo);
            return estudianteActualizado;

        } catch (RuntimeException e) {
            if ("GRUPO_COMPLETO".equals(e.getMessage())) {
                throw e;
            }
            log.error("Error al asignar estudiante al grupo: {}", e.getMessage());
            throw new RuntimeException("Error en la base de datos", e);
        }
    }

    public Estudiante desvincularEstudianteDeGrupo(Long codigoEstudiante) {
        log.info("Desvinculando estudiante {} de su grupo", codigoEstudiante);

        try {
            Estudiante estudiante = estudianteRepository.findById(codigoEstudiante)
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

            if (estudiante.getGrupo() == null) {
                log.warn("El estudiante {} no tiene grupo asignado", codigoEstudiante);
                throw new RuntimeException("ESTUDIANTE_SIN_GRUPO");
            }

            estudiante.setGrupo(null);
            Estudiante estudianteActualizado = estudianteRepository.save(estudiante);

            log.info("Estudiante {} desvinculado exitosamente de su grupo", codigoEstudiante);
            return estudianteActualizado;

        } catch (RuntimeException e) {
            if ("ESTUDIANTE_SIN_GRUPO".equals(e.getMessage())) {
                throw e;
            }
            log.error("Error al desvincular estudiante del grupo: {}", e.getMessage());
            throw new RuntimeException("Error en la base de datos", e);
        }
    }

    public Estudiante modificarEstudiante(Long codigoEstudiante, Estudiante estudianteModificado) {
        log.info("Modificando estudiante con código: {}", codigoEstudiante);

        try {
            Estudiante estudianteExistente = estudianteRepository.findById(codigoEstudiante)
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

            validarDatosEstudiante(estudianteModificado);

            if (estudianteModificado.getPersona().getDocumento() != null
                    && !estudianteModificado.getPersona().getDocumento().isEmpty()) {

                Optional<Estudiante> estudianteConDocumento
                        = estudianteRepository.findByDocumentoEstudiante(estudianteModificado.getPersona().getDocumento());

                if (estudianteConDocumento.isPresent()
                        && !estudianteConDocumento.get().getCodigoEstudiante().equals(codigoEstudiante)) {
                    log.warn("El documento {} ya existe en otro estudiante",
                            estudianteModificado.getPersona().getDocumento());
                    throw new RuntimeException("DOCUMENTO_DUPLICADO");
                }
            }

            actualizarDatosEstudiante(estudianteExistente, estudianteModificado);
            Estudiante estudianteActualizado = estudianteRepository.save(estudianteExistente);

            log.info("Estudiante {} modificado exitosamente", codigoEstudiante);
            return estudianteActualizado;

        } catch (RuntimeException e) {
            if ("DATOS_INVALIDOS".equals(e.getMessage())
                    || "DOCUMENTO_DUPLICADO".equals(e.getMessage())) {
                throw e;
            }
            log.error("Error al modificar estudiante: {}", e.getMessage());
            throw new RuntimeException("Error en la base de datos", e);
        }
    }

    private void validarDatosEstudiante(Estudiante estudiante) {
        StringBuilder errores = new StringBuilder();

        if (estudiante.getPersona().getNombre() == null || estudiante.getPersona().getNombre().trim().isEmpty()) {
            errores.append("El nombre es obligatorio. ");
        } else if (estudiante.getPersona().getNombre().trim().length() < 2) {
            errores.append("El nombre debe tener al menos 2 caracteres. ");
        }

        if (estudiante.getPersona().getApellido() == null || estudiante.getPersona().getApellido().trim().isEmpty()) {
            errores.append("El apellido es obligatorio. ");
        } else if (estudiante.getPersona().getApellido().trim().length() < 2) {
            errores.append("El apellido debe tener al menos 2 caracteres. ");
        }

        if (estudiante.getPersona().getDocumento() == null || estudiante.getPersona().getDocumento().trim().isEmpty()) {
            errores.append("El documento es obligatorio. ");
        } else if (!estudiante.getPersona().getDocumento().matches("\\d+")) {
            errores.append("El documento debe contener solo números. ");
        } else if (estudiante.getPersona().getDocumento().trim().length() < 6
                || estudiante.getPersona().getDocumento().trim().length() > 20) {
            errores.append("El documento debe tener entre 6 y 20 dígitos. ");
        }

        if (errores.length() > 0) {
            log.warn("Datos inválidos: {}", errores.toString());
            throw new RuntimeException("DATOS_INVALIDOS: " + errores.toString().trim());
        }
    }

    private void actualizarDatosEstudiante(Estudiante existente, Estudiante modificado) {
        existente.getPersona().setNombre(modificado.getPersona().getNombre().trim());
        existente.getPersona().setApellido(modificado.getPersona().getApellido().trim());
        existente.getPersona().setDocumento(modificado.getPersona().getDocumento().trim());

        if (modificado.getPersona().getFechaDeNacimiento() != null) {
            existente.getPersona().setFechaDeNacimiento(modificado.getPersona().getFechaDeNacimiento());
        }
    }

    @Transactional(readOnly = true)
    public Estudiante obtenerEstudiantePorCodigo(Long codigoEstudiante) {
        log.info("Obteniendo estudiante con código: {}", codigoEstudiante);
        return estudianteRepository.findById(codigoEstudiante)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
    }

    public Estudiante cambiarEstadoEstudiante(Long codigoEstudiante) {
        log.info("Cambiando estado del estudiante con código: {}", codigoEstudiante);

        try {
            Estudiante estudiante = estudianteRepository.findById(codigoEstudiante)
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

            String estadoActual = estudiante.getEstado();
            log.info("Estado actual del estudiante {}: {}", codigoEstudiante, estadoActual);

            if ("Inactivo".equalsIgnoreCase(estadoActual)) {
                estudiante.setEstado("Activo");
                log.info("Cambiando estado a Activo para estudiante {}", codigoEstudiante);
            } else {
                estudiante.setEstado("Inactivo");
                log.info("Cambiando estado a Inactivo para estudiante {}", codigoEstudiante);
            }

            Estudiante estudianteActualizado = estudianteRepository.save(estudiante);

            log.info("Estado del estudiante {} actualizado exitosamente a: {}",
                    codigoEstudiante, estudianteActualizado.getEstado());

            return estudianteActualizado;

        } catch (RuntimeException e) {
            if ("Estudiante no encontrado".equals(e.getMessage())) {
                throw e;
            }
            log.error("Error al cambiar estado del estudiante: {}", e.getMessage());
            throw new RuntimeException("Error en la base de datos", e);
        }
    }

    // Agregar este método al EstudianteService existente:
    /**
     * Cambiar estado del estudiante directamente a un valor específico (no
     * toggle, sino asignar el estado exacto)
     */
    public Estudiante cambiarEstadoEstudianteDirecto(Long codigoEstudiante, String nuevoEstado) {
        log.info("Cambiando estado del estudiante {} a: {}", codigoEstudiante, nuevoEstado);

        try {
            Estudiante estudiante = estudianteRepository.findById(codigoEstudiante)
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

            // Validar que el estado sea válido
            List<String> estadosValidos = Arrays.asList("Pendiente", "Activo", "Inactivo", "Rechazado");

            if (!estadosValidos.contains(nuevoEstado)) {
                throw new RuntimeException("Estado no válido: " + nuevoEstado);
            }

            log.info("Estado actual del estudiante {}: {}", codigoEstudiante, estudiante.getEstado());
            log.info("Cambiando a: {}", nuevoEstado);

            estudiante.setEstado(nuevoEstado);
            Estudiante estudianteActualizado = estudianteRepository.save(estudiante);

            log.info("Estado del estudiante {} actualizado exitosamente a: {}",
                    codigoEstudiante, estudianteActualizado.getEstado());

            return estudianteActualizado;

        } catch (RuntimeException e) {
            if ("Estudiante no encontrado".equals(e.getMessage())) {
                throw e;
            }
            log.error("Error al cambiar estado del estudiante: {}", e.getMessage());
            throw new RuntimeException("Error en la base de datos", e);
        }
    }

    /**
     * Obtener lista de estudiantes asignados a un grupo específico
     */
    @Transactional(readOnly = true)
    public List<Estudiante> obtenerEstudiantesPorGrupo(Long idGrupo) {
        log.info("Buscando estudiantes del grupo ID: {}", idGrupo);

        // Llamamos al método que acabamos de crear en el repositorio
        List<Estudiante> estudiantes = estudianteRepository.buscarPorIdGrupo(idGrupo);

        if (estudiantes.isEmpty()) {
            log.warn("No se encontraron estudiantes para el grupo {}", idGrupo);
        }

        return estudiantes;
    }

    /**
     * Obtener estudiantes asignados a un Acudiente específico por nombre de usuario
     */
    @Transactional(readOnly = true)
    public List<Estudiante> obtenerEstudiantesPorAcudiente(String nombreUsuario) {
        log.info("Obteniendo estudiantes para el acudiente usuario: {}", nombreUsuario);
        
        try {
            // Buscar el acudiente a través del servicio
            Acudiente acudiente = acudienteService.obtenerAcudientePorUsuario(nombreUsuario);
            
            // Buscar estudiantes asociados al acudiente
            List<Estudiante> estudiantes = estudianteRepository.findByAcudiente(acudiente.getIdAcudiente());
            
            log.info("Se encontraron {} estudiantes para el acudiente {}", estudiantes.size(), nombreUsuario);
            return estudiantes;
            
        } catch (RuntimeException e) {
            log.error("Error al obtener estudiantes por acudiente: {}", e.getMessage());
            throw e;
        }
    }
}
