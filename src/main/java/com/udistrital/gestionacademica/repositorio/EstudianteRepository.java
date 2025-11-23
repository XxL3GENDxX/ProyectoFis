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

    /**
     * Obtiene todos los estudiantes ordenados alfabéticamente
     */
    @Query("SELECT e FROM Estudiante e ORDER BY e.apellido, e.nombre")
    List<Estudiante> findAllOrdenados();
    
    /**
     * Busca un estudiante por número de documento
     * Necesario para validar documentos duplicados en modificación
     */
    @Query("SELECT e FROM Estudiante e WHERE e.documento = :documento")
    Optional<Estudiante> findByDocumento(@Param("documento") String documento);
}