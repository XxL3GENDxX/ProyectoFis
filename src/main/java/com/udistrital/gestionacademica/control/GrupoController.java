package com.udistrital.gestionacademica.control;

import com.udistrital.gestionacademica.modelo.Estudiante;
import com.udistrital.gestionacademica.modelo.Grupo;
import com.udistrital.gestionacademica.servicio.GrupoService;
import com.udistrital.gestionacademica.servicio.PdfGeneratorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/grupos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GrupoController {

    private final GrupoService grupoService;
    private final PdfGeneratorService pdfGeneratorService;

    @GetMapping("/grado/{idGrado}")
    public ResponseEntity<?> obtenerGruposPorGrado(@PathVariable Long idGrado) {
        try {
            List<Grupo> grupos = grupoService.obtenerGruposPorGrado(idGrado);
            return ResponseEntity.ok(grupos);
        } catch (Exception e) {
            log.error("Error al obtener grupos del grado", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    private Map<String, Object> crearRespuestaError(String mensaje) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("mensaje", mensaje);
        return response;
    }

    @PostMapping("/crear")
    public ResponseEntity<?> crearGrupo(@RequestBody Map<String, Object> request) {
        try {
            // Extraer datos del request
            Grupo grupo = new Grupo();
            grupo.setNumeroGrupo(((Number) request.get("numeroGrupo")).intValue());
            
            // Obtener grado (asumiendo que viene el ID)
            Map<String, Object> gradoData = (Map<String, Object>) request.get("grado");
            com.udistrital.gestionacademica.modelo.Grado grado = new com.udistrital.gestionacademica.modelo.Grado();
            grado.setIdGrado(((Number) gradoData.get("idGrado")).longValue());
            grupo.setGrado(grado);
            
            // Obtener documento de director (opcional)
            String documentoDirector = (String) request.get("documentoDirector");
            
            Grupo nuevoGrupo = grupoService.crearGrupo(grupo, documentoDirector);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoGrupo);
        } catch (RuntimeException e) {
            log.error("Error al crear el grupo: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al crear el grupo", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    // Cambia esto: @PutMapping("/actualizar/{idGrupo}")
// Por esto (Estándar REST):
    @PutMapping("actualizar/{idGrupo}")
    public ResponseEntity<?> actualizarGrupo(
            @PathVariable Long idGrupo, 
            @RequestBody Map<String, Object> request) {
        try {
            // Extraer datos del request
            Grupo grupo = new Grupo();
            grupo.setIdGrupo(idGrupo);
            grupo.setNumeroGrupo(((Number) request.get("numeroGrupo")).intValue());
            
            // Obtener grado
            Map<String, Object> gradoData = (Map<String, Object>) request.get("grado");
            com.udistrital.gestionacademica.modelo.Grado grado = new com.udistrital.gestionacademica.modelo.Grado();
            grado.setIdGrado(((Number) gradoData.get("idGrado")).longValue());
            grupo.setGrado(grado);
            
            // Obtener documento de director (opcional)
            String documentoDirector = (String) request.get("documentoDirector");

            Grupo grupoActualizado = grupoService.actualizarGrupo(grupo, documentoDirector);
            return ResponseEntity.ok(grupoActualizado);
        } catch (RuntimeException e) {
            log.error("Error al actualizar el grupo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al actualizar el grupo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    @DeleteMapping("/eliminar/{idGrupo}")
    public ResponseEntity<?> eliminarGrupo(@PathVariable Long idGrupo) {
        try {
            grupoService.eliminarGrupo(idGrupo);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar el grupo", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearRespuestaError(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al eliminar el grupo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearRespuestaError("Error en la base de datos"));
        }
    }

    @GetMapping("/{idGrupo}/generar-listado-pdf")
    public ResponseEntity<byte[]> generarListadoPdf(@PathVariable Long idGrupo) {
        try {
            log.info("Generando PDF para grupo {}", idGrupo);

            Grupo grupo = grupoService.obtenerGrupoPorId(idGrupo);

            // Obtener estudiantes del grupo
            List<Estudiante> estudiantes = grupo.getEstudiantes() != null
                    ? new ArrayList<>(grupo.getEstudiantes().values()) : new ArrayList<>();

            // Ordenar por apellido
            estudiantes.sort(Comparator.comparing(e -> e.getPersona().getApellido()));

            // Generar PDF
            byte[] pdfBytes = pdfGeneratorService.generarListadoEstudiantes(grupo, estudiantes);

            // Configurar headers para el PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename(String.format("Listado_%s_Grupo%d.pdf",
                            grupo.getGrado().getNombreGrado().replace(" ", "_"),
                            grupo.getNumeroGrupo()))
                    .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (RuntimeException e) {
            log.error("Error al generar PDF del grupo", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        } catch (Exception e) {
            log.error("Error inesperado al generar PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

}
