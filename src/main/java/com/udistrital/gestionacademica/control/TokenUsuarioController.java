package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.modelo.TokenUsuario;
import com.udistrital.gestionacademica.servicio.TokenUsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/token_usuario")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500", "http://127.0.0.1:8080", "http://localhost:8080"})
public class TokenUsuarioController {

    private final TokenUsuarioService tokenUsuarioService;

    // Login
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
            // Distinguir entre usuario inhabilitado y credenciales inválidas
            String mensaje = e.getMessage();
            if (mensaje != null && mensaje.contains("inhabilitado")) {
                return new ResponseEntity<>(
                        Map.of("error", true, "mensaje", "Tu usuario está inhabilitado. Contacta con el administrador."),
                        HttpStatus.UNAUTHORIZED
                );
            } else {
                return new ResponseEntity<>(
                        Map.of("error", true, "mensaje", "Usuario o contraseña inválidos"),
                        HttpStatus.UNAUTHORIZED
                );
            }
        } catch (Exception e) {
            return new ResponseEntity<>(
                    Map.of("error", true, "mensaje", "Error al validar login: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // Obtener todos los usuarios
    @GetMapping
    public ResponseEntity<?> obtenerTodos() {
        try {
            List<TokenUsuario> usuarios = tokenUsuarioService.obtenerTodos();
            return new ResponseEntity<>(usuarios, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al obtener usuarios: {}", e.getMessage());
            return new ResponseEntity<>(
                    Map.of("error", true, "mensaje", "Error al cargar usuarios"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // Obtener usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            TokenUsuario usuario = tokenUsuarioService.obtenerPorId(id);
            return new ResponseEntity<>(usuario, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    Map.of("error", true, "mensaje", "Usuario no encontrado"),
                    HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            log.error("Error al obtener usuario: {}", e.getMessage());
            return new ResponseEntity<>(
                    Map.of("error", true, "mensaje", "Error al cargar el usuario"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PostMapping("/crear")
    public ResponseEntity<?> crearUsuario(@RequestBody TokenUsuario usuario) {
        try {
            // Validaciones básicas de usuario y contraseña
            if (usuario.getNombreUsuario() == null || usuario.getContrasena() == null) {
                return new ResponseEntity<>(
                        Map.of("error", true, "mensaje", "Usuario y contraseña son obligatorios"),
                        HttpStatus.BAD_REQUEST
                );
            }

            TokenUsuario nuevoUsuario = tokenUsuarioService.crearUsuario(usuario);
            return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    Map.of("error", true, "mensaje", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            log.error("Error creando usuario: ", e);
            return new ResponseEntity<>(
                    Map.of("error", true, "mensaje", "Error interno al crear usuario"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // Editar usuario
    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody TokenUsuario tokenUsuario) {
        try {
            TokenUsuario usuarioEditado = tokenUsuarioService.editar(id, tokenUsuario);
            return new ResponseEntity<>(usuarioEditado, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            String mensaje = e.getMessage();
            if (mensaje != null && mensaje.contains("ya existe")) {
                return new ResponseEntity<>(
                        Map.of("error", true, "mensaje", "El nombre de usuario ya existe"),
                        HttpStatus.BAD_REQUEST
                );
            } else if (mensaje != null && mensaje.contains("no encontrado")) {
                return new ResponseEntity<>(
                        Map.of("error", true, "mensaje", "Usuario no encontrado"),
                        HttpStatus.NOT_FOUND
                );
            }
            return new ResponseEntity<>(
                    Map.of("error", true, "mensaje", mensaje),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            log.error("Error al editar usuario: {}", e.getMessage());
            return new ResponseEntity<>(
                    Map.of("error", true, "mensaje", "Error al editar el usuario: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // Cambiar estado
    @PatchMapping("/{id}/cambiar-estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id) {
        try {
            TokenUsuario usuarioActualizado = tokenUsuarioService.cambiarEstado(id);
            return new ResponseEntity<>(usuarioActualizado, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    Map.of("error", true, "mensaje", "Usuario no encontrado"),
                    HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            log.error("Error al cambiar estado: {}", e.getMessage());
            return new ResponseEntity<>(
                    Map.of("error", true, "mensaje", "Error al cambiar el estado"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // Cambiar contraseña
    @PatchMapping("/{id}/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String nuevaContrasena = request.get("contrasena");
            if (nuevaContrasena == null || nuevaContrasena.isEmpty()) {
                return new ResponseEntity<>(
                        Map.of("error", true, "mensaje", "La contraseña es obligatoria"),
                        HttpStatus.BAD_REQUEST
                );
            }

            TokenUsuario usuarioActualizado = tokenUsuarioService.cambiarPassword(id, nuevaContrasena);
            return new ResponseEntity<>(usuarioActualizado, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    Map.of("error", true, "mensaje", "Usuario no encontrado"),
                    HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            log.error("Error al cambiar contraseña: {}", e.getMessage());
            return new ResponseEntity<>(
                    Map.of("error", true, "mensaje", "Error al cambiar la contraseña"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

}
