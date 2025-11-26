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
@CrossOrigin(origins = "*")

public class PreinscripcionController {
    private final PreinscripcionService preinscripcionService;

    @PostMapping("/crear")
    public ResponseEntity<Preinscripcion> crearPreinscripcion(@RequestBody Preinscripcion preinscripcion) {
        Preinscripcion nuevaPreinscripcion = preinscripcionService.crearPreinscripcion(preinscripcion);
        return new ResponseEntity<>(nuevaPreinscripcion, HttpStatus.CREATED);
    }
    
}
