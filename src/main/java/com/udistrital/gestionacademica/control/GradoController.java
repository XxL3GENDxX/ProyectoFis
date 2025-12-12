package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.modelo.Grado;
import com.udistrital.gestionacademica.servicio.GradoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grados")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GradoController {

    private final GradoService gradoService;

    @GetMapping
    public ResponseEntity<?> obtenerTodosLosGrados() {
        try {
            List<Grado> grados = gradoService.obtenerTodosLosGrados();
            return ResponseEntity.ok(grados);
        } catch (Exception e) {
            log.error("Error al obtener grados", e);
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
}