package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.TokenUsuario;
import com.udistrital.gestionacademica.repositorio.TokenUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TokenUsuarioService {

    private final TokenUsuarioRepository tokenUsuarioRepository;

    
    public TokenUsuario validarTokenUsuario(String nombreUsuario, String contrasena) {
      
        return tokenUsuarioRepository.findByNombreUsuarioAndContrasena(nombreUsuario, contrasena)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o no encontrado"));
    }










    
}
