package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.modelo.TokenUsuario;
import com.udistrital.gestionacademica.servicio.TokenUsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/token_usuarios")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TokenUsuarioController {
    
    private final TokenUsuarioService tokenUsuarioService;

    @GetMapping
    public ResponseEntity<TokenUsuario> validarTokenUsuario(@RequestBody TokenUsuario tokenUsuario) {
        TokenUsuario nuevoTokenUsuario = tokenUsuarioService.validarTokenUsuario(tokenUsuario.getNombreUsuario(), tokenUsuario.getContrasena());
        return new ResponseEntity<>(nuevoTokenUsuario, HttpStatus.CREATED);
    }

}
