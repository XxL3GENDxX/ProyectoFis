package com.udistrital.gestionacademica.repositorio;

import com.udistrital.gestionacademica.modelo.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    
    @Query("SELECT g FROM Grupo g WHERE g.grado.idGrado = :idGrado ORDER BY g.numeroGrupo")
    List<Grupo> findByGradoId(@Param("idGrado") Long idGrado);
    
    @Query("SELECT g FROM Grupo g WHERE g.grado.idGrado = :idGrado AND g.numeroGrupo = :numeroGrupo")
    Optional<Grupo> findByGradoIdAndNumeroGrupo(@Param("idGrado") Long idGrado, @Param("numeroGrupo") Integer numeroGrupo);
}