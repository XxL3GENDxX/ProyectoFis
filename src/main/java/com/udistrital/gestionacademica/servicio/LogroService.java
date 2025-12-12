package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.Calificacion;
import com.udistrital.gestionacademica.modelo.Logro;
import com.udistrital.gestionacademica.repositorio.CalificacionRepository;
import com.udistrital.gestionacademica.repositorio.LogroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LogroService {

    private final LogroRepository logroRepository;
    private final CalificacionRepository calificacionRepository;

    /**
     * Obtener todos los logros de una categoría
     */
    @Transactional(readOnly = true)
    public List<Logro> obtenerLogrosPorCategoria(String categoria) {
        log.info("Obteniendo logros de la categoría: {}", categoria);
        return logroRepository.findByCategoria(categoria);
    }

    /**
     * Obtener un logro por ID
     */
    @Transactional(readOnly = true)
    public Logro obtenerLogroPorId(Long idLogro) {
        log.info("Obteniendo logro con ID: {}", idLogro);
        return logroRepository.findById(idLogro)
                .orElseThrow(() -> new RuntimeException("Logro no encontrado con ID: " + idLogro));
    }

    /**
     * Crear un nuevo logro
     */
    public Logro crearLogro(Logro logro) {
        log.info("Creando logro: {} en categoría: {}", logro.getNombreLogro(), logro.getCategoria());
        
        // Validar datos
        validarLogro(logro);
        
        // Verificar que no exista un logro con el mismo nombre en la misma categoría
        if (logroRepository.existsByNombreLogroAndCategoria(logro.getNombreLogro(), logro.getCategoria())) {
            throw new RuntimeException("Ya existe un logro con este nombre en esta categoría");
        }
        
        Logro nuevoLogro = logroRepository.save(logro);
        log.info("Logro creado exitosamente con ID: {}", nuevoLogro.getIdLogro());
        return nuevoLogro;
    }

    /**
     * Actualizar un logro existente
     */
    public Logro actualizarLogro(Long idLogro, Logro logroActualizado) {
        log.info("Actualizando logro con ID: {}", idLogro);
        
        Logro logroExistente = obtenerLogroPorId(idLogro);
        
        // Validar datos
        validarLogro(logroActualizado);
        
        // Si cambió el nombre, verificar que no exista otro con ese nombre en la misma categoría
        if (!logroExistente.getNombreLogro().equals(logroActualizado.getNombreLogro())) {
            if (logroRepository.existsByNombreLogroAndCategoria(
                    logroActualizado.getNombreLogro(), 
                    logroActualizado.getCategoria())) {
                throw new RuntimeException("Ya existe un logro con este nombre en esta categoría");
            }
        }
        
        // Actualizar campos
        logroExistente.setNombreLogro(logroActualizado.getNombreLogro());
        logroExistente.setDescripcion(logroActualizado.getDescripcion());
        logroExistente.setCategoria(logroActualizado.getCategoria());
        
        Logro logroGuardado = logroRepository.save(logroExistente);
        log.info("Logro actualizado exitosamente");
        return logroGuardado;
    }

    /**
     * Eliminar un logro
     * Primero elimina todas las calificaciones asociadas y luego elimina el logro
     */
    public void eliminarLogro(Long idLogro) {
        log.info("Eliminando logro con ID: {}", idLogro);
        
        // Verificar que el logro existe
        Logro logro = obtenerLogroPorId(idLogro);
        
        // Buscar y eliminar todas las calificaciones asociadas
        List<Calificacion> calificacionesAsociadas = calificacionRepository.findByLogroId(idLogro);
        if (!calificacionesAsociadas.isEmpty()) {
            log.info("Eliminando {} calificaciones asociadas al logro", calificacionesAsociadas.size());
            calificacionRepository.deleteAll(calificacionesAsociadas);
        }
        
        // Eliminar el logro
        logroRepository.delete(logro);
        
        log.info("Logro eliminado exitosamente");
    }

    /**
     * Validar datos del logro
     */
    private void validarLogro(Logro logro) {
        if (logro.getNombreLogro() == null || logro.getNombreLogro().trim().isEmpty()) {
            throw new RuntimeException("El nombre del logro es obligatorio");
        }
        
        if (logro.getNombreLogro().length() > 200) {
            throw new RuntimeException("El nombre del logro no puede exceder 200 caracteres");
        }
        
        if (logro.getDescripcion() == null || logro.getDescripcion().trim().isEmpty()) {
            throw new RuntimeException("La descripción es obligatoria");
        }
        
        if (logro.getDescripcion().length() > 500) {
            throw new RuntimeException("La descripción no puede exceder 500 caracteres");
        }
        
        if (logro.getCategoria() == null || logro.getCategoria().trim().isEmpty()) {
            throw new RuntimeException("La categoría es obligatoria");
        }
        
        // Validar que la categoría sea una de las permitidas
        List<String> categoriasValidas = List.of("psicosociales", "academicos", "deportivos", "artisticos", "culturales");
        if (!categoriasValidas.contains(logro.getCategoria().toLowerCase())) {
            throw new RuntimeException("Categoría no válida");
        }
    }
}