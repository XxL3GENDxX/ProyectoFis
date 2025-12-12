package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.servicio.BoletinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/boletin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BoletinController {

    private final BoletinService boletinService;

    /**
     * Generar boletín de calificaciones en PDF y guardarlo
     */
    @GetMapping("/generar/{codigoEstudiante}/{idPeriodo}")
    public ResponseEntity<?> generarBoletin(
            @PathVariable Long codigoEstudiante,
            @PathVariable Long idPeriodo) {
        
        try {
            log.info("Solicitud de boletín para estudiante {} en periodo {}", codigoEstudiante, idPeriodo);

            byte[] pdfBytes = boletinService.generarYGuardarBoletin(codigoEstudiante, idPeriodo);

            // Retornar PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // inline para previsualizar
            headers.setContentDispositionFormData("inline", "boletin_" + codigoEstudiante + "_" + idPeriodo + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.warn("Error de validación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error interno generando boletín", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(crearRespuestaError("Error interno del servidor al generar el boletín."));
        }
    }

    private Map<String, Object> crearRespuestaError(String mensaje) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("mensaje", mensaje);
        return response;
    }
}
