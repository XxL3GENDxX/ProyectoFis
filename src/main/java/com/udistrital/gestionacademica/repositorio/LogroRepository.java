package com.udistrital.gestionacademica.repositorio;

import com.udistrital.gestionacademica.modelo.Logro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LogroRepository extends JpaRepository<Logro, Long> {
    
    /**
     * Buscar logros por categoría
     */
    List<Logro> findByCategoria(String categoria);
    
    /**
     * Verificar si existe un logro con el mismo nombre en una categoría
     */
    boolean existsByNombreLogroAndCategoria(String nombreLogro, String categoria);
}