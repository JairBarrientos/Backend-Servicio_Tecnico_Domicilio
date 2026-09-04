package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.ReclamacionRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.ReclamacionResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.model.LibroReclamacion;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Ticket;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Usuario;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.LibroReclamacionRepository;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.TicketRepository;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LibroReclamacionService {

    private final LibroReclamacionRepository libroReclamacionRepository;
    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;

    public LibroReclamacionService(LibroReclamacionRepository libroReclamacionRepository,
                                    TicketRepository ticketRepository,
                                    UsuarioRepository usuarioRepository) {
        this.libroReclamacionRepository = libroReclamacionRepository;
        this.ticketRepository = ticketRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public ReclamacionResponse crear(String correoCliente, ReclamacionRequest request) {
        Usuario cliente = usuarioRepository.findByCorreo(correoCliente)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Ticket ticket = null;
        if (request.getTicketId() != null) {
            ticket = ticketRepository.findById(request.getTicketId())
                    .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        }

        Usuario tecnico = null;
        if (request.getTecnicoId() != null) {
            tecnico = usuarioRepository.findById(request.getTecnicoId())
                    .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));
        }

        String codigo = "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        LibroReclamacion reclamo = LibroReclamacion.builder()
                .ticket(ticket)
                .cliente(cliente)
                .tecnico(tecnico)
                .tipoReclamo(request.getTipoReclamo())
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .prioridad(request.getPrioridad() != null ? request.getPrioridad() : "MEDIA")
                .estado("PENDIENTE")
                .esAnonimo(request.getEsAnonimo() != null ? request.getEsAnonimo() : false)
                .codigoSeguimiento(codigo)
                .build();

        LibroReclamacion guardado = libroReclamacionRepository.save(reclamo);
        return mapearResponse(guardado);
    }

    public ReclamacionResponse consultarPorCodigo(String codigo) {
        LibroReclamacion reclamo = libroReclamacionRepository.findByCodigoSeguimiento(codigo)
                .orElseThrow(() -> new RuntimeException("Reclamo no encontrado"));
        return mapearResponse(reclamo);
    }

    private ReclamacionResponse mapearResponse(LibroReclamacion reclamo) {
        return ReclamacionResponse.builder()
                .id(reclamo.getId())
                .codigoSeguimiento(reclamo.getCodigoSeguimiento())
                .tipoReclamo(reclamo.getTipoReclamo())
                .titulo(reclamo.getTitulo())
                .descripcion(reclamo.getDescripcion())
                .prioridad(reclamo.getPrioridad())
                .estado(reclamo.getEstado())
                .fechaCreacion(reclamo.getFechaCreacion())
                .respuesta(reclamo.getRespuesta())
                .build();
    }
}