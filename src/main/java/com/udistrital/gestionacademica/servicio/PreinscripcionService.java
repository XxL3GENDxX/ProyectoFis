package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.Acudiente;
import com.udistrital.gestionacademica.modelo.Preinscripcion;
import com.udistrital.gestionacademica.repositorio.AcudienteRepository;
import com.udistrital.gestionacademica.repositorio.PreinscripcionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PreinscripcionService {

    private final PreinscripcionRepository preinscripcionRepository;
    private final AcudienteRepository acudienteRepository;
    private final emailService emailService;

    /**
     * Crear una nueva preinscripción
     */
    public Preinscripcion crearPreinscripcion(Preinscripcion preinscripcion) {
        try {
            log.info("Creando nueva preinscripción");

            // Validar que tenga aspirante y acudiente
            if (preinscripcion.getAspirante() == null
                    || preinscripcion.getAspirante().getCodigoEstudiante() == null) {
                throw new RuntimeException("El aspirante es obligatorio");
            }

            if (preinscripcion.getAcudiente() == null
                    || preinscripcion.getAcudiente().getIdAcudiente() == null) {
                throw new RuntimeException("El acudiente es obligatorio");
            }

            // Establecer fecha de preinscripción si no viene
            if (preinscripcion.getFechaEntrevista() == null) {
                preinscripcion.setFechaEntrevista(LocalDateTime.now());
            }

            return preinscripcionRepository.save(preinscripcion);

        } catch (RuntimeException e) {
            log.error("Error al crear preinscripción: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al crear preinscripción", e);
            throw new RuntimeException("Error en la base de datos: " + e.getMessage());
        }
    }

    /**
     * Obtener todas las preinscripciones con estado Pendiente
     */
    @Transactional(readOnly = true)
    public List<Preinscripcion> obtenerTodasLasPreinscripciones() {
        log.info("Obteniendo preinscripciones con estado Pendiente");
        
        return preinscripcionRepository.findAll().stream()
                .filter(p -> p.getAspirante() != null 
                        && "Pendiente".equalsIgnoreCase(p.getAspirante().getEstado()))
                .collect(Collectors.toList());
    }

    /**
     * Obtener preinscripción por ID
     */
    @Transactional(readOnly = true)
    public Preinscripcion obtenerPreinscripcionPorId(Long idPreinscripcion) {
        log.info("Obteniendo preinscripción con ID: {}", idPreinscripcion);

        return preinscripcionRepository.findById(idPreinscripcion)
                .orElseThrow(() -> new RuntimeException(
                "Preinscripción no encontrada con ID: " + idPreinscripcion));
    }

    /**
     * Buscar preinscripciones por texto (nombre de aspirante o acudiente)
     */
    @Transactional(readOnly = true)
    public List<Preinscripcion> buscarPreinscripciones(String textoBusqueda) {
        log.info("Buscando preinscripciones con texto: {}", textoBusqueda);

        List<Preinscripcion> todasLasPreinscripciones = preinscripcionRepository.findAll();

        String textoLower = textoBusqueda.toLowerCase().trim();

        return todasLasPreinscripciones.stream()
                .filter(p -> {
                    // Buscar en nombre del aspirante
                    String nombreAspirante = "";
                    if (p.getAspirante() != null && p.getAspirante().getPersona() != null) {
                        nombreAspirante = (p.getAspirante().getPersona().getNombre() + " "
                                + p.getAspirante().getPersona().getApellido()).toLowerCase();
                    }

                    // Buscar en nombre del acudiente
                    String nombreAcudiente = "";
                    if (p.getAcudiente() != null && p.getAcudiente().getPersona() != null) {
                        nombreAcudiente = (p.getAcudiente().getPersona().getNombre() + " "
                                + p.getAcudiente().getPersona().getApellido()).toLowerCase();
                    }

                    return nombreAspirante.contains(textoLower)
                            || nombreAcudiente.contains(textoLower);
                })
                .collect(Collectors.toList());
    }

    /**
     * Programar entrevista para una preinscripción
     */
    public Preinscripcion programarEntrevista(
            Long idPreinscripcion,
            String fechaEntrevista,
            String lugarEntrevista) {

        log.info("Programando entrevista para preinscripción: {}", idPreinscripcion);

        try {
            Preinscripcion preinscripcion = obtenerPreinscripcionPorId(idPreinscripcion);

            // Convertir fecha String a LocalDateTime
            LocalDateTime fechaEntrevistaDateTime = LocalDateTime.parse(fechaEntrevista);

            // Actualizar preinscripción
            preinscripcion.setFechaEntrevista(fechaEntrevistaDateTime);
            preinscripcion.setLugarEntrevista(lugarEntrevista);

            Preinscripcion preinscripcionActualizada = preinscripcionRepository.save(preinscripcion);
            
            // Enviar correo al acudiente
            if (preinscripcion.getAcudiente() != null 
                    && preinscripcion.getAcudiente().getCorreoElectronico() != null) {
                
                String nombreAcudiente = preinscripcion.getAcudiente().getPersona().getNombre() 
                        + " " + preinscripcion.getAcudiente().getPersona().getApellido();
                String nombreEstudiante = preinscripcion.getAspirante().getPersona().getNombre() 
                        + " " + preinscripcion.getAspirante().getPersona().getApellido();
                
                emailService.enviarCorreoEntrevistaPreinscripcion(
                    preinscripcion.getAcudiente().getCorreoElectronico(),
                    nombreAcudiente,
                    nombreEstudiante,
                    fechaEntrevistaDateTime,
                    lugarEntrevista
                );
                
                log.info("Correo de entrevista enviado a: {}", 
                        preinscripcion.getAcudiente().getCorreoElectronico());
            } else {
                log.warn("No se pudo enviar correo: acudiente o correo no disponible");
            }

            return preinscripcionActualizada;

        } catch (Exception e) {
            log.error("Error al programar entrevista", e);
            throw new RuntimeException("Error al programar entrevista: " + e.getMessage());
        }
    }

    /**
     * Actualizar teléfono del acudiente
     */
    public void actualizarTelefonoAcudiente(Long idAcudiente, String telefono) {
        log.info("Actualizando teléfono del acudiente: {}", idAcudiente);

        try {
            Acudiente acudiente = acudienteRepository.findById(idAcudiente)
                    .orElseThrow(() -> new RuntimeException("Acudiente no encontrado"));

            acudiente.setTelefono(telefono);
            acudienteRepository.save(acudiente);

        } catch (Exception e) {
            log.error("Error al actualizar teléfono del acudiente", e);
            throw new RuntimeException("Error al actualizar teléfono: " + e.getMessage());
        }
    }

    /**
     * Actualizar estado del acudiente
     */
    public void actualizarEstadoAcudiente(Long idAcudiente, String nuevoEstado) {
        log.info("Actualizando estado del acudiente: {} a {}", idAcudiente, nuevoEstado);

        try {
            Acudiente acudiente = acudienteRepository.findById(idAcudiente)
                    .orElseThrow(() -> new RuntimeException("Acudiente no encontrado"));

            acudiente.setEstado(nuevoEstado);
            acudienteRepository.save(acudiente);

        } catch (Exception e) {
            log.error("Error al actualizar estado del acudiente", e);
            throw new RuntimeException("Error al actualizar estado: " + e.getMessage());
        }
    }

    /**
     * Obtener preinscripciones por estado del aspirante
     */
    @Transactional(readOnly = true)
    public List<Preinscripcion> obtenerPreinscripcionesPorEstado(String estado) {
        log.info("Obteniendo preinscripciones con estado: {}", estado);

        List<Preinscripcion> todasLasPreinscripciones = preinscripcionRepository.findAll();

        return todasLasPreinscripciones.stream()
                .filter(p -> p.getAspirante() != null
                && estado.equalsIgnoreCase(p.getAspirante().getEstado()))
                .collect(Collectors.toList());
    }
}
