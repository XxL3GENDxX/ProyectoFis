package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.modelo.Preinscripcion;
import com.udistrital.gestionacademica.servicio.PreinscripcionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preinscripcion")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})  // AGREGADO CORS

public class PreinscripcionController {

    private final PreinscripcionService preinscripcionService;

    @PostMapping("/crear")
    public ResponseEntity<Preinscripcion> crearPreinscripcion(@RequestBody Preinscripcion preinscripcion) {
        try {
            log.info("Creando preinscripción para estudiante: {}",
                    preinscripcion.getAspirante().getCodigoEstudiante());
            Preinscripcion nuevaPreinscripcion = preinscripcionService.crearPreinscripcion(preinscripcion);
            log.info("Preinscripción creada exitosamente con ID: {}", nuevaPreinscripcion.getIdPreinscripcion());
            return new ResponseEntity<>(nuevaPreinscripcion, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error al crear preinscripción: {}", e.getMessage(), e);
            throw e;
        }
    }
}
