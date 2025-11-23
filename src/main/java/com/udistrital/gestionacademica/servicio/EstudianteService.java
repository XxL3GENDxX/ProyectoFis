package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.Estudiante;
import com.udistrital.gestionacademica.modelo.Grupo;
import com.udistrital.gestionacademica.repositorio.EstudianteRepository;
import com.udistrital.gestionacademica.repositorio.GrupoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final GrupoRepository grupoRepository;

    @Transactional(readOnly = true)
    public List<Estudiante> obtenerTodosLosEstudiantes() {
        log.info("Obteniendo todos los estudiantes");
        try {
            return estudianteRepository.findAllOrdenados();
        } catch (Exception e) {
            log.error("Error al obtener estudiantes: {}", e.getMessage());
            throw new RuntimeException("Error en la base de datos al obtener estudiantes", e);
        }
    }

    
    public Estudiante asignarEstudianteAGrupo(Long codigoEstudiante, Long idGrupo) {
        log.info("Asignando estudiante {} al grupo {}", codigoEstudiante, idGrupo);
        
        try {
            Estudiante estudiante = estudianteRepository.findById(codigoEstudiante)
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
            
            Grupo grupo = grupoRepository.findById(idGrupo)
                    .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
            
            if (grupo.estaCompleto()) {
                log.warn("El grupo {} está completo", idGrupo);
                throw new RuntimeException("GRUPO_COMPLETO");
            }
            
            estudiante.setGrupo(grupo);
            Estudiante estudianteActualizado = estudianteRepository.save(estudiante);
            
            log.info("Estudiante {} asignado exitosamente al grupo {}", codigoEstudiante, idGrupo);
            return estudianteActualizado;
            
        } catch (RuntimeException e) {
            if ("GRUPO_COMPLETO".equals(e.getMessage())) {
                throw e;
            }
            log.error("Error al asignar estudiante al grupo: {}", e.getMessage());
            throw new RuntimeException("Error en la base de datos", e);
        }
    }

    public Estudiante desvincularEstudianteDeGrupo(Long codigoEstudiante) {
        log.info("Desvinculando estudiante {} de su grupo", codigoEstudiante);
        
        try {
            Estudiante estudiante = estudianteRepository.findById(codigoEstudiante)
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
            
            if (estudiante.getGrupo() == null) {
                log.warn("El estudiante {} no tiene grupo asignado", codigoEstudiante);
                throw new RuntimeException("ESTUDIANTE_SIN_GRUPO");
            }
            
            estudiante.setGrupo(null);
            Estudiante estudianteActualizado = estudianteRepository.save(estudiante);
            
            log.info("Estudiante {} desvinculado exitosamente de su grupo", codigoEstudiante);
            return estudianteActualizado;
            
        } catch (RuntimeException e) {
            if ("ESTUDIANTE_SIN_GRUPO".equals(e.getMessage())) {
                throw e;
            }
            log.error("Error al desvincular estudiante del grupo: {}", e.getMessage());
            throw new RuntimeException("Error en la base de datos", e);
        }
    }

    public Estudiante modificarEstudiante(Long codigoEstudiante, Estudiante estudianteModificado) {
        log.info("Modificando estudiante con código: {}", codigoEstudiante);
        
        try {
            Estudiante estudianteExistente = estudianteRepository.findById(codigoEstudiante)
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
            
            validarDatosEstudiante(estudianteModificado);
            
            if (estudianteModificado.getDocumento() != null && 
                !estudianteModificado.getDocumento().isEmpty()) {
                
                Optional<Estudiante> estudianteConDocumento = 
                    estudianteRepository.findByDocumento(estudianteModificado.getDocumento());
                
                if (estudianteConDocumento.isPresent() && 
                    !estudianteConDocumento.get().getCodigoEstudiante().equals(codigoEstudiante)) {
                    log.warn("El documento {} ya existe en otro estudiante", 
                            estudianteModificado.getDocumento());
                    throw new RuntimeException("DOCUMENTO_DUPLICADO");
                }
            }
            
            actualizarDatosEstudiante(estudianteExistente, estudianteModificado);
            Estudiante estudianteActualizado = estudianteRepository.save(estudianteExistente);
            
            log.info("Estudiante {} modificado exitosamente", codigoEstudiante);
            return estudianteActualizado;
            
        } catch (RuntimeException e) {
            if ("DATOS_INVALIDOS".equals(e.getMessage()) || 
                "DOCUMENTO_DUPLICADO".equals(e.getMessage())) {
                throw e;
            }
            log.error("Error al modificar estudiante: {}", e.getMessage());
            throw new RuntimeException("Error en la base de datos", e);
        }
    }
    
    private void validarDatosEstudiante(Estudiante estudiante) {
        StringBuilder errores = new StringBuilder();
        
        if (estudiante.getNombre() == null || estudiante.getNombre().trim().isEmpty()) {
            errores.append("El nombre es obligatorio. ");
        } else if (estudiante.getNombre().trim().length() < 2) {
            errores.append("El nombre debe tener al menos 2 caracteres. ");
        }
        
        if (estudiante.getApellido() == null || estudiante.getApellido().trim().isEmpty()) {
            errores.append("El apellido es obligatorio. ");
        } else if (estudiante.getApellido().trim().length() < 2) {
            errores.append("El apellido debe tener al menos 2 caracteres. ");
        }
        
        if (estudiante.getDocumento() == null || estudiante.getDocumento().trim().isEmpty()) {
            errores.append("El documento es obligatorio. ");
        } else if (!estudiante.getDocumento().matches("\\d+")) {
            errores.append("El documento debe contener solo números. ");
        } else if (estudiante.getDocumento().trim().length() < 6 || 
                   estudiante.getDocumento().trim().length() > 20) {
            errores.append("El documento debe tener entre 6 y 20 dígitos. ");
        }
        
        if (errores.length() > 0) {
            log.warn("Datos inválidos: {}", errores.toString());
            throw new RuntimeException("DATOS_INVALIDOS: " + errores.toString().trim());
        }
    }
    
    private void actualizarDatosEstudiante(Estudiante existente, Estudiante modificado) {
        existente.setNombre(modificado.getNombre().trim());
        existente.setApellido(modificado.getApellido().trim());
        existente.setDocumento(modificado.getDocumento().trim());
        
        if (modificado.getFechaNacimiento() != null) {
            existente.setFechaNacimiento(modificado.getFechaNacimiento());
        }
    }
    
    @Transactional(readOnly = true)
    public Estudiante obtenerEstudiantePorCodigo(Long codigoEstudiante) {
        log.info("Obteniendo estudiante con código: {}", codigoEstudiante);
        return estudianteRepository.findById(codigoEstudiante)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
    }

    /**
     * Cambiar estado del estudiante (Activo <-> Inactivo)
     * Implementa el caso de uso "Gestionar Estado del Estudiante"
     * 
     * Paso 5: Consulta el estado del estudiante
     * Paso 6: Verifica el estado actual
     * Paso 7/11: Actualiza el estado según corresponda
     * 
     * @param codigoEstudiante Código del estudiante
     * @return Estudiante con estado actualizado
     * @throws RuntimeException si ocurre un error en la base de datos
     */
    public Estudiante cambiarEstadoEstudiante(Long codigoEstudiante) {
        log.info("Cambiando estado del estudiante con código: {}", codigoEstudiante);
        
        try {
            // Paso 5: Consulta el estado del estudiante
            Estudiante estudiante = estudianteRepository.findById(codigoEstudiante)
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
            
            String estadoActual = estudiante.getEstado();
            log.info("Estado actual del estudiante {}: {}", codigoEstudiante, estadoActual);
            
            // Paso 6: Verifica el estado actual del estudiante
            if ("Inactivo".equalsIgnoreCase(estadoActual)) {
                // Paso 7: Actualiza el estado del estudiante como activo
                estudiante.setEstado("Activo");
                log.info("Cambiando estado a Activo para estudiante {}", codigoEstudiante);
            } else {
                // Paso 11: Actualiza el estado del estudiante como inactivo
                estudiante.setEstado("Inactivo");
                log.info("Cambiando estado a Inactivo para estudiante {}", codigoEstudiante);
            }
            
            // Guardar cambios
            Estudiante estudianteActualizado = estudianteRepository.save(estudiante);
            
            log.info("Estado del estudiante {} actualizado exitosamente a: {}", 
                    codigoEstudiante, estudianteActualizado.getEstado());
            
            return estudianteActualizado;
            
        } catch (RuntimeException e) {
            if ("Estudiante no encontrado".equals(e.getMessage())) {
                throw e;
            }
            // Flujo alternativo: Error en la base de datos
            log.error("Error al cambiar estado del estudiante: {}", e.getMessage());
            throw new RuntimeException("Error en la base de datos", e);
        }
    }
}