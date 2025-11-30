package com.udistrital.gestionacademica.repositorio;

import com.udistrital.gestionacademica.modelo.Logro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LogroRepository extends JpaRepository<Logro, Long> {
    List<Logro> findByCategoriaLogro(String categoriaLogro);
    List<Logro> findByEstadoTrue();
}
