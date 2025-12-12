package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.Grado;
import com.udistrital.gestionacademica.repositorio.GradoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GradoService {

    private final GradoRepository gradoRepository;

    @Transactional(readOnly = true)
    public List<Grado> obtenerTodosLosGrados() {
        log.info("Obteniendo todos los grados");
        return gradoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Grado obtenerGradoPorId(Long idGrado) {
        log.info("Obteniendo grado con id: {}", idGrado);
        return gradoRepository.findById(idGrado)
                .orElseThrow(() -> new RuntimeException("Grado no encontrado"));
    }
}