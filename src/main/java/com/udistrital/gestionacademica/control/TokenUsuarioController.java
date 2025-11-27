package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.modelo.TokenUsuario;
import com.udistrital.gestionacademica.servicio.TokenUsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/api/token_usuario")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500", "http://127.0.0.1:8080","http://localhost:8080"}) 
public class TokenUsuarioController {
    
    private final TokenUsuarioService tokenUsuarioService;

    @PostMapping("/validarLogin")    
    public ResponseEntity<?> validarTokenUsuario(@RequestBody TokenUsuario tokenUsuario) {
        try {
            
            if (tokenUsuario.getNombreUsuario() == null || tokenUsuario.getNombreUsuario().isEmpty()) {
                return new ResponseEntity<>(
                    Map.of("error", true, "mensaje", "El nombre de usuario es obligatorio"),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            if (tokenUsuario.getContrasena() == null || tokenUsuario.getContrasena().isEmpty()) {
                return new ResponseEntity<>(
                    Map.of("error", true, "mensaje", "La contraseña es obligatoria"),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            TokenUsuario nuevoTokenUsuario = tokenUsuarioService.validarTokenUsuario(
                tokenUsuario.getNombreUsuario(), 
                tokenUsuario.getContrasena()
            );
            
            return new ResponseEntity<>(nuevoTokenUsuario, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                Map.of("error", true, "mensaje", "Usuario o contraseña inválidos"),
                HttpStatus.UNAUTHORIZED
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of("error", true, "mensaje", "Error al validar login: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

}
