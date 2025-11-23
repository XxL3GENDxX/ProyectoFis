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
     * Obtener un estudiante por código
     * Paso 2: El sistema despliega campos con los datos actuales
     */
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
    
    /**
     * Modificar estudiante existente
     * Implementa el caso de uso "Modificar Estudiante"
     * 
     * @param codigoEstudiante Código del estudiante a modificar
     * @param estudiante Datos modificados del estudiante
     * @return ResponseEntity con el resultado de la operación
     */
    @PutMapping("/{codigoEstudiante}")
    public ResponseEntity<?> modificarEstudiante(
            @PathVariable Long codigoEstudiante,
            @RequestBody Estudiante estudiante) {
        try {
            log.info("Modificando estudiante con código: {}", codigoEstudiante);
            
            // Paso 7: El sistema guarda los cambios
            Estudiante estudianteActualizado = estudianteService.modificarEstudiante(
                    codigoEstudiante, 
                    estudiante
            );
            
            // Paso 8: Mensaje de éxito
            return ResponseEntity.ok(crearRespuestaExito(
                    "Estudiante modificado exitosamente", 
                    estudianteActualizado
            ));
            
        } catch (RuntimeException e) {
            log.error("Error al modificar estudiante: {}", e.getMessage());
            
            // Flujo alternativo A: Datos inválidos
            if (e.getMessage().startsWith("DATOS_INVALIDOS")) {
                String detalleError = e.getMessage().replace("DATOS_INVALIDOS: ", "");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(crearRespuestaError("Datos ingresados no válidos: " + detalleError));
            }
            
            // Flujo alternativo B: Documento duplicado
            if ("DOCUMENTO_DUPLICADO".equals(e.getMessage())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(crearRespuestaError("Ya existe un registro con este documento"));
            }
            
            // Flujo alternativo C: Error en la base de datos
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