
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

    @Transactional(readOnly = true)
    public Estudiante obtenerEstudiantePorId(Long codigoEstudiante) {
        log.info("Obteniendo estudiante con código: {}", codigoEstudiante);
        return estudianteRepository.findById(codigoEstudiante)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
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

    public Estudiante desvincularEstudianteDeGrupo(Long codigoEstudiante) {
        log.info("Desvinculando estudiante {} del grupo", codigoEstudiante);
        
        try {
            // Obtener el estudiante
            Estudiante estudiante = estudianteRepository.findById(codigoEstudiante)
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
            
            // Desvincular el grupo (establecer a null)
            estudiante.setGrupo(null);
            
            // Guardar el estudiante
            Estudiante estudianteActualizado = estudianteRepository.save(estudiante);
            
            log.info("Estudiante {} desvinculado exitosamente del grupo", codigoEstudiante);
            return estudianteActualizado;
            
        } catch (Exception e) {
            log.error("Error al desvincular estudiante del grupo: {}", e.getMessage());
            throw new RuntimeException("Error en la base de datos", e);
        }
    }
}