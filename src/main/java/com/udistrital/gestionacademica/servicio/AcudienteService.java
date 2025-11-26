package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.Acudiente;
import com.udistrital.gestionacademica.repositorio.AcudienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AcudienteService {

    private AcudienteRepository acudienteRepository;

    public Acudiente crearAcudiente(Acudiente acudiente) {
        try {
            return acudienteRepository.save(acudiente);
        } catch (Exception e) {
            if ("ACUDIENTE_YA_EXISTE".equals(e.getMessage()) ||
                    "DOCUMENTO_DUPLICADO".equals(e.getMessage())) {
                throw e;
            }
            throw new RuntimeException("Error en la base de datos", e);
        }
    }

    public List<Acudiente> obtenerTodosLosAcudientes() {
        return acudienteRepository.findAll();
    }
    
    public Acudiente obtenerAcudientePorId(Long id) {
        return acudienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Acudiente no encontrado con id: " + id));
    }
}
