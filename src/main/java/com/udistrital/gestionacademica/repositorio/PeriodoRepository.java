package com.udistrital.gestionacademica.repositorio;

import com.udistrital.gestionacademica.modelo.Periodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeriodoRepository extends JpaRepository<Periodo, Long> {

    /**
     * Buscar periodo por nombre
     */
    Optional<Periodo> findByNombrePeriodo(String nombrePeriodo);

    /**
     * Obtener todos los periodos activos (por fecha actual)
     */
    @Query("SELECT p FROM Periodo p WHERE CURRENT_DATE BETWEEN p.fechaInicio AND p.fechaFin ORDER BY p.nombrePeriodo DESC")
    List<Periodo> findAllActivos();

    /**
     * Obtener el periodo actual basado en la fecha
     */
    @Query("SELECT p FROM Periodo p WHERE CURRENT_DATE BETWEEN p.fechaInicio AND p.fechaFin")
    Optional<Periodo> findPeriodoActual();

    /**
     * Buscar periodos que ya hayan finalizado
     */
    @Query("SELECT p FROM Periodo p WHERE p.fechaFin < CURRENT_DATE ORDER BY p.nombrePeriodo DESC")
    List<Periodo> findPeriodosFinalizados();
}
