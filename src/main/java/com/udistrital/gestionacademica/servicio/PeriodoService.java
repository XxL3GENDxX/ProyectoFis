package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.Periodo;
import com.udistrital.gestionacademica.repositorio.PeriodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PeriodoService {

    private final PeriodoRepository periodoRepository;

    /**
     * Crear un nuevo período
     */
    public Periodo crearPeriodo(Periodo periodo) {
        log.info("Creando nuevo periodo: {}", periodo.getNombrePeriodo());
        
        // Validar que no exista un periodo con el mismo nombre
        if (periodoRepository.findByNombrePeriodo(periodo.getNombrePeriodo()).isPresent()) {
            throw new RuntimeException("Ya existe un periodo con el nombre: " + periodo.getNombrePeriodo());
        }
        
        // Validar que la fecha inicio sea menor a la fecha fin
        if (periodo.getFechaInicio().isAfter(periodo.getFechaFin())) {
            throw new RuntimeException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
        
        return periodoRepository.save(periodo);
    }

    /**
     * Actualizar un período existente
     */
    public Periodo actualizarPeriodo(Long idPeriodo, Periodo periodo) {
        log.info("Actualizando periodo: {}", idPeriodo);
        
        Periodo periodoExistente = periodoRepository.findById(idPeriodo)
                .orElseThrow(() -> new RuntimeException("Periodo no encontrado con id: " + idPeriodo));
        
        if (periodo.getNombrePeriodo() != null && !periodo.getNombrePeriodo().equals(periodoExistente.getNombrePeriodo())) {
            if (periodoRepository.findByNombrePeriodo(periodo.getNombrePeriodo()).isPresent()) {
                throw new RuntimeException("Ya existe un periodo con el nombre: " + periodo.getNombrePeriodo());
            }
            periodoExistente.setNombrePeriodo(periodo.getNombrePeriodo());
        }
        
        if (periodo.getFechaInicio() != null) {
            periodoExistente.setFechaInicio(periodo.getFechaInicio());
        }
        
        if (periodo.getFechaFin() != null) {
            periodoExistente.setFechaFin(periodo.getFechaFin());
        }
        
        return periodoRepository.save(periodoExistente);
    }

    /**
     * Obtener un período por id
     */
    public Optional<Periodo> obtenerPeriodo(Long idPeriodo) {
        log.info("Obteniendo periodo: {}", idPeriodo);
        return periodoRepository.findById(idPeriodo);
    }

    /**
     * Obtener todos los períodos
     */
    public List<Periodo> obtenerTodosPeriodos() {
        log.info("Obteniendo todos los periodos");
        return periodoRepository.findAll();
    }

    /**
     * Obtener todos los períodos activos
     */
    public List<Periodo> obtenerPeriodosActivos() {
        log.info("Obteniendo periodos activos");
        return periodoRepository.findAllActivos();
    }

    /**
     * Obtener el período actual
     */
    public Optional<Periodo> obtenerPeriodoActual() {
        log.info("Obteniendo periodo actual");
        return periodoRepository.findPeriodoActual();
    }

    /**
     * Obtener período por nombre
     */
    public Optional<Periodo> obtenerPeriodoPorNombre(String nombrePeriodo) {
        log.info("Obteniendo periodo por nombre: {}", nombrePeriodo);
        return periodoRepository.findByNombrePeriodo(nombrePeriodo);
    }

    /**
     * Eliminar un período
     */
    public void eliminarPeriodo(Long idPeriodo) {
        log.info("Eliminando periodo: {}", idPeriodo);
        periodoRepository.deleteById(idPeriodo);
    }
}
