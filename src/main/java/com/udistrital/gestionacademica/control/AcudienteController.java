package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.modelo.Acudiente;
import com.udistrital.gestionacademica.servicio.AcudienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/acudiente")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})
public class AcudienteController {

    private final AcudienteService acudienteService;

    @PostMapping("/crear")
    public ResponseEntity<Acudiente> crearAcudiente(@RequestBody Acudiente acudiente) {
        return ResponseEntity.ok(acudienteService.crearAcudiente(acudiente));
    }
}
