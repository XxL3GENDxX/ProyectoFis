package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.Persona;
import com.udistrital.gestionacademica.modelo.TokenUsuario;
import com.udistrital.gestionacademica.repositorio.PersonaRepository;
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
    private final PersonaRepository personaRepository;

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

    // Método para crear usuario
    public TokenUsuario crearUsuario(TokenUsuario usuario) {

        // 1. Validamos que venga el objeto persona y su documento
        if (usuario.getPersona() == null || usuario.getPersona().getDocumento() == null || usuario.getPersona().getDocumento().isEmpty()) {
            throw new IllegalArgumentException("El documento de la persona es obligatorio");
        }

        // 2. Sacamos el documento que vino desde el Frontend
        String documentoBusqueda = usuario.getPersona().getDocumento();

        // 3. Buscamos la Persona REAL en la base de datos
        Persona personaReal = personaRepository.findByDocumento(documentoBusqueda)
                .orElseThrow(() -> new IllegalArgumentException("No existe una persona con el documento: " + documentoBusqueda));

        // 4. Reemplazamos el objeto persona "parcial" por la persona real de la BD
        usuario.setPersona(personaReal);

        // 5. Configuramos otros valores por defecto si es necesario
        usuario.setEstado(true);

        // 6. Guardamos
        log.info("Creando usuario '{}' para la persona con documento {}", usuario.getNombreUsuario(), documentoBusqueda);
        return tokenUsuarioRepository.save(usuario);
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
