package com.udistrital.gestionacademica.servicio;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.udistrital.gestionacademica.modelo.Estudiante;
import com.udistrital.gestionacademica.modelo.Grupo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class PdfGeneratorService {

    /**
     * Genera un PDF con el listado de estudiantes de un grupo
     *
     * @param grupo Grupo del cual generar el listado
     * @param estudiantes Lista de estudiantes del grupo
     * @return Array de bytes del PDF generado
     */
    public byte[] generarListadoEstudiantes(Grupo grupo, List<Estudiante> estudiantes) {
        log.info("Generando PDF para grupo: {} - Grupo {}",
                grupo.getGrado().getNombreGrado(), grupo.getNumeroGrupo());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // Crear documento PDF
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Colores personalizados
            DeviceRgb azulPrimario = new DeviceRgb(37, 99, 235);
            DeviceRgb grisClaro = new DeviceRgb(248, 250, 252);

            // Encabezado del documento
            Paragraph titulo = new Paragraph("LISTADO DE ESTUDIANTES")
                    .setFontSize(20)
                    .setBold()
                    .setFontColor(azulPrimario)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(titulo);

            // Información del grupo
            Paragraph infoGrupo = new Paragraph(
                    String.format("Grado: %s - Grupo: %d",
                            grupo.getGrado().getNombreGrado(),
                            grupo.getNumeroGrupo()))
                    .setFontSize(14)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            document.add(infoGrupo);

            // Director de grupo
            if (grupo.getDirectorGrupo() != null) {
                String nombreDirector = grupo.getDirectorGrupo().getPersona().getNombre() + " " + 
                                       grupo.getDirectorGrupo().getPersona().getApellido();
                Paragraph director = new Paragraph("Director de Grupo: " + nombreDirector)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(5);
                document.add(director);
            }

            // Fecha de generación
            Paragraph fecha = new Paragraph(
                    "Fecha de generación: " + LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(fecha);

            // Estadísticas del grupo
            Paragraph stats = new Paragraph(
                    String.format("Total de estudiantes: %d / %d",
                            estudiantes.size(),
                            10))
                    .setFontSize(11)
                    .setBold()
                    .setMarginBottom(15);
            document.add(stats);

            // Tabla de estudiantes
            float[] columnWidths = {1, 3, 3, 2, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Encabezados de la tabla
            String[] headers = {"#", "Nombre", "Apellido", "Documento", "Estado"};
            for (String header : headers) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(header).setBold())
                        .setBackgroundColor(azulPrimario)
                        .setFontColor(new DeviceRgb(255, 255, 255))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(8);
                table.addHeaderCell(headerCell);
            }

            // Datos de estudiantes
            int contador = 1;
            for (Estudiante estudiante : estudiantes) {
                // Alternar color de filas
                DeviceRgb bgColor = (contador % 2 == 0) ? grisClaro : new DeviceRgb(255, 255, 255);

                // Número
                table.addCell(new Cell()
                        .add(new Paragraph(String.valueOf(contador)))
                        .setBackgroundColor(bgColor)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(6));

                // Nombre
                table.addCell(new Cell()
                        .add(new Paragraph(estudiante.getPersona().getNombre()))
                        .setBackgroundColor(bgColor)
                        .setPadding(6));

                // Apellido
                table.addCell(new Cell()
                        .add(new Paragraph(estudiante.getPersona().getApellido()))
                        .setBackgroundColor(bgColor)
                        .setPadding(6));

                // Documento
                String documento = estudiante.getPersona().getDocumento() != null
                        ? estudiante.getPersona().getDocumento() : "N/A";
                table.addCell(new Cell()
                        .add(new Paragraph(documento))
                        .setBackgroundColor(bgColor)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(6));

                // Estado
                Cell estadoCell = new Cell()
                        .add(new Paragraph(estudiante.getEstado()))
                        .setBackgroundColor(bgColor)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(6);

                // Color según estado
                if ("Activo".equalsIgnoreCase(estudiante.getEstado())) {
                    estadoCell.setFontColor(new DeviceRgb(6, 95, 70)); // Verde oscuro
                } else if ("Inactivo".equalsIgnoreCase(estudiante.getEstado())) {
                    estadoCell.setFontColor(new DeviceRgb(153, 27, 27)); // Rojo oscuro
                }

                table.addCell(estadoCell);
                contador++;
            }

            document.add(table);

            // Pie de página
            Paragraph footer = new Paragraph(
                    "Este documento fue generado automáticamente por el Sistema de Gestión Académica")
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
            document.add(footer);

            // Cerrar documento
            document.close();

            log.info("PDF generado exitosamente. Tamaño: {} bytes", baos.size());

            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar PDF", e);
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage());
        }
    }
    /**
     * Genera un boletín de calificaciones para un estudiante
     * 
     * @param estudiante Estudiante al que pertenece el boletín
     * @param periodo Periodo académico
     * @param calificaciones Lista de calificaciones
     * @return Array de bytes del PDF generado
     */
    public byte[] generarBoletin(Estudiante estudiante, com.udistrital.gestionacademica.modelo.Periodo periodo, List<com.udistrital.gestionacademica.modelo.Calificacion> calificaciones) {
        log.info("Generando boletín para estudiante: {} - Periodo: {}",
                estudiante.getPersona().getNombre(), periodo.getNombrePeriodo());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Colores
            DeviceRgb azulInstitucional = new DeviceRgb(0, 51, 153); // Azul oscuro
            DeviceRgb grisFondo = new DeviceRgb(240, 240, 240);
            
            // 1. Encabezado Institucional
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1}));
            headerTable.setWidth(UnitValue.createPercentValue(100));
            
            Cell titleCell = new Cell().add(new Paragraph("INSTITUCIÓN EDUCATIVA DISTRITAL PROYECTO FIS")
                    .setFontSize(16).setBold().setFontColor(ColorConstants.WHITE).setTextAlignment(TextAlignment.CENTER))
                    .setBackgroundColor(azulInstitucional)
                    .setPadding(10);
            headerTable.addCell(titleCell);
            
            Cell subTitleCell = new Cell().add(new Paragraph("INFORME DE EVALUACIÓN ACADÉMICA")
                    .setFontSize(14).setBold().setTextAlignment(TextAlignment.CENTER))
                    .setPadding(5);
            headerTable.addCell(subTitleCell);
            
            document.add(headerTable);
            document.add(new Paragraph("\n")); // Espacio
            
            // 2. Información del Estudiante y Periodo
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            infoTable.setWidth(UnitValue.createPercentValue(100));
            
            // Columna Izquierda: Datos Estudiante
            log.info("Obteniendo datos del estudiante...");
            if (estudiante.getPersona() == null) {
                log.error("El estudiante no tiene persona asociada");
                throw new RuntimeException("El estudiante no tiene información personal asociada");
            }
            
            String nombreCompleto = estudiante.getPersona().getNombre() + " " + estudiante.getPersona().getApellido();
            String documento = estudiante.getPersona().getDocumento() != null ? 
                    estudiante.getPersona().getDocumento() : "SIN DOCUMENTO";
            
            log.info("Datos del estudiante obtenidos correctamente: {}", nombreCompleto);
            
            Cell studentInfo = new Cell().add(new Paragraph("ESTUDIANTE: " + nombreCompleto.toUpperCase())
                    .setBold().setFontSize(11))
                    .add(new Paragraph("DOCUMENTO: " + documento).setFontSize(10))
                    .setBorder(null);
            
            // Columna Derecha: Datos Periodo/Curso
            Cell academicInfo = new Cell()
                    .add(new Paragraph("PERIODO: " + periodo.getNombrePeriodo()).setBold().setFontSize(11).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph("FECHA: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).setFontSize(10).setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(null);
            
            infoTable.addCell(studentInfo);
            infoTable.addCell(academicInfo);
            
            document.add(infoTable);
            document.add(new Paragraph("\n"));
            
            // 3. Tabla de Calificaciones
            Table gradesTable = new Table(UnitValue.createPercentArray(new float[]{5, 2}));
            gradesTable.setWidth(UnitValue.createPercentValue(100));
            
            // Encabezados
            gradesTable.addHeaderCell(new Cell().add(new Paragraph("ASIGNATURA / LOGRO").setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(azulInstitucional).setTextAlignment(TextAlignment.CENTER));
            gradesTable.addHeaderCell(new Cell().add(new Paragraph("CALIFICACIÓN").setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(azulInstitucional).setTextAlignment(TextAlignment.CENTER));
            
            if (calificaciones.isEmpty()) {
                Cell noDataCell = new Cell(1, 2).add(new Paragraph("No se registran calificaciones para este periodo.")
                        .setItalic().setTextAlignment(TextAlignment.CENTER))
                        .setPadding(20);
                gradesTable.addCell(noDataCell);
            } else {
                for (com.udistrital.gestionacademica.modelo.Calificacion cal : calificaciones) {
                   
                    // Nombre del Logro (Asignatura)
                    String nombreLogro = cal.getLogro().getNombreLogro();
                    String descripcionLogro = cal.getLogro().getDescripcion();
                    
                    Cell materiaCell = new Cell()
                            .add(new Paragraph(nombreLogro).setBold())
                            .add(new Paragraph(descripcionLogro).setFontSize(9).setItalic());
                    gradesTable.addCell(materiaCell);
                    
                    // Nota (Asumimos que si tiene el logro asignado es una nota aprobatoria o se muestra 'CUMPLIDO')
                    // Ajuste: El modelo actual solo linkea Logro con Estudiante, no tiene un valor numérico explícito en 'Calificacion'
                    // se asume que la existencia del registro implica cumplimiento.
                    // Si se requiere nota numérica, debería estar en el modelo. Por ahora indicamos "CUMPLIDO".
                    
                    Cell notaCell = new Cell()
                            .add(new Paragraph("CUMPLIDO").setBold().setFontColor(new DeviceRgb(0, 100, 0)))
                            .setTextAlignment(TextAlignment.CENTER)
                            .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);
                    gradesTable.addCell(notaCell);
                }
            }
            
            document.add(gradesTable);
            
            // 4. Pie de página / Firmas
            document.add(new Paragraph("\n\n\n"));
            
            Table signatureTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            signatureTable.setWidth(UnitValue.createPercentValue(100));
            
            Cell signature1 = new Cell().add(new Paragraph("__________________________\nRECTOR(A)")
                    .setTextAlignment(TextAlignment.CENTER).setFontSize(10)).setBorder(null);
            
            Cell signature2 = new Cell().add(new Paragraph("__________________________\nDIRECTOR(A) DE GRUPO")
                    .setTextAlignment(TextAlignment.CENTER).setFontSize(10)).setBorder(null);
            
            signatureTable.addCell(signature1);
            signatureTable.addCell(signature2);
            
            document.add(signatureTable);
            
            // Leyenda final
            document.add(new Paragraph("\nDocumento generado automáticamente por el Sistema de Gestión Académica.")
                    .setFontSize(8).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));
            
            document.close();
            
            log.info("Boletín generado exitosamente. Tamaño: {} bytes", baos.size());
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error al generar boletín", e);
            throw new RuntimeException("Error al generar el boletín: " + e.getMessage());
        }
    }
}
