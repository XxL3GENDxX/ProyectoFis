package com.udistrital.gestionacademica.repositorio;

import com.udistrital.gestionacademica.modelo.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long>{

    @Query("INSERT INTO Persona (nombre, apellido, documento, fechaDeNacimiento, genero) VALUES (:nombre, :apellido, :documento, :fechaDeNacimiento, :genero)")
    Persona crearPersona(@Param("nombre") String nombre,
                         @Param("apellido") String apellido,
                         @Param("documento") String documento,
                         @Param("fechaDeNacimiento") String fechaDeNacimiento,
                         @Param("genero") String genero);
}
