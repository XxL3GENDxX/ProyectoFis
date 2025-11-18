package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.modelo.Estudiante;
import com.udistrital.gestionacademica.servicio.EstudianteService;
// import com.udistrital.gestionacademica.servicio.EstudianteService.EstadisticasDTO;
//import com.udistrital.gestionacademica.dto.FiltroEstudianteDTO;
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

    /**
     * Obtiene todos los estudiantes GET /api/estudiantes
     */
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


    // Métodos auxiliares para respuestas
    private Map<String, Object> crearRespuestaError(String mensaje) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("mensaje", mensaje);
        return response;
    }


}
