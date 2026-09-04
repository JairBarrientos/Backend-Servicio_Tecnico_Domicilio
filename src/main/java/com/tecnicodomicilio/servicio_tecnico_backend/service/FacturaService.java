package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.FacturaResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Factura;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Ticket;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.FacturaRepository;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final TicketRepository ticketRepository;

    public FacturaService(FacturaRepository facturaRepository, TicketRepository ticketRepository) {
        this.facturaRepository = facturaRepository;
        this.ticketRepository = ticketRepository;
    }

    public FacturaResponse generar(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        if (facturaRepository.findByTicketId(ticketId).isPresent()) {
            throw new RuntimeException("Este ticket ya tiene una factura generada");
        }

        if (!ticket.getEstado().equals("FINALIZADO")) {
            throw new RuntimeException("El ticket debe estar FINALIZADO para generar factura");
        }

        BigDecimal subtotal = ticketRepository.calcularPrecioTicket(ticketId);
        BigDecimal impuesto = subtotal.multiply(new BigDecimal("0.18")).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(impuesto);

        String numeroFactura = "F001-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Factura factura = Factura.builder()
                .ticket(ticket)
                .numeroFactura(numeroFactura)
                .subtotal(subtotal)
                .descuento(BigDecimal.ZERO)
                .impuesto(impuesto)
                .total(total)
                .metodoPago(ticket.getMetodoPago())
                .estadoPago(ticket.getPagado() ? "PAGADO" : "PENDIENTE")
                .build();

        Factura guardada = facturaRepository.save(factura);
        return mapearResponse(guardada);
    }

    public FacturaResponse obtenerPorTicket(Long ticketId) {
        Factura factura = facturaRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada para este ticket"));
        return mapearResponse(factura);
    }

    private FacturaResponse mapearResponse(Factura factura) {
        return FacturaResponse.builder()
                .id(factura.getId())
                .numeroFactura(factura.getNumeroFactura())
                .ticketId(factura.getTicket().getId())
                .subtotal(factura.getSubtotal())
                .descuento(factura.getDescuento())
                .impuesto(factura.getImpuesto())
                .total(factura.getTotal())
                .metodoPago(factura.getMetodoPago())
                .estadoPago(factura.getEstadoPago())
                .fechaEmision(factura.getFechaEmision())
                .build();
    }
}