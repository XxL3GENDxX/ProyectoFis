package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.Grupo;
import com.udistrital.gestionacademica.modelo.Profesor;
import com.udistrital.gestionacademica.repositorio.GrupoRepository;
import com.udistrital.gestionacademica.repositorio.ProfesorRepository;
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
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final ProfesorRepository profesorRepository;

    @Transactional(readOnly = true)
    public List<Grupo> obtenerGruposPorGrado(Long idGrado) {
        log.info("Obteniendo grupos del grado: {}", idGrado);
        return grupoRepository.findByGradoId(idGrado);
    }

    @Transactional(readOnly = true)
    public Grupo obtenerGrupoPorId(Long idGrupo) {
        log.info("Obteniendo grupo con id: {}", idGrupo);
        return grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
    }

    public Grupo crearGrupo(Grupo grupo, String documentoDirector) {
        log.info("Creando grupo {} para grado {}", grupo.getNumeroGrupo(), grupo.getGrado().getIdGrado());
        
        Optional<Grupo> grupoExiste = grupoRepository.findByGradoIdAndNumeroGrupo(
                grupo.getGrado().getIdGrado(),
                grupo.getNumeroGrupo());

        if (grupoExiste.isPresent()) {
            throw new RuntimeException("Ya existe un grupo con el mismo número en este grado");
        }
        
        // Buscar y asociar director si se proporciona documento
        if (documentoDirector != null && !documentoDirector.trim().isEmpty()) {
            Profesor director = profesorRepository.findByPersona_Documento(documentoDirector)
                    .orElseThrow(() -> new RuntimeException(
                            "No se encontró un profesor con documento: " + documentoDirector));
            grupo.setDirectorGrupo(director);
            log.info("Director asignado: {} {}", 
                    director.getPersona().getNombre(), 
                    director.getPersona().getApellido());
        } else {
            grupo.setDirectorGrupo(null);
            log.info("Grupo creado sin director");
        }

        return grupoRepository.save(grupo);
    }

    public Grupo actualizarGrupo(Grupo grupo, String documentoDirector) {
        log.info("Actualizando grupo con id: {}", grupo.getIdGrupo());
        
        Grupo grupoExistente = obtenerGrupoPorId(grupo.getIdGrupo());

        grupoExistente.setGrado(grupo.getGrado());
        grupoExistente.setNumeroGrupo(grupo.getNumeroGrupo());
        
        // Buscar y asociar director si se proporciona documento
        if (documentoDirector != null && !documentoDirector.trim().isEmpty()) {
            Profesor director = profesorRepository.findByPersona_Documento(documentoDirector)
                    .orElseThrow(() -> new RuntimeException(
                            "No se encontró un profesor con documento: " + documentoDirector));
            grupoExistente.setDirectorGrupo(director);
            log.info("Director actualizado: {} {}", 
                    director.getPersona().getNombre(), 
                    director.getPersona().getApellido());
        } else {
            grupoExistente.setDirectorGrupo(null);
            log.info("Director removido del grupo");
        }

        return grupoRepository.save(grupoExistente);
    }

    public void eliminarGrupo(Long idGrupo) {
        Grupo grupoExistente = obtenerGrupoPorId(idGrupo);
        grupoRepository.delete(grupoExistente);
    }
}
