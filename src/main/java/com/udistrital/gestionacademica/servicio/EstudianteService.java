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

    /**
     * Modificar estudiante existente
     * Implementa el flujo principal del diagrama "Modificar Estudiante"
     * 
     * @param codigoEstudiante Código del estudiante a modificar
     * @param estudianteModificado Datos modificados del estudiante
     * @return Estudiante modificado
     * @throws RuntimeException con mensajes específicos según la validación
     */
    public Estudiante modificarEstudiante(Long codigoEstudiante, Estudiante estudianteModificado) {
        log.info("Modificando estudiante con código: {}", codigoEstudiante);
        
        try {
            // Obtener el estudiante existente
            Estudiante estudianteExistente = estudianteRepository.findById(codigoEstudiante)
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
            
            // Paso 5: Validar datos ingresados, campos obligatorios y formato
            validarDatosEstudiante(estudianteModificado);
            
            // Paso 6: Validar si el documento ya existe en otro estudiante
            if (estudianteModificado.getDocumento() != null && 
                !estudianteModificado.getDocumento().isEmpty()) {
                
                Optional<Estudiante> estudianteConDocumento = 
                    estudianteRepository.findByDocumento(estudianteModificado.getDocumento());
                
                // Verificar que el documento no pertenezca a otro estudiante
                if (estudianteConDocumento.isPresent() && 
                    !estudianteConDocumento.get().getCodigoEstudiante().equals(codigoEstudiante)) {
                    log.warn("El documento {} ya existe en otro estudiante", 
                            estudianteModificado.getDocumento());
                    throw new RuntimeException("DOCUMENTO_DUPLICADO");
                }
            }
            
            // Paso 7: Guardar los cambios realizados
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
    
    /**
     * Valida los datos del estudiante
     * Implementa el paso 5 del diagrama: validación de campos obligatorios y formato
     */
    private void validarDatosEstudiante(Estudiante estudiante) {
        StringBuilder errores = new StringBuilder();
        
        // Validar nombre (obligatorio)
        if (estudiante.getNombre() == null || estudiante.getNombre().trim().isEmpty()) {
            errores.append("El nombre es obligatorio. ");
        } else if (estudiante.getNombre().trim().length() < 2) {
            errores.append("El nombre debe tener al menos 2 caracteres. ");
        }
        
        // Validar apellido (obligatorio)
        if (estudiante.getApellido() == null || estudiante.getApellido().trim().isEmpty()) {
            errores.append("El apellido es obligatorio. ");
        } else if (estudiante.getApellido().trim().length() < 2) {
            errores.append("El apellido debe tener al menos 2 caracteres. ");
        }
        
        // Validar documento (obligatorio y formato)
        if (estudiante.getDocumento() == null || estudiante.getDocumento().trim().isEmpty()) {
            errores.append("El documento es obligatorio. ");
        } else if (!estudiante.getDocumento().matches("\\d+")) {
            errores.append("El documento debe contener solo números. ");
        } else if (estudiante.getDocumento().trim().length() < 6 || 
                   estudiante.getDocumento().trim().length() > 20) {
            errores.append("El documento debe tener entre 6 y 20 dígitos. ");
        }
        
        // Si hay errores, lanzar excepción
        if (errores.length() > 0) {
            log.warn("Datos inválidos: {}", errores.toString());
            throw new RuntimeException("DATOS_INVALIDOS: " + errores.toString().trim());
        }
    }
    
    /**
     * Actualiza los datos del estudiante existente con los nuevos valores
     */
    private void actualizarDatosEstudiante(Estudiante existente, Estudiante modificado) {
        existente.setNombre(modificado.getNombre().trim());
        existente.setApellido(modificado.getApellido().trim());
        existente.setDocumento(modificado.getDocumento().trim());
        
        // Actualizar fecha de nacimiento si se proporciona
        if (modificado.getFechaNacimiento() != null) {
            existente.setFechaNacimiento(modificado.getFechaNacimiento());
        }
    }
    
    /**
     * Obtener un estudiante por código
     * Necesario para cargar los datos en el formulario de modificación
     */
    @Transactional(readOnly = true)
    public Estudiante obtenerEstudiantePorCodigo(Long codigoEstudiante) {
        log.info("Obteniendo estudiante con código: {}", codigoEstudiante);
        return estudianteRepository.findById(codigoEstudiante)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
    }
}