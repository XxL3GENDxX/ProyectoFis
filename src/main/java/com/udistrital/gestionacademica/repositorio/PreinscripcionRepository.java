package com.udistrital.gestionacademica.repositorio;

import com.udistrital.gestionacademica.modelo.Preinscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PreinscripcionRepository extends JpaRepository<Preinscripcion, Long> {
    
    @Query("INSERT INTO Preinscripcion (aspirante, fechaEntrevista, horaEntrevista, acudiente) VALUES (:aspirante, :fechaEntrevista, :horaEntrevista, :acudiente)")
    Preinscripcion crearPreinscripcion(@Param("aspirante") Long aspirante,
                                      @Param("fechaEntrevista") String fechaEntrevista,
                                      @Param("horaEntrevista") String horaEntrevista,
                                      @Param("acudiente") Long acudiente);
                                      
                                      
}
