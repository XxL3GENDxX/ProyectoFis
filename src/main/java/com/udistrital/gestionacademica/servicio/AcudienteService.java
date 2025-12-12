package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.Acudiente;
import com.udistrital.gestionacademica.modelo.Persona;
import com.udistrital.gestionacademica.modelo.TokenUsuario;
import com.udistrital.gestionacademica.repositorio.AcudienteRepository;
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
public class AcudienteService {

    private final AcudienteRepository acudienteRepository;
    private final TokenUsuarioRepository tokenUsuarioRepository;

    public Acudiente crearAcudiente(Acudiente acudiente) {
        try {
            // Validar que tenga persona asociada
            if (acudiente.getPersona() == null || acudiente.getPersona().getIdPersona() == null) {
                throw new RuntimeException("DATOS_INVALIDOS: La persona del acudiente es obligatoria");
            }

            // Validar correo electrónico
            if (acudiente.getCorreoElectronico() == null || acudiente.getCorreoElectronico().isEmpty()) {
                throw new RuntimeException("DATOS_INVALIDOS: El correo electrónico es obligatorio");
            }

            // Establecer estado por defecto si no viene
            if (acudiente.getEstado() == null || acudiente.getEstado().isEmpty()) {
                acudiente.setEstado("Pendiente");
            }

            log.info("Creando acudiente con correo: {}", acudiente.getCorreoElectronico());
            Acudiente nuevoAcudiente = acudienteRepository.save(acudiente);
            log.info("Acudiente creado exitosamente con ID: {}", nuevoAcudiente.getIdAcudiente());

            return nuevoAcudiente;

        } catch (RuntimeException e) {
            if (e.getMessage().startsWith("DATOS_INVALIDOS")) {
                throw e;
            }
            log.error("Error al crear acudiente: {}", e.getMessage(), e);
            throw new RuntimeException("Error en la base de datos: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<Acudiente> obtenerTodosLosAcudientes() {
        return acudienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Acudiente obtenerAcudientePorId(Long id) {
        return acudienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Acudiente no encontrado con id: " + id));
    }

    /**
     * Obtener Acudiente por nombre de usuario
     */
    @Transactional(readOnly = true)
    public Acudiente obtenerAcudientePorUsuario(String nombreUsuario) {
        log.info("Buscando acudiente para usuario: {}", nombreUsuario);
        
        // Buscar el usuario
        TokenUsuario usuario = tokenUsuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + nombreUsuario));
        
        // Obtener la persona asociada
        Persona persona = usuario.getPersona();
        if (persona == null) {
            throw new RuntimeException("El usuario " + nombreUsuario + " no tiene persona asociada");
        }
        
        // Buscar el acudiente por persona
        Acudiente acudiente = acudienteRepository.findByPersona(persona.getIdPersona())
                .orElseThrow(() -> new RuntimeException("El usuario " + nombreUsuario + " no tiene perfil de acudiente"));
        
        log.info("Acudiente encontrado con ID: {}", acudiente.getIdAcudiente());
        return acudiente;
    }
}
