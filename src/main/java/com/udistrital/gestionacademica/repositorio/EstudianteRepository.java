package com.udistrital.gestionacademica.repositorio;

import com.udistrital.gestionacademica.modelo.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    @Query("SELECT e FROM Estudiante e ORDER BY e.persona.apellido, e.persona.nombre")
    List<Estudiante> findAllOrdenados();

    @Query("SELECT e FROM Estudiante e WHERE e.persona.documento = :documento")
    Optional<Estudiante> findByDocumentoEstudiante(@Param("documento") String documento);

    @Query("SELECT e FROM Estudiante e WHERE e.grupo.idGrupo = :idGrupo")
    List<Estudiante> buscarPorIdGrupo(@Param("idGrupo") Long idGrupo);

    @Query("SELECT e FROM Estudiante e WHERE e.acudiente.idAcudiente = :idAcudiente ORDER BY e.persona.apellido, e.persona.nombre")
    List<Estudiante> findByAcudiente(@Param("idAcudiente") Long idAcudiente);
}
