package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.Preinscripcion;
import com.udistrital.gestionacademica.repositorio.PreinscripcionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j

public class PreinscripcionService {
    
    private final PreinscripcionRepository preinscripcionRepository;

    public Preinscripcion crearPreinscripcion(Preinscripcion preinscripcion) {
        return preinscripcionRepository.save(preinscripcion);
    }

}
