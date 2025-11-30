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
            if (grupo.getDirectorGrupo() != null && !grupo.getDirectorGrupo().isEmpty()) {
                Paragraph director = new Paragraph("Director de Grupo: " + grupo.getDirectorGrupo())
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
                            grupo.getLimiteEstudiantes()))
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
}
