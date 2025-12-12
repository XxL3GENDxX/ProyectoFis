package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.Boletin;
import com.udistrital.gestionacademica.modelo.Calificacion;
import com.udistrital.gestionacademica.modelo.Estudiante;
import com.udistrital.gestionacademica.modelo.Periodo;
import com.udistrital.gestionacademica.repositorio.BoletinRepository;
import com.udistrital.gestionacademica.repositorio.EstudianteRepository;
import com.udistrital.gestionacademica.repositorio.PeriodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BoletinService {

    private final BoletinRepository boletinRepository;
    private final EstudianteRepository estudianteRepository;
    private final PeriodoRepository periodoRepository;
    private final CalificacionService calificacionService;
    private final PdfGeneratorService pdfGeneratorService;

    /**
     * Genera un boletín valida que existan calificaciones, lo guarda en BD y retorna el PDF.
     * Si ya existe un boletín para ese periodo, lo actualiza (regenera).
     */
    public byte[] generarYGuardarBoletin(Long codigoEstudiante, Long idPeriodo) {
        log.info("Iniciando proceso de generación de boletín para estudiante {} en periodo {}", codigoEstudiante, idPeriodo);

        // 1. Validaciones
        Estudiante estudiante = estudianteRepository.findById(codigoEstudiante)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        Periodo periodo = periodoRepository.findById(idPeriodo)
                .orElseThrow(() -> new RuntimeException("Periodo no encontrado"));

        // 2. Obtener Calificaciones
        List<Calificacion> calificaciones = calificacionService.obtenerCalificacionesPorEstudianteYPeriodo(codigoEstudiante, idPeriodo);

        if (calificaciones.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron calificaciones registradas para el estudiante en el periodo seleccionado.");
        }

        // 3. Generar PDF
        byte[] pdfBytes = pdfGeneratorService.generarBoletin(estudiante, periodo, calificaciones);

        // 4. Persistencia (Guardar o Actualizar)
        Optional<Boletin> boletinExistente = boletinRepository.findByEstudianteCodigoEstudianteAndPeriodoIdPeriodo(codigoEstudiante, idPeriodo);

        Boletin boletin;
        if (boletinExistente.isPresent()) {
            log.info("Actualizando boletín existente");
            boletin = boletinExistente.get();
        } else {
            log.info("Creando nuevo boletín");
            boletin = new Boletin();
            boletin.setEstudiante(estudiante);
            boletin.setPeriodo(periodo);
        }

        boletin.setFechaGeneracion(java.time.LocalDateTime.now());

        boletinRepository.save(boletin);
        log.info("Boletín guardado exitosamente en base de datos. ID: {}", boletin.getIdBoletin());

        return pdfBytes;
    }
}
