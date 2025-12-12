package com.udistrital.gestionacademica.repositorio;

import com.udistrital.gestionacademica.modelo.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfesorRepository extends JpaRepository<Profesor, Long> {

    Optional<Profesor> findByPersona_Documento(String documento);
    
    Optional<Profesor> findByPersona(com.udistrital.gestionacademica.modelo.Persona persona);
}
