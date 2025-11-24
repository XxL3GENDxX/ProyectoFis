package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.modelo.Estudiante;
import com.udistrital.gestionacademica.servicio.EstudianteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estudiantes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EstudianteController {

    private final EstudianteService estudianteService;

    @GetMapping
    public ResponseEntity<?> obtenerTodosLosEstudiantes() {
        try {
            List<Estudiante> estudiantes = estudianteService.obtenerTodosLosEstudiantes();
            return ResponseEntity.ok(estudiantes);
        } catch (Exception e) {
            log.error("Error al obtener estudiantes", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }
    
    /**
     * Buscar y filtrar estudiantes
     * Paso 1-9: Implementa el flujo completo del caso de uso "Mostrar Estudiantes por Filtro"
     * 
     * @param textoBusqueda Texto de búsqueda (Paso 1)
     * @param genero Filtro de género (Paso 3-5)
     * @param edadMinima Edad mínima del rango (Paso 3-5)
     * @param edadMaxima Edad máxima del rango (Paso 3-5)
     * @param ordenAlfabetico Orden alfabético A-Z (Paso 3-5)
     * @return Lista de estudiantes filtrados o mensaje de error
     */
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarYFiltrarEstudiantes(
            @RequestParam(required = false) String textoBusqueda,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) Integer edadMinima,
            @RequestParam(required = false) Integer edadMaxima,
            @RequestParam(required = false, defaultValue = "true") Boolean ordenAlfabetico) {
        
        try {
            log.info("Buscando estudiantes con filtros - Búsqueda: {}, Género: {}, EdadMin: {}, EdadMax: {}, Orden: {}", 
                    textoBusqueda, genero, edadMinima, edadMaxima, ordenAlfabetico);
            
            // Paso 7: El sistema consulta los estudiantes según la barra de búsqueda y los filtros aplicados
            List<Estudiante> estudiantes = estudianteService.buscarYFiltrarEstudiantes(
                    textoBusqueda, 
                    genero, 
                    edadMinima, 
                    edadMaxima, 
                    ordenAlfabetico
            );
            
            // Paso 8: El sistema verifica si se encontraron resultados
            if (estudiantes.isEmpty()) {
                // Flujo Alternativo: No se encontraron resultados
                log.info("No se encontraron estudiantes con los criterios especificados");
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(crearRespuestaError("No se encontró ningún estudiante con los criterios especificados"));
            }
            
            // Paso 9: El sistema muestra los estudiantes encontrados
            log.info("Se encontraron {} estudiantes", estudiantes.size());
            return ResponseEntity.ok(estudiantes);
            
        } catch (Exception e) {
            // Flujo Alternativo: Error al conectar con la base de datos
            log.error("Error al buscar y filtrar estudiantes", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }
    
    @GetMapping("/{codigoEstudiante}")
    public ResponseEntity<?> obtenerEstudiantePorCodigo(@PathVariable Long codigoEstudiante) {
        try {
            log.info("Obteniendo estudiante con código: {}", codigoEstudiante);
            Estudiante estudiante = estudianteService.obtenerEstudiantePorCodigo(codigoEstudiante);
            return ResponseEntity.ok(estudiante);
        } catch (RuntimeException e) {
            log.error("Error al obtener estudiante: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(crearRespuestaError("Estudiante no encontrado"));
        }
    }

    @PostMapping("/{codigoEstudiante}/asignar-grupo/{idGrupo}")
    public ResponseEntity<?> asignarEstudianteAGrupo(
            @PathVariable Long codigoEstudiante,
            @PathVariable Long idGrupo) {
        try {
            log.info("Asignando estudiante {} al grupo {}", codigoEstudiante, idGrupo);
            
            Estudiante estudianteActualizado = estudianteService.asignarEstudianteAGrupo(
                    codigoEstudiante, 
                    idGrupo
            );
            
            return ResponseEntity.ok(crearRespuestaExito(
                    "Estudiante asignado exitosamente", 
                    estudianteActualizado
            ));
            
        } catch (RuntimeException e) {
            log.error("Error al asignar estudiante: {}", e.getMessage());
            
            if ("GRUPO_COMPLETO".equals(e.getMessage())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(crearRespuestaError("Grupo completo"));
            }
            
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    @DeleteMapping("/{codigoEstudiante}/desvincular-grupo")
    public ResponseEntity<?> desvincularEstudianteDeGrupo(@PathVariable Long codigoEstudiante) {
        try {
            log.info("Desvinculando estudiante {} de su grupo", codigoEstudiante);
            
            Estudiante estudianteActualizado = estudianteService.desvincularEstudianteDeGrupo(codigoEstudiante);
            
            return ResponseEntity.ok(crearRespuestaExito(
                    "Estudiante desvinculado satisfactoriamente", 
                    estudianteActualizado
            ));
            
        } catch (RuntimeException e) {
            log.error("Error al desvincular estudiante: {}", e.getMessage());
            
            if ("ESTUDIANTE_SIN_GRUPO".equals(e.getMessage())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(crearRespuestaError("El estudiante no tiene grupo asignado"));
            }
            
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }
    
    @PutMapping("/{codigoEstudiante}")
    public ResponseEntity<?> modificarEstudiante(
            @PathVariable Long codigoEstudiante,
            @RequestBody Estudiante estudiante) {
        try {
            log.info("Modificando estudiante con código: {}", codigoEstudiante);
            
            Estudiante estudianteActualizado = estudianteService.modificarEstudiante(
                    codigoEstudiante, 
                    estudiante
            );
            
            return ResponseEntity.ok(crearRespuestaExito(
                    "Estudiante modificado exitosamente", 
                    estudianteActualizado
            ));
            
        } catch (RuntimeException e) {
            log.error("Error al modificar estudiante: {}", e.getMessage());
            
            if (e.getMessage().startsWith("DATOS_INVALIDOS")) {
                String detalleError = e.getMessage().replace("DATOS_INVALIDOS: ", "");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(crearRespuestaError("Datos ingresados no válidos: " + detalleError));
            }
            
            if ("DOCUMENTO_DUPLICADO".equals(e.getMessage())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(crearRespuestaError("Ya existe un registro con este documento"));
            }
            
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    @PatchMapping("/{codigoEstudiante}/cambiar-estado")
    public ResponseEntity<?> cambiarEstadoEstudiante(@PathVariable Long codigoEstudiante) {
        try {
            log.info("Cambiando estado del estudiante con código: {}", codigoEstudiante);
            
            Estudiante estudianteActualizado = estudianteService.cambiarEstadoEstudiante(codigoEstudiante);
            
            return ResponseEntity.ok(crearRespuestaExito(
                    "Estado modificado exitosamente", 
                    estudianteActualizado
            ));
            
        } catch (RuntimeException e) {
            log.error("Error al cambiar estado del estudiante: {}", e.getMessage());
            
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    private Map<String, Object> crearRespuestaError(String mensaje) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("mensaje", mensaje);
        return response;
    }

    private Map<String, Object> crearRespuestaExito(String mensaje, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", false);
        response.put("mensaje", mensaje);
        response.put("data", data);
        return response;
    }
}