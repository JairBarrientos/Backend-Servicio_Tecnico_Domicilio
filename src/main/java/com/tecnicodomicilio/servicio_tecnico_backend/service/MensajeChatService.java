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

    private static final List<String> ESTADOS_CHAT =
            List.of("ASIGNADO", "EN_CAMINO", "EN_REPARACION", "COTIZACION_PENDIENTE");

    private final MensajeChatRepository mensajeChatRepository;
    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;

    public MensajeChatService(MensajeChatRepository mensajeChatRepository, TicketRepository ticketRepository,
                               UsuarioRepository usuarioRepository) {
        this.mensajeChatRepository = mensajeChatRepository;
        this.ticketRepository = ticketRepository;
        this.usuarioRepository = usuarioRepository;
    }

    private void exigirParticipante(Ticket ticket, Usuario usuario, boolean soloLectura) {
        boolean cliente = ticket.getCliente().getId().equals(usuario.getId());
        boolean tecnico = ticket.getTecnicoAsignado() != null
                && ticket.getTecnicoAsignado().getId().equals(usuario.getId());
        boolean admin = "ADMIN".equals(usuario.getRol());

        if (!cliente && !tecnico && !(admin && soloLectura)) {
            throw new RuntimeException("No tienes acceso al chat de este ticket");
        }
        if (!soloLectura && !ESTADOS_CHAT.contains(ticket.getEstado())) {
            throw new RuntimeException("El chat solo está disponible mientras el servicio está en curso");
        }
    }

    public List<MensajeChatResponse> listar(Long ticketId, String correo) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        exigirParticipante(ticket, usuario, true);

        List<MensajeChat> mensajes = mensajeChatRepository.findByTicketIdOrderByFechaEnvioAsc(ticketId);

        if (!"ADMIN".equals(usuario.getRol())) {
            List<MensajeChat> sinLeer = mensajes.stream()
                    .filter(m -> !m.getRemitente().getId().equals(usuario.getId())
                            && !Boolean.TRUE.equals(m.getLeido()))
                    .collect(Collectors.toList());

            if (!sinLeer.isEmpty()) {
                sinLeer.forEach(m -> m.setLeido(true));
                mensajeChatRepository.saveAll(sinLeer);
            }
        }

        return mensajes.stream()
                .map(m -> mapearResponse(m, usuario))
                .collect(Collectors.toList());
    }

    public MensajeChatResponse enviar(Long ticketId, String correoRemitente, MensajeChatRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        Usuario remitente = usuarioRepository.findByCorreo(correoRemitente)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        exigirParticipante(ticket, remitente, false);

        String contenido = request.getContenido() != null ? request.getContenido().trim() : "";
        if (contenido.isEmpty()) {
            throw new RuntimeException("Escribe un mensaje");
        }
        if (contenido.length() > 1000) {
            throw new RuntimeException("El mensaje es demasiado largo (máximo 1000 caracteres)");
        }

        MensajeChat mensaje = MensajeChat.builder()
                .ticket(ticket)
                .remitente(remitente)
                .remitenteRol(remitente.getRol())
                .contenido(contenido)
                .tipoMensaje(request.getTipoMensaje() != null ? request.getTipoMensaje() : "TEXTO")
                .archivoUrl(request.getArchivoUrl())
                .leido(false)
                .build();

        return mapearResponse(mensajeChatRepository.save(mensaje), remitente);
    }

    private MensajeChatResponse mapearResponse(MensajeChat mensaje, Usuario usuarioActual) {
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
                .esMio(mensaje.getRemitente().getId().equals(usuarioActual.getId()))
                .build();
    }
}