package com.udistrital.gestionacademica.repositorio;

import com.udistrital.gestionacademica.modelo.Preinscripcion;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PreinscripcionRepository extends JpaRepository<Preinscripcion, Long> {

    // CORRECCIÓN
    @Query("INSERT INTO Preinscripcion (aspirante, fechaEntrevista, acudiente) VALUES (:aspirante, :fechaEntrevista, :acudiente)")
    void crearPreinscripcion(@Param("aspirante") Long idAspirante,
            @Param("fechaEntrevista") LocalDateTime fecha,
            @Param("acudiente") Long idAcudiente);

}
