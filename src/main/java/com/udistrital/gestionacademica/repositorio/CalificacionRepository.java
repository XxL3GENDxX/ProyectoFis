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
     * Verificar si ya existe una calificación para un estudiante con un logro específico
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
}