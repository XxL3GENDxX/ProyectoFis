package com.udistrital.gestionacademica.repositorio;

import com.udistrital.gestionacademica.modelo.Boletin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoletinRepository extends JpaRepository<Boletin, Long> {
    
    // Buscar boletines por estudiante
    List<Boletin> findByEstudianteCodigoEstudiante(Long codigoEstudiante);
    
    // Buscar si ya existe un boletín para un estudiante en un periodo
    Optional<Boletin> findByEstudianteCodigoEstudianteAndPeriodoIdPeriodo(Long codigoEstudiante, Long idPeriodo);
}
