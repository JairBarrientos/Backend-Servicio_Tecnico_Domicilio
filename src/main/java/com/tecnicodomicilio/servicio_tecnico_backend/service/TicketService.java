package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.CambiarEstadoRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.TicketRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.TicketResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.model.*;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;
    private final CatalogoServicioRepository catalogoServicioRepository;
    private final DireccionUsuarioRepository direccionUsuarioRepository;
    private final HistorialEstadoTicketRepository historialRepository;
    private final TecnicoDetalleRepository tecnicoDetalleRepository;

    public TicketService(TicketRepository ticketRepository, UsuarioRepository usuarioRepository,
                          CatalogoServicioRepository catalogoServicioRepository,
                          DireccionUsuarioRepository direccionUsuarioRepository,
                          HistorialEstadoTicketRepository historialRepository,
                          TecnicoDetalleRepository tecnicoDetalleRepository) {
        this.ticketRepository = ticketRepository;
        this.usuarioRepository = usuarioRepository;
        this.catalogoServicioRepository = catalogoServicioRepository;
        this.direccionUsuarioRepository = direccionUsuarioRepository;
        this.historialRepository = historialRepository;
        this.tecnicoDetalleRepository = tecnicoDetalleRepository;
    }

    public TicketResponse crear(String correoCliente, TicketRequest request) {
        Usuario cliente = usuarioRepository.findByCorreo(correoCliente)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        CatalogoServicio servicio = null;
        if (request.getServicioId() != null) {
            servicio = catalogoServicioRepository.findById(request.getServicioId())
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        }

        DireccionUsuario direccion = null;
        if (request.getDireccionId() != null) {
            direccion = direccionUsuarioRepository.findById(request.getDireccionId())
                    .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));
        }

        Ticket ticket = Ticket.builder()
                .cliente(cliente)
                .servicio(servicio)
                .direccion(direccion)
                .direccionPersonalizada(request.getDireccionPersonalizada())
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .tipoDispositivo(request.getTipoDispositivo())
                .marca(request.getMarca())
                .modelo(request.getModelo())
                .urgencia(request.getUrgencia() != null ? request.getUrgencia() : "MEDIA")
                .precioOfertado(request.getPrecioOfertado())
                .estado("PENDIENTE")
                .build();

        if (request.getLatitud() != null && request.getLongitud() != null) {
            java.time.LocalDateTime fechaReferencia = java.time.LocalDateTime.now();

            tecnicoDetalleRepository.buscarTecnicoMasCercanoDisponible(
                    request.getLatitud(), request.getLongitud(), fechaReferencia
            ).ifPresent(tecnicoId -> {
                Usuario tecnico = usuarioRepository.findById(tecnicoId).orElse(null);
                if (tecnico != null) {
                    ticket.setTecnicoAsignado(tecnico);
                    ticket.setEstado("PENDIENTE_ACEPTACION");
                }
            });
        }

        Ticket guardado = ticketRepository.save(ticket);
        return mapearResponse(guardado);
    }

    public List<TicketResponse> listarPorCliente(String correoCliente) {
        Usuario cliente = usuarioRepository.findByCorreo(correoCliente)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ticketRepository.findByClienteId(cliente.getId()).stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    public TicketResponse obtenerDetalle(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        return mapearResponse(ticket);
    }

    public TicketResponse cambiarEstado(Long ticketId, String correoUsuario, CambiarEstadoRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ticket.setEstado(request.getNuevoEstado());
        Ticket actualizado = ticketRepository.save(ticket);

        return mapearResponse(actualizado);
    }

    public TicketResponse asignarTecnico(Long ticketId, Long tecnicoId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        Usuario tecnico = usuarioRepository.findById(tecnicoId)
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));

        ticket.setTecnicoAsignado(tecnico);
        ticket.setEstado("ASIGNADO");

        Ticket actualizado = ticketRepository.save(ticket);
        return mapearResponse(actualizado);
    }

    public TicketResponse cancelar(Long ticketId, String correoCliente, String motivo) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        ticket.setEstado("CANCELADO");
        ticket.setMensajeCancelacion(motivo);

        Ticket actualizado = ticketRepository.save(ticket);
        return mapearResponse(actualizado);
    }

    public TicketResponse calificar(Long ticketId, Integer calificacion, String comentario) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        ticket.setCalificacionCliente(calificacion);
        ticket.setComentarioCliente(comentario);

        Ticket actualizado = ticketRepository.save(ticket);
        return mapearResponse(actualizado);
    }

    public TicketResponse aceptar(Long ticketId, Long tecnicoId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        if (!ticket.getTecnicoAsignado().getId().equals(tecnicoId)) {
            throw new RuntimeException("Este ticket no te fue asignado");
        }

        ticket.setEstado("ASIGNADO");
        Ticket actualizado = ticketRepository.save(ticket);
        return mapearResponse(actualizado);
    }

    public TicketResponse rechazar(Long ticketId, Long tecnicoId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        ticket.setEstado("PENDIENTE");
        ticket.setTecnicoAsignado(null);
        Ticket actualizado = ticketRepository.save(ticket);
        return mapearResponse(actualizado);
    }

    public TicketResponse contraofertar(Long ticketId, Long tecnicoId, BigDecimal nuevoPrecio) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        ticket.setPrecioContraoferta(nuevoPrecio);
        Ticket actualizado = ticketRepository.save(ticket);
        return mapearResponse(actualizado);
    }

    public TicketResponse aceptarContraoferta(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        ticket.setPrecioOfertado(ticket.getPrecioContraoferta());
        ticket.setEstado("ASIGNADO");
        Ticket actualizado = ticketRepository.save(ticket);
        return mapearResponse(actualizado);
    }

    private TicketResponse mapearResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .codigoTicket(ticket.getCodigoTicket())
                .clienteId(ticket.getCliente().getId())
                .clienteNombre(ticket.getCliente().getNombres() + " " + ticket.getCliente().getApellidos())
                .tecnicoAsignadoId(ticket.getTecnicoAsignado() != null ? ticket.getTecnicoAsignado().getId() : null)
                .tecnicoNombre(ticket.getTecnicoAsignado() != null
                        ? ticket.getTecnicoAsignado().getNombres() + " " + ticket.getTecnicoAsignado().getApellidos()
                        : null)
                .tituloServicio(ticket.getServicio() != null ? ticket.getServicio().getNombreServicio() : null)
                .titulo(ticket.getTitulo())
                .descripcion(ticket.getDescripcion())
                .tipoDispositivo(ticket.getTipoDispositivo())
                .marca(ticket.getMarca())
                .modelo(ticket.getModelo())
                .urgencia(ticket.getUrgencia())
                .estado(ticket.getEstado())
                .fechaCreacion(ticket.getFechaCreacion())
                .fechaCita(ticket.getFechaCita())
                .precioFinal(ticket.getPrecioFinal())
                .precioOfertado(ticket.getPrecioOfertado())
                .precioContraoferta(ticket.getPrecioContraoferta())
                .build();
    }

    public List<TicketResponse> listarPendientesTecnico(String correoTecnico) {
        Usuario tecnico = usuarioRepository.findByCorreo(correoTecnico)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ticketRepository.findByTecnicoAsignadoId(tecnico.getId()).stream()
                .filter(t -> "PENDIENTE_ACEPTACION".equals(t.getEstado()))
                .map(this::mapearResponse)
                .collect(Collectors.toList());
        }
}