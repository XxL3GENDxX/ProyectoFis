package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.modelo.Grupo;
import com.udistrital.gestionacademica.servicio.GrupoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grupos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GrupoController {

    private final GrupoService grupoService;

    @GetMapping("/grado/{idGrado}")
    public ResponseEntity<?> obtenerGruposPorGrado(@PathVariable Long idGrado) {
        try {
            List<Grupo> grupos = grupoService.obtenerGruposPorGrado(idGrado);
            return ResponseEntity.ok(grupos);
        } catch (Exception e) {
            log.error("Error al obtener grupos del grado", e);
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