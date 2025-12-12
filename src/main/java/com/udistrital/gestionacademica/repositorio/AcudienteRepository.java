package com.udistrital.gestionacademica.repositorio;

import com.udistrital.gestionacademica.modelo.Acudiente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AcudienteRepository extends JpaRepository<Acudiente, Long> {

    /**
     * Busca un acudiente por número de documento Necesario para validar
     * documentos duplicados en modificación
     */
    @Query("SELECT a FROM Acudiente a WHERE a.persona.documento = :documento")
    Optional<Acudiente> findByDocumento(@Param("documento") String documento);

    @Query("SELECT a FROM Acudiente a WHERE a.persona.idPersona = :idPersona")
    Optional<Acudiente> findByPersona(@Param("idPersona") Long idPersona);
}
