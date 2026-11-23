package com.tecnicodomicilio.servicio_tecnico_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void enviarCodigoVerificacion(String destinatario, String codigo) {
        enviar(destinatario,
                "Código de verificación - Servicio Técnico a Domicilio",
                "Tu código de verificación es: " + codigo + "\n\nEste código expira en 15 minutos.");
    }

    @Async
    public void enviarCodigoRecuperacion(String destinatario, String codigo) {
        enviar(destinatario,
                "Recuperación de contraseña - Servicio Técnico a Domicilio",
                "Tu código para recuperar tu contraseña es: " + codigo
                        + "\n\nEste código expira en 15 minutos."
                        + "\nSi no lo pediste tú, ignora este mensaje.");
    }

    private void enviar(String destinatario, String asunto, String texto) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(texto);
            mailSender.send(mensaje);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo a {}: {}", destinatario, e.getMessage());
        }
    }
}