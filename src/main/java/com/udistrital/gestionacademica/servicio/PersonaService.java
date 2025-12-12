package com.udistrital.gestionacademica.servicio;


import com.udistrital.gestionacademica.modelo.Persona;
import com.udistrital.gestionacademica.repositorio.PersonaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PersonaService {

    private final PersonaRepository personaRepository;

    public Persona crearPersona(Persona persona) {
        log.info("Creando una nueva persona: {}", persona);
        return personaRepository.save(persona);
    }
    
}
