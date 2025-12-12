package com.udistrital.gestionacademica.repositorio;

import com.udistrital.gestionacademica.modelo.Grado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GradoRepository extends JpaRepository<Grado, Long> {
    
    Optional<Grado> findByNombreGrado(String nombreGrado);
}