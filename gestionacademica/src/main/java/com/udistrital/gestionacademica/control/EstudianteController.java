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
            log.info("Desvinculando estudiante {} del grupo", codigoEstudiante);

            Estudiante estudianteActualizado = estudianteService.desvincularEstudianteDeGrupo(codigoEstudiante);

            return ResponseEntity.ok(crearRespuestaExito(
                    "Estudiante desvinculado satisfactoriamente",
                    estudianteActualizado
            ));

        } catch (RuntimeException e) {
            log.error("Error al desvincular estudiante: {}", e.getMessage());

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