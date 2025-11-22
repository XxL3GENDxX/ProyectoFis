package com.udistrital.gestionacademica.repositorio;

import com.udistrital.gestionacademica.modelo.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {


    /**
     * Obtiene todos los estudiantes ordenados alfabéticamente
     */
    @Query("SELECT e FROM Estudiante e ORDER BY e.apellido, e.nombre")
    List<Estudiante> findAllOrdenados();

  

}
