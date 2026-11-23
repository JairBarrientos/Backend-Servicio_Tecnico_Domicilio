package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.FacturaResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Factura;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Ticket;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Usuario;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.FacturaRepository;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.TicketRepository;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FacturaService {

    private static final BigDecimal FACTOR_IGV = new BigDecimal("1.18");

    private final FacturaRepository facturaRepository;
    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;

    public FacturaService(FacturaRepository facturaRepository, TicketRepository ticketRepository,
                           UsuarioRepository usuarioRepository) {
        this.facturaRepository = facturaRepository;
        this.ticketRepository = ticketRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Factura emitir(Ticket ticket) {
        return facturaRepository.findByTicketId(ticket.getId()).orElseGet(() -> {
            if (!"FINALIZADO".equals(ticket.getEstado())) {
                throw new RuntimeException("El ticket debe estar FINALIZADO para generar factura");
            }
            if (ticket.getPrecioFinal() == null) {
                throw new RuntimeException("El ticket no tiene un total definido");
            }

            BigDecimal total = ticket.getPrecioFinal();
            BigDecimal subtotal = total.divide(FACTOR_IGV, 2, RoundingMode.HALF_UP);
            BigDecimal impuesto = total.subtract(subtotal);
            boolean pagado = Boolean.TRUE.equals(ticket.getPagado());

            Factura factura = Factura.builder()
                    .ticket(ticket)
                    .numeroFactura(String.format("F001-%06d", ticket.getId()))
                    .subtotal(subtotal)
                    .descuento(BigDecimal.ZERO)
                    .impuesto(impuesto)
                    .total(total)
                    .metodoPago(ticket.getMetodoPago() != null ? ticket.getMetodoPago() : "EFECTIVO")
                    .estadoPago(pagado ? "PAGADO" : "PENDIENTE")
                    .fechaPago(pagado ? ticket.getFechaPago() : null)
                    .build();

            return facturaRepository.save(factura);
        });
    }

    public FacturaResponse generar(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        return mapearResponse(emitir(ticket));
    }

    public FacturaResponse obtenerPorTicket(Long ticketId, String correo) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        exigirAcceso(ticket, correo);

        Factura factura = facturaRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new RuntimeException("Este ticket aún no tiene factura"));
        return mapearResponse(factura);
    }

    public Factura obtenerAutorizada(Long facturaId, String correo) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        exigirAcceso(factura.getTicket(), correo);
        return factura;
    }

    private void exigirAcceso(Ticket ticket, String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean admin = "ADMIN".equals(usuario.getRol());
        boolean cliente = ticket.getCliente().getId().equals(usuario.getId());
        boolean tecnico = ticket.getTecnicoAsignado() != null
                && ticket.getTecnicoAsignado().getId().equals(usuario.getId());

        if (!admin && !cliente && !tecnico) {
            throw new RuntimeException("No tienes acceso a esta factura");
        }
    }

    private FacturaResponse mapearResponse(Factura factura) {
        Ticket ticket = factura.getTicket();

        return FacturaResponse.builder()
                .id(factura.getId())
                .numeroFactura(factura.getNumeroFactura())
                .ticketId(ticket.getId())
                .codigoTicket(ticket.getCodigoTicket())
                .tituloServicio(ticket.getServicio() != null ? ticket.getServicio().getNombreServicio() : ticket.getTitulo())
                .clienteNombre(ticket.getCliente().getNombres() + " " + ticket.getCliente().getApellidos())
                .tecnicoNombre(ticket.getTecnicoAsignado() != null
                        ? ticket.getTecnicoAsignado().getNombres() + " " + ticket.getTecnicoAsignado().getApellidos()
                        : null)
                .subtotal(factura.getSubtotal())
                .descuento(factura.getDescuento())
                .impuesto(factura.getImpuesto())
                .total(factura.getTotal())
                .metodoPago(factura.getMetodoPago())
                .estadoPago(factura.getEstadoPago())
                .fechaEmision(factura.getFechaEmision())
                .fechaPago(factura.getFechaPago())
                .build();
    }
}