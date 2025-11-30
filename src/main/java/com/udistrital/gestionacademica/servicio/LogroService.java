package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.Logro;
import com.udistrital.gestionacademica.repositorio.LogroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LogroService {

    private final LogroRepository logroRepository;

    /**
     * Obtiene todas las categorías únicas de logros
     */
    public List<String> obtenerCategorias() {
        log.info("Obteniendo categorías de logros");
        List<Logro> logros = logroRepository.findAll();
        return logros.stream()
            .map(Logro::getCategoriaLogro)
            .distinct()
            .sorted()
            .toList();
    }

    /**
     * Obtiene los logros por categoría
     */
    public List<Logro> obtenerLogrosPorCategoria(String categoria) {
        log.info("Obteniendo logros para categoría: {}", categoria);
        return logroRepository.findByCategoriaLogro(categoria);
    }

    /**
     * Crea un nuevo logro
     */
    public Logro crearLogro(Logro logro) {
        log.info("Creando logro: {}", logro.getNombreLogro());
        
        if (logro.getNombreLogro() == null || logro.getNombreLogro().isEmpty()) {
            throw new IllegalArgumentException("El nombre del logro es obligatorio");
        }
        
        if (logro.getCategoriaLogro() == null || logro.getCategoriaLogro().isEmpty()) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        
        if (logro.getEstado() == null) {
            logro.setEstado(true);
        }
        
        return logroRepository.save(logro);
    }

    /**
     * Obtiene un logro por ID
     */
    public Logro obtenerLogro(Long id) {
        log.info("Obteniendo logro con ID: {}", id);
        return logroRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Logro no encontrado con ID: " + id));
    }

    /**
     * Edita un logro existente
     */
    public Logro editarLogro(Long id, Logro logroActualizado) {
        log.info("Editando logro con ID: {}", id);
        
        Logro logro = obtenerLogro(id);
        
        if (logroActualizado.getNombreLogro() != null && !logroActualizado.getNombreLogro().isEmpty()) {
            logro.setNombreLogro(logroActualizado.getNombreLogro());
        }
        
        if (logroActualizado.getCategoriaLogro() != null && !logroActualizado.getCategoriaLogro().isEmpty()) {
            logro.setCategoriaLogro(logroActualizado.getCategoriaLogro());
        }
        
        if (logroActualizado.getDescripcion() != null) {
            logro.setDescripcion(logroActualizado.getDescripcion());
        }
        
        return logroRepository.save(logro);
    }

    /**
     * Cambia el estado (inhabilita) un logro
     */
    public Logro cambiarEstado(Long id) {
        log.info("Cambiando estado del logro con ID: {}", id);
        
        Logro logro = obtenerLogro(id);
        logro.setEstado(!logro.getEstado());
        
        return logroRepository.save(logro);
    }

    /**
     * Elimina un logro (opcional, ya que podemos usar cambiarEstado)
     */
    public void eliminarLogro(Long id) {
        log.info("Eliminando logro con ID: {}", id);
        
        if (!logroRepository.existsById(id)) {
            throw new IllegalArgumentException("Logro no encontrado con ID: " + id);
        }
        
        logroRepository.deleteById(id);
    }

    /**
     * Obtiene todos los logros habilitados
     */
    public List<Logro> obtenerLogrosHabilitados() {
        log.info("Obteniendo logros habilitados");
        return logroRepository.findByEstadoTrue();
    }

}
