package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.MovimientoCreditoResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.SaldoCreditosResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.model.MovimientoCredito;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Ticket;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Usuario;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.MovimientoCreditoRepository;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.TicketRepository;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreditoService {

    private final MovimientoCreditoRepository movimientoCreditoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TicketRepository ticketRepository;

    public CreditoService(MovimientoCreditoRepository movimientoCreditoRepository,
                           UsuarioRepository usuarioRepository,
                           TicketRepository ticketRepository) {
        this.movimientoCreditoRepository = movimientoCreditoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ticketRepository = ticketRepository;
    }

    public SaldoCreditosResponse consultarSaldo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return SaldoCreditosResponse.builder().saldo(usuario.getSaldoCreditos()).build();
    }

    public List<MovimientoCreditoResponse> listarMovimientos(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return movimientoCreditoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId()).stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    public MovimientoCreditoResponse abonar(Long usuarioId, Long ticketId, BigDecimal monto, String descripcion) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Ticket ticket = null;
        if (ticketId != null) {
            ticket = ticketRepository.findById(ticketId).orElse(null);
        }

        MovimientoCredito movimiento = MovimientoCredito.builder()
                .usuario(usuario)
                .ticket(ticket)
                .tipo("ABONO")
                .monto(monto)
                .descripcion(descripcion)
                .build();

        MovimientoCredito guardado = movimientoCreditoRepository.save(movimiento);
        return mapearResponse(guardado);
    }

    public MovimientoCreditoResponse usar(String correo, Long ticketId, BigDecimal monto, String descripcion) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        MovimientoCredito movimiento = MovimientoCredito.builder()
                .usuario(usuario)
                .ticket(ticket)
                .tipo("USO")
                .monto(monto)
                .descripcion(descripcion)
                .build();

        try {
            MovimientoCredito guardado = movimientoCreditoRepository.save(movimiento);
            return mapearResponse(guardado);
        } catch (Exception e) {
            throw new RuntimeException("Saldo de créditos insuficiente");
        }
    }

    private MovimientoCreditoResponse mapearResponse(MovimientoCredito movimiento) {
        return MovimientoCreditoResponse.builder()
                .id(movimiento.getId())
                .tipo(movimiento.getTipo())
                .monto(movimiento.getMonto())
                .descripcion(movimiento.getDescripcion())
                .saldoResultante(movimiento.getSaldoResultante())
                .fechaCreacion(movimiento.getFechaCreacion())
                .build();
    }
}