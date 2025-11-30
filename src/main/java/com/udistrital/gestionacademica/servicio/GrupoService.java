package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.Grupo;
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
public class GrupoService {

    private final GrupoRepository grupoRepository;

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

    public Grupo crearGrupo(Grupo grupo) {
        Optional<Grupo> grupoExiste = grupoRepository.findByGradoIdAndNumeroGrupo(grupo.getGrado().getIdGrado(),
                grupo.getNumeroGrupo());

        if (grupoExiste.isEmpty()) {
            return grupoRepository.save(grupo);
        } else {
            throw new RuntimeException("Ya existe un grupo con el mismo nombre en este grado");

        }

    }

    public Grupo actualizarGrupo(Grupo grupo) {
        Grupo grupoExistente = obtenerGrupoPorId(grupo.getIdGrupo());

        grupoExistente.setGrado(grupo.getGrado());
        grupoExistente.setNumeroGrupo(grupo.getNumeroGrupo());
        grupoExistente.setLimiteEstudiantes(grupo.getLimiteEstudiantes());
        grupoExistente.setDirectorGrupo(grupo.getDirectorGrupo());

        return grupoRepository.save(grupoExistente);
    }

    public void eliminarGrupo(Long idGrupo) {
        Grupo grupoExistente = obtenerGrupoPorId(idGrupo);
        grupoRepository.delete(grupoExistente);
    }
}
