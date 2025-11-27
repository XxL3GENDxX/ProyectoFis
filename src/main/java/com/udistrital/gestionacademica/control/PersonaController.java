package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.modelo.Persona;
import com.udistrital.gestionacademica.servicio.PersonaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/persona")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})  // AGREGADO CORS
public class PersonaController {

    private final PersonaService personaService;

    @PostMapping("/crear")
    public ResponseEntity<Persona> crearPersona(@RequestBody Persona persona) {
        try {
            log.info("Creando persona: {} {}", persona.getNombre(), persona.getApellido());
            Persona nuevaPersona = personaService.crearPersona(persona);
            log.info("Persona creada exitosamente con ID: {}", nuevaPersona.getIdPersona());
            return new ResponseEntity<>(nuevaPersona, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error al crear persona: {}", e.getMessage(), e);
            throw e;
        }
    }
}
