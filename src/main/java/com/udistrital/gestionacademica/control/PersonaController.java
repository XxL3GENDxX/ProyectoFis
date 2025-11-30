package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.modelo.Persona;
import com.udistrital.gestionacademica.servicio.PersonaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/persona")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500", "http://127.0.0.1:8080","http://localhost:8080"}) 
public class PersonaController {

    private final PersonaService personaService;

    @PostMapping("/crear")
    public ResponseEntity<?> crearPersona(@RequestBody Persona persona) {
        try {
            log.info("Creando persona: {} {}", persona.getNombre(), persona.getApellido());
            log.info("Datos recibidos: {}", persona);
            
            // Validar datos obligatorios
            if (persona.getNombre() == null || persona.getNombre().isEmpty()) {
                return new ResponseEntity<>(
                    Map.of("error", true, "mensaje", "El nombre es obligatorio"),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            // El apellido es opcional
            if (persona.getApellido() == null || persona.getApellido().isEmpty()) {
                persona.setApellido("");
            }
            
            Persona nuevaPersona = personaService.crearPersona(persona);
            log.info("Persona creada exitosamente con ID: {}", nuevaPersona.getIdPersona());
            return new ResponseEntity<>(nuevaPersona, HttpStatus.CREATED);
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Error de integridad en datos: {}", e.getMessage());
            return new ResponseEntity<>(
                Map.of("error", true, "mensaje", "El documento ya existe o viola una restricción única"),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            log.error("Error al crear persona: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                Map.of("error", true, "mensaje", "Error al crear persona: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
