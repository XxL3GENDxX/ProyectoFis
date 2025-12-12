package com.udistrital.gestionacademica.repositorio;

import com.udistrital.gestionacademica.modelo.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    
    /**
     * Buscar calificaciones por código de estudiante
     */
    @Query("SELECT c FROM Calificacion c WHERE c.estudiante.codigoEstudiante = :codigoEstudiante ORDER BY c.fechaAsignacion DESC")
    List<Calificacion> findByEstudianteCodigoEstudiante(@Param("codigoEstudiante") Long codigoEstudiante);

    /**
     * Buscar calificaciones por código de estudiante y período
     */
    @Query("SELECT c FROM Calificacion c WHERE c.estudiante.codigoEstudiante = :codigoEstudiante AND c.periodo.idPeriodo = :idPeriodo ORDER BY c.fechaAsignacion DESC")
    List<Calificacion> findByEstudianteCodigoEstudianteAndPeriodo(
        @Param("codigoEstudiante") Long codigoEstudiante,
        @Param("idPeriodo") Long idPeriodo
    );

    /**
     * Buscar calificaciones por nombre de período
     */
    @Query("SELECT c FROM Calificacion c WHERE c.periodo.nombrePeriodo = :nombrePeriodo ORDER BY c.fechaAsignacion DESC")
    List<Calificacion> findByPeriodoNombrePeriodo(@Param("nombrePeriodo") String nombrePeriodo);
    
    /**
     * Verificar si ya existe una calificación para un estudiante con un logro específico en un período
     */
    @Query("SELECT c FROM Calificacion c WHERE c.estudiante.codigoEstudiante = :codigoEstudiante AND c.logro.idLogro = :idLogro AND c.periodo.idPeriodo = :idPeriodo")
    Optional<Calificacion> findByEstudianteAndLogroPeriodo(
        @Param("codigoEstudiante") Long codigoEstudiante,
        @Param("idLogro") Long idLogro,
        @Param("idPeriodo") Long idPeriodo
    );
    
    /**
     * Verificar si ya existe una calificación para un estudiante con un logro específico (sin periodo)
     */
    @Query("SELECT c FROM Calificacion c WHERE c.estudiante.codigoEstudiante = :codigoEstudiante AND c.logro.idLogro = :idLogro")
    Optional<Calificacion> findByEstudianteAndLogro(
        @Param("codigoEstudiante") Long codigoEstudiante,
        @Param("idLogro") Long idLogro
    );
    
    /**
     * Contar calificaciones por estudiante
     */
    @Query("SELECT COUNT(c) FROM Calificacion c WHERE c.estudiante.codigoEstudiante = :codigoEstudiante")
    long countByEstudianteCodigoEstudiante(@Param("codigoEstudiante") Long codigoEstudiante);

    /**
     * Contar calificaciones por estudiante y período
     */
    @Query("SELECT COUNT(c) FROM Calificacion c WHERE c.estudiante.codigoEstudiante = :codigoEstudiante AND c.periodo.idPeriodo = :idPeriodo")
    long countByEstudianteAndPeriodo(@Param("codigoEstudiante") Long codigoEstudiante, @Param("idPeriodo") Long idPeriodo);

    /**
     * Buscar todas las calificaciones asociadas a un logro
     */
    @Query("SELECT c FROM Calificacion c WHERE c.logro.idLogro = :idLogro")
    List<Calificacion> findByLogroId(@Param("idLogro") Long idLogro);
}