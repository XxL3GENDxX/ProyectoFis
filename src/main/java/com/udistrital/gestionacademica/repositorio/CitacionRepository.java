package com.udistrital.gestionacademica.repositorio;

import com.udistrital.gestionacademica.modelo.Citacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CitacionRepository extends JpaRepository<Citacion, Long> {

    /**
     * Buscar citaciones por acudiente
     */
    @Query("SELECT c FROM Citacion c WHERE c.acudiente.idAcudiente = :idAcudiente ORDER BY c.fechaCitacion DESC")
    List<Citacion> findByAcudienteId(@Param("idAcudiente") Long idAcudiente);

    /**
     * Buscar citaciones por estudiante
     */
    @Query("SELECT c FROM Citacion c WHERE c.estudiante.codigoEstudiante = :codigoEstudiante ORDER BY c.fechaCitacion DESC")
    List<Citacion> findByEstudianteId(@Param("codigoEstudiante") Long codigoEstudiante);

    /**
     * Buscar citaciones por grupo
     */
    @Query("SELECT c FROM Citacion c WHERE c.estudiante.grupo.idGrupo = :idGrupo ORDER BY c.fechaCitacion DESC")
    List<Citacion> findByGrupoId(@Param("idGrupo") Long idGrupo);
}
