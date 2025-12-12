package com.udistrital.gestionacademica.servicio;

import com.udistrital.gestionacademica.modelo.Acudiente;
import com.udistrital.gestionacademica.modelo.Citacion;
import com.udistrital.gestionacademica.modelo.Estudiante;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class emailService {

    private final JavaMailSender mailSender;

    @Value("${institucion.nombre:Institución Educativa}")
    private String nombreInstitucion;

    @Value("${institucion.direccion:}")
    private String direccionInstitucion;

    @Value("${institucion.telefono:}")
    private String telefonoInstitucion;

    @Value("${institucion.correo:}")
    private String correoInstitucion;

    @Value("${spring.mail.username}")
    private String correoRemitente;

    /**
     * Enviar correo de citación de forma asíncrona
     */
    @Async
    public void enviarCorreoCitacion(Citacion citacion) {
        System.out.println("Enviando correo de citación de forma asíncrona...");
        try {
            Estudiante estudiante = citacion.getEstudiante();
            Acudiente acudiente = citacion.getAcudiente();

            if (acudiente == null || acudiente.getCorreoElectronico() == null) {
                log.warn("No se puede enviar correo: acudiente o correo no disponible para estudiante {}",
                        estudiante.getCodigoEstudiante());
                return;
            }

            String correoDestino = acudiente.getCorreoElectronico();
            String asunto = "Citación - " + nombreInstitucion;
            String contenidoHtml = construirCorreoCitacion(citacion);

            System.out.println("Contenido del correo: " + correoDestino);
            enviarCorreoHtml(correoDestino, asunto, contenidoHtml);

            log.info("Correo de citación enviado exitosamente a: {}", correoDestino);

        } catch (Exception e) {
            log.error("Error al enviar correo de citación: {}", e.getMessage(), e);
        }
    }

    /**
     * Enviar correo HTML
     */
    private void enviarCorreoHtml(String destinatario, String asunto, String contenidoHtml)
            throws MessagingException {

        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

        helper.setFrom(correoRemitente);
        helper.setTo(destinatario);
        helper.setSubject(asunto);
        helper.setText(contenidoHtml, true);

        mailSender.send(mensaje);
    }

    /**
     * Construir el contenido HTML del correo de citación
     */
    private String construirCorreoCitacion(Citacion citacion) {
        Estudiante estudiante = citacion.getEstudiante();
        Acudiente acudiente = citacion.getAcudiente();
        LocalDateTime fechaCitacion = citacion.getFechaCitacion();

        String nombreEstudiante = estudiante.getPersona().getNombre() + " " + estudiante.getPersona().getApellido();
        String nombreAcudiente = acudiente.getPersona().getNombre() + " "
                + acudiente.getPersona().getApellido();
        String nombreGrupo = estudiante.getGrupo() != null
                ? estudiante.getGrupo().getGrado().getNombreGrado() + " - Grupo "
                + estudiante.getGrupo().getNumeroGrupo() : "Sin grupo";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy");
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        String fechaFormateada = fechaCitacion.format(formatter);
        String horaFormateada = fechaCitacion.format(horaFormatter);

        return String.format("""
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: white;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 0 20px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #2563eb 0%%, #1d4ed8 100%%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                    }
                    .header p {
                        margin: 5px 0 0 0;
                        opacity: 0.9;
                        font-size: 14px;
                    }
                    .content {
                        padding: 30px;
                    }
                    .greeting {
                        font-size: 18px;
                        color: #2563eb;
                        margin-bottom: 20px;
                    }
                    .message {
                        margin-bottom: 25px;
                        font-size: 15px;
                        line-height: 1.8;
                    }
                    .info-box {
                        background: #f8fafc;
                        border-left: 4px solid #2563eb;
                        padding: 20px;
                        margin: 25px 0;
                        border-radius: 5px;
                    }
                    .info-item {
                        display: flex;
                        margin: 10px 0;
                        padding: 8px 0;
                        border-bottom: 1px solid #e2e8f0;
                    }
                    .info-item:last-child {
                        border-bottom: none;
                    }
                    .info-label {
                        font-weight: 600;
                        color: #64748b;
                        width: 140px;
                        flex-shrink: 0;
                    }
                    .info-value {
                        color: #1e293b;
                        flex: 1;
                    }
                    .date-highlight {
                        background: #dbeafe;
                        padding: 20px;
                        border-radius: 8px;
                        text-align: center;
                        margin: 25px 0;
                    }
                    .date-highlight .date {
                        font-size: 20px;
                        font-weight: bold;
                        color: #1d4ed8;
                        margin-bottom: 5px;
                    }
                    .date-highlight .time {
                        font-size: 24px;
                        font-weight: bold;
                        color: #2563eb;
                    }
                    .important-note {
                        background: #fef3c7;
                        border-left: 4px solid #f59e0b;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .important-note strong {
                        color: #92400e;
                    }
                    .footer {
                        background: #f8fafc;
                        padding: 25px;
                        text-align: center;
                        font-size: 13px;
                        color: #64748b;
                        border-top: 1px solid #e2e8f0;
                    }
                    .footer p {
                        margin: 5px 0;
                    }
                    .footer strong {
                        color: #1e293b;
                    }
                    @media only screen and (max-width: 600px) {
                        .container {
                            margin: 0;
                            border-radius: 0;
                        }
                        .content {
                            padding: 20px;
                        }
                        .info-item {
                            flex-direction: column;
                        }
                        .info-label {
                            width: 100%%;
                            margin-bottom: 5px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📧 Citación Escolar</h1>
                        <p>%s</p>
                    </div>
                    
                    <div class="content">
                        <div class="greeting">
                            Estimado(a) %s,
                        </div>
                        
                        <div class="message">
                            Por medio de la presente, nos permitimos comunicarle que se ha programado 
                            una citación para tratar asuntos relacionados con el estudiante a su cargo.
                        </div>
                        
                        <div class="info-box">
                            <div class="info-item">
                                <span class="info-label">👨‍🎓 Estudiante:</span>
                                <span class="info-value">%s</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">📚 Grupo:</span>
                                <span class="info-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="date-highlight">
                            <div class="date">📅 %s</div>
                            <div class="time">⏰ %s</div>
                        </div>
                        
                        <div class="important-note">
                            <strong>⚠️ Importante:</strong> Solicitamos su puntual asistencia. 
                            En caso de no poder asistir, por favor comuníquese con anticipación 
                            a los números de contacto de la institución.
                        </div>
                        
                        <div class="message">
                            Agradecemos de antemano su atención y colaboración. Quedamos atentos 
                            a su confirmación de asistencia.
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p><strong>%s</strong></p>
                        <p>%s</p>
                        <p>📞 %s</p>
                        <p>✉️ %s</p>
                        <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 15px 0;">
                        <p style="font-size: 11px; color: #94a3b8;">
                            Este es un correo automático, por favor no responda a este mensaje.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
                nombreInstitucion,
                nombreAcudiente,
                nombreEstudiante,
                nombreGrupo,
                fechaFormateada,
                horaFormateada,
                nombreInstitucion,
                direccionInstitucion,
                telefonoInstitucion,
                correoInstitucion
        );
    }

    /**
     * Enviar correo de prueba
     */
    public void enviarCorreoPrueba(String destinatario) throws MessagingException {
        String asunto = "Correo de Prueba - " + nombreInstitucion;
        String contenido = """
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <h2 style="color: #2563eb;">✅ Configuración de Correo Exitosa</h2>
                <p>Este es un correo de prueba del sistema de gestión académica.</p>
                <p>Si recibió este mensaje, significa que el servicio de correo está funcionando correctamente.</p>
                <hr>
                <p style="color: #64748b; font-size: 12px;">%s</p>
            </body>
            </html>
            """.formatted(nombreInstitucion);

        enviarCorreoHtml(destinatario, asunto, contenido);
        log.info("Correo de prueba enviado a: {}", destinatario);
    }

    /**
     * Enviar correo de notificación de entrevista para preinscripción
     */
    @Async
    public void enviarCorreoEntrevistaPreinscripcion(
            String correoDestino,
            String nombreAcudiente,
            String nombreEstudiante,
            LocalDateTime fechaEntrevista,
            String lugarEntrevista) {
        
        log.info("Enviando correo de notificación de entrevista a: {}", correoDestino);
        
        try {
            String asunto = "Entrevista Programada - " + nombreInstitucion;
            String contenidoHtml = construirCorreoEntrevista(
                nombreAcudiente, nombreEstudiante, fechaEntrevista, lugarEntrevista);
            
            enviarCorreoHtml(correoDestino, asunto, contenidoHtml);
            log.info("Correo de entrevista enviado exitosamente a: {}", correoDestino);
            
        } catch (Exception e) {
            log.error("Error al enviar correo de entrevista: {}", e.getMessage(), e);
        }
    }

    /**
     * Construir contenido HTML para correo de entrevista
     */
    private String construirCorreoEntrevista(
            String nombreAcudiente,
            String nombreEstudiante,
            LocalDateTime fechaEntrevista,
            String lugarEntrevista) {
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy");
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        String fechaFormateada = fechaEntrevista.format(formatter);
        String horaFormateada = fechaEntrevista.format(horaFormatter);

        return String.format("""
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: white;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 0 20px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #10b981 0%%, #059669 100%%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                    }
                    .header p {
                        margin: 5px 0 0 0;
                        opacity: 0.9;
                        font-size: 14px;
                    }
                    .content {
                        padding: 30px;
                    }
                    .greeting {
                        font-size: 18px;
                        color: #10b981;
                        margin-bottom: 20px;
                    }
                    .message {
                        margin-bottom: 25px;
                        font-size: 15px;
                        line-height: 1.8;
                    }
                    .info-box {
                        background: #f0fdf4;
                        border-left: 4px solid #10b981;
                        padding: 20px;
                        margin: 25px 0;
                        border-radius: 5px;
                    }
                    .info-item {
                        display: flex;
                        margin: 10px 0;
                        padding: 8px 0;
                        border-bottom: 1px solid #d1fae5;
                    }
                    .info-item:last-child {
                        border-bottom: none;
                    }
                    .info-label {
                        font-weight: 600;
                        color: #064e3b;
                        width: 140px;
                        flex-shrink: 0;
                    }
                    .info-value {
                        color: #1e293b;
                        flex: 1;
                    }
                    .date-highlight {
                        background: #d1fae5;
                        padding: 20px;
                        border-radius: 8px;
                        text-align: center;
                        margin: 25px 0;
                    }
                    .date-highlight .date {
                        font-size: 20px;
                        font-weight: bold;
                        color: #059669;
                        margin-bottom: 5px;
                    }
                    .date-highlight .time {
                        font-size: 24px;
                        font-weight: bold;
                        color: #10b981;
                    }
                    .location-box {
                        background: #fef3c7;
                        border-left: 4px solid #f59e0b;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .location-box strong {
                        color: #92400e;
                    }
                    .footer {
                        background: #f8fafc;
                        padding: 25px;
                        text-align: center;
                        font-size: 13px;
                        color: #64748b;
                        border-top: 1px solid #e2e8f0;
                    }
                    .footer p {
                        margin: 5px 0;
                    }
                    .footer strong {
                        color: #1e293b;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📅 Entrevista de Preinscripción</h1>
                        <p>%s</p>
                    </div>
                    
                    <div class="content">
                        <div class="greeting">
                            Estimado(a) %s,
                        </div>
                        
                        <div class="message">
                            Nos complace informarle que hemos programado la entrevista de preinscripción 
                            para el aspirante <strong>%s</strong>.
                        </div>
                        
                        <div class="date-highlight">
                            <div class="date">📅 %s</div>
                            <div class="time">⏰ %s</div>
                        </div>
                        
                        <div class="location-box">
                            <strong>📍 Lugar:</strong> %s
                        </div>
                        
                        <div class="message">
                            <strong>Puntos a tratar durante la entrevista:</strong>
                            <ul>
                                <li>Presentación del aspirante y acudiente</li>
                                <li>Información sobre la institución y proyecto educativo</li>
                                <li>Requisitos y documentación necesaria</li>
                                <li>Proceso de matrícula</li>
                                <li>Resolución de dudas e inquietudes</li>
                            </ul>
                        </div>
                        
                        <div class="message" style="background: #f0f9ff; padding: 15px; border-radius: 5px;">
                            <strong>⚠️ Importante:</strong> Por favor llegue 10 minutos antes de la hora programada. 
                            Traiga consigo los documentos del aspirante (registro civil, certificado de vacunas, 
                            y últimos certificados académicos si aplica).
                        </div>
                        
                        <div class="message">
                            En caso de no poder asistir, por favor comuníquese con anticipación 
                            a los números de contacto de la institución.
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p><strong>%s</strong></p>
                        <p>%s</p>
                        <p>📞 %s</p>
                        <p>✉️ %s</p>
                        <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 15px 0;">
                        <p style="font-size: 11px; color: #94a3b8;">
                            Este es un correo automático, por favor no responda a este mensaje.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
                nombreInstitucion,
                nombreAcudiente,
                nombreEstudiante,
                fechaFormateada,
                horaFormateada,
                lugarEntrevista,
                nombreInstitucion,
                direccionInstitucion,
                telefonoInstitucion,
                correoInstitucion
        );
    }
}
