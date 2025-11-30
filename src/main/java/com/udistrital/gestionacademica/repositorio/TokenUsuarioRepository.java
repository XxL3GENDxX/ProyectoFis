package com.udistrital.gestionacademica.repositorio;

import com.udistrital.gestionacademica.modelo.TokenUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenUsuarioRepository extends JpaRepository<TokenUsuario, Long> {


    
    @Query("SELECT t FROM TokenUsuario t WHERE t.nombreUsuario = :nombreUsuario AND t.contrasena = :contrasena")
    java.util.Optional<TokenUsuario> findByNombreUsuarioAndContrasena(@Param("nombreUsuario") String nombreUsuario, @Param("contrasena") String contrasena);
    
    @Query("SELECT t FROM TokenUsuario t WHERE t.nombreUsuario = :nombreUsuario")
    java.util.Optional<TokenUsuario> findByNombreUsuario(@Param("nombreUsuario") String nombreUsuario);
    
}
