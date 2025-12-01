package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.modelo.Periodo;
import com.udistrital.gestionacademica.servicio.PeriodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/periodos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PeriodoController {

    private final PeriodoService periodoService;

    /**
     * Obtener todos los períodos
     */
    @GetMapping
    public ResponseEntity<List<Periodo>> obtenerTodos() {
        log.info("Obteniendo todos los periodos");
        List<Periodo> periodos = periodoService.obtenerTodosPeriodos();
        return ResponseEntity.ok(periodos);
    }


    // Clases auxiliares para respuestas
    public static class ErrorResponse {
        public String tipo;
        public String mensaje;

        public ErrorResponse(String tipo, String mensaje) {
            this.tipo = tipo;
            this.mensaje = mensaje;
        }

        public String getTipo() { return tipo; }
        public String getMensaje() { return mensaje; }
    }

    public static class SuccessResponse {
        public String mensaje;

        public SuccessResponse(String mensaje) {
            this.mensaje = mensaje;
        }

        public String getMensaje() { return mensaje; }
    }
}
