package com.udistrital.gestionacademica.repositorio;

import com.udistrital.gestionacademica.modelo.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long>{
    


    
}
