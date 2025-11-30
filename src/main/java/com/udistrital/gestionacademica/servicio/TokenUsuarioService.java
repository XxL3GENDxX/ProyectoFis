package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.TokenUsuario;
import com.udistrital.gestionacademica.repositorio.TokenUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TokenUsuarioService {

    private final TokenUsuarioRepository tokenUsuarioRepository;

    
    public TokenUsuario validarTokenUsuario(String nombreUsuario, String contrasena) {
        
        var usuario = tokenUsuarioRepository.findByNombreUsuarioAndContrasena(nombreUsuario, contrasena);
        
        if (usuario.isEmpty()) {
            log.warn("Usuario '{}' no encontrado o contraseña incorrecta", nombreUsuario);
            throw new IllegalArgumentException("Token inválido o no encontrado");
        }
        
        // Validar que el usuario esté habilitado (estado = true)
        TokenUsuario usuarioEncontrado = usuario.get();
        if (!usuarioEncontrado.getEstado()) {
            log.warn("Intento de login con usuario inhabilitado: {}", nombreUsuario);
            throw new IllegalArgumentException("Usuario inhabilitado");
        }
        
        log.info("Usuario validado correctamente: {}", nombreUsuario);
        return usuarioEncontrado;
    }

    // Obtener todos los usuarios
    public List<TokenUsuario> obtenerTodos() {
        log.info("Obteniendo todos los usuarios");
        return tokenUsuarioRepository.findAll();
    }

    // Obtener usuario por ID
    public TokenUsuario obtenerPorId(Long id) {
        log.info("Obteniendo usuario con ID: {}", id);
        return tokenUsuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
    }

    // Crear nuevo usuario
    public TokenUsuario crear(TokenUsuario tokenUsuario) {
        log.info("Creando nuevo usuario: {}", tokenUsuario.getNombreUsuario());
        
        // Validar que el nombre de usuario sea único
        if (tokenUsuarioRepository.findByNombreUsuario(tokenUsuario.getNombreUsuario()).isPresent()) {
            log.warn("Intento de crear usuario con nombre de usuario duplicado: {}", tokenUsuario.getNombreUsuario());
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }
        
        if (tokenUsuario.getEstado() == null) {
            tokenUsuario.setEstado(true);
        }
        
        TokenUsuario usuarioGuardado = tokenUsuarioRepository.save(tokenUsuario);
        log.info("Usuario creado exitosamente con ID: {}", usuarioGuardado.getIdTokenUsuario());
        return usuarioGuardado;
    }

    // Editar usuario
    public TokenUsuario editar(Long id, TokenUsuario tokenUsuarioActualizado) {
        log.info("Editando usuario con ID: {}", id);
        
        TokenUsuario usuario = tokenUsuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
        
        if (tokenUsuarioActualizado.getNombreUsuario() != null && !tokenUsuarioActualizado.getNombreUsuario().isEmpty()) {
            // Validar que el nuevo nombre de usuario no exista en otro usuario
            var usuarioConNombreDuplicado = tokenUsuarioRepository.findByNombreUsuario(tokenUsuarioActualizado.getNombreUsuario());
            if (usuarioConNombreDuplicado.isPresent() && !usuarioConNombreDuplicado.get().getIdTokenUsuario().equals(id)) {
                log.warn("Intento de editar usuario con nombre de usuario duplicado: {}", tokenUsuarioActualizado.getNombreUsuario());
                throw new IllegalArgumentException("El nombre de usuario ya existe");
            }
            usuario.setNombreUsuario(tokenUsuarioActualizado.getNombreUsuario());
        }
        
        if (tokenUsuarioActualizado.getContrasena() != null && !tokenUsuarioActualizado.getContrasena().isEmpty()) {
            usuario.setContrasena(tokenUsuarioActualizado.getContrasena());
        }
        
        if (tokenUsuarioActualizado.getEstado() != null) {
            usuario.setEstado(tokenUsuarioActualizado.getEstado());
        }
        
        if (tokenUsuarioActualizado.getRol() != null && !tokenUsuarioActualizado.getRol().isEmpty()) {
            usuario.setRol(tokenUsuarioActualizado.getRol());
        }
        
        TokenUsuario usuarioActualizado = tokenUsuarioRepository.save(usuario);
        log.info("Usuario editado exitosamente con ID: {}", id);
        return usuarioActualizado;
    }

    // Cambiar estado del usuario
    public TokenUsuario cambiarEstado(Long id) {
        log.info("Cambiando estado del usuario con ID: {}", id);
        
        TokenUsuario usuario = tokenUsuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
        
        usuario.setEstado(!usuario.getEstado());
        TokenUsuario usuarioActualizado = tokenUsuarioRepository.save(usuario);
        log.info("Estado del usuario {} cambiado a: {}", id, usuarioActualizado.getEstado());
        return usuarioActualizado;
    }

    // Cambiar contraseña
    public TokenUsuario cambiarPassword(Long id, String nuevaContrasena) {
        log.info("Cambiando contraseña del usuario con ID: {}", id);
        
        TokenUsuario usuario = tokenUsuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
        
        usuario.setContrasena(nuevaContrasena);
        TokenUsuario usuarioActualizado = tokenUsuarioRepository.save(usuario);
        log.info("Contraseña del usuario {} cambiada exitosamente", id);
        return usuarioActualizado;
    }

}
