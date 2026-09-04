package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.MensajeChatRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.MensajeChatResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.model.MensajeChat;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Ticket;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Usuario;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.MensajeChatRepository;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.TicketRepository;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MensajeChatService {

    private final MensajeChatRepository mensajeChatRepository;
    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;

    public MensajeChatService(MensajeChatRepository mensajeChatRepository, TicketRepository ticketRepository,
                               UsuarioRepository usuarioRepository) {
        this.mensajeChatRepository = mensajeChatRepository;
        this.ticketRepository = ticketRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<MensajeChatResponse> listar(Long ticketId) {
        return mensajeChatRepository.findByTicketIdOrderByFechaEnvioAsc(ticketId).stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    public MensajeChatResponse enviar(Long ticketId, String correoRemitente, MensajeChatRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        Usuario remitente = usuarioRepository.findByCorreo(correoRemitente)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        MensajeChat mensaje = MensajeChat.builder()
                .ticket(ticket)
                .remitente(remitente)
                .remitenteRol(remitente.getRol())
                .contenido(request.getContenido())
                .tipoMensaje(request.getTipoMensaje() != null ? request.getTipoMensaje() : "TEXTO")
                .archivoUrl(request.getArchivoUrl())
                .leido(false)
                .build();

        MensajeChat guardado = mensajeChatRepository.save(mensaje);
        return mapearResponse(guardado);
    }

    private MensajeChatResponse mapearResponse(MensajeChat mensaje) {
        return MensajeChatResponse.builder()
                .id(mensaje.getId())
                .remitenteId(mensaje.getRemitente().getId())
                .remitenteNombre(mensaje.getRemitente().getNombres() + " " + mensaje.getRemitente().getApellidos())
                .remitenteRol(mensaje.getRemitenteRol())
                .contenido(mensaje.getContenido())
                .tipoMensaje(mensaje.getTipoMensaje())
                .archivoUrl(mensaje.getArchivoUrl())
                .leido(mensaje.getLeido())
                .fechaEnvio(mensaje.getFechaEnvio())
                .build();
    }
}