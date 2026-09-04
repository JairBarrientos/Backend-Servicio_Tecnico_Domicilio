package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.MensajeChatRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.MensajeChatResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.service.MensajeChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final MensajeChatService mensajeChatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(MensajeChatService mensajeChatService, SimpMessagingTemplate messagingTemplate) {
        this.mensajeChatService = mensajeChatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/ticket/{ticketId}/mensaje")
    public void enviarMensaje(@DestinationVariable Long ticketId, MensajeChatWebSocketPayload payload) {
        MensajeChatRequest request = new MensajeChatRequest();
        request.setContenido(payload.getContenido());
        request.setTipoMensaje(payload.getTipoMensaje());
        request.setArchivoUrl(payload.getArchivoUrl());

        MensajeChatResponse response = mensajeChatService.enviar(ticketId, payload.getCorreoRemitente(), request);

        messagingTemplate.convertAndSend("/topic/ticket/" + ticketId, response);
    }

    public static class MensajeChatWebSocketPayload {
        private String correoRemitente;
        private String contenido;
        private String tipoMensaje;
        private String archivoUrl;

        public String getCorreoRemitente() { return correoRemitente; }
        public void setCorreoRemitente(String correoRemitente) { this.correoRemitente = correoRemitente; }
        public String getContenido() { return contenido; }
        public void setContenido(String contenido) { this.contenido = contenido; }
        public String getTipoMensaje() { return tipoMensaje; }
        public void setTipoMensaje(String tipoMensaje) { this.tipoMensaje = tipoMensaje; }
        public String getArchivoUrl() { return archivoUrl; }
        public void setArchivoUrl(String archivoUrl) { this.archivoUrl = archivoUrl; }
    }
}