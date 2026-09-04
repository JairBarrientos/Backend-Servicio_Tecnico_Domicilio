package com.tecnicodomicilio.servicio_tecnico_backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCodigoVerificacion(String destinatario, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("Código de verificación - Servicio Técnico a Domicilio");
        mensaje.setText("Tu código de verificación es: " + codigo + "\n\nEste código expira en 15 minutos.");
        mailSender.send(mensaje);
    }
}