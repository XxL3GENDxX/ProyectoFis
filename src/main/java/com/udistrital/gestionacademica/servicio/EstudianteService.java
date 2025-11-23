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
            // Obtener el estudiante
            Estudiante estudiante = estudianteRepository.findById(codigoEstudiante)
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
            
            // Obtener el grupo
            Grupo grupo = grupoRepository.findById(idGrupo)
                    .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
            
            // Verificar si el grupo está completo
            if (grupo.estaCompleto()) {
                log.warn("El grupo {} está completo", idGrupo);
                throw new RuntimeException("GRUPO_COMPLETO");
            }
            
            // Asignar el grupo al estudiante
            estudiante.setGrupo(grupo);
            
            // Guardar el estudiante
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

    /**
     * Desvincular estudiante de su grupo actual
     * Paso 5 del flujo principal del diagrama de actividades
     * 
     * @param codigoEstudiante Código del estudiante a desvincular
     * @return Estudiante desvinculado
     * @throws RuntimeException si el estudiante no existe o hay error en BD
     */
    public Estudiante desvincularEstudianteDeGrupo(Long codigoEstudiante) {
        log.info("Desvinculando estudiante {} de su grupo", codigoEstudiante);
        
        try {
            // Obtener el estudiante
            Estudiante estudiante = estudianteRepository.findById(codigoEstudiante)
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
            
            // Verificar si el estudiante tiene grupo asignado
            if (estudiante.getGrupo() == null) {
                log.warn("El estudiante {} no tiene grupo asignado", codigoEstudiante);
                throw new RuntimeException("ESTUDIANTE_SIN_GRUPO");
            }
            
            // Desvincular al estudiante del grupo (establecer grupo a null)
            estudiante.setGrupo(null);
            
            // Guardar el estudiante actualizado
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
}