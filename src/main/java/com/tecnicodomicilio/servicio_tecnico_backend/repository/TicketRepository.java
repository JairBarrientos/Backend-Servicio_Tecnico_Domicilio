package com.tecnicodomicilio.servicio_tecnico_backend.repository;

import com.tecnicodomicilio.servicio_tecnico_backend.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByClienteId(Long clienteId);
    List<Ticket> findByTecnicoAsignadoId(Long tecnicoId);
    List<Ticket> findByEstado(String estado);

    @Query(value = "SELECT fn_calcular_precio_ticket(:ticketId)", nativeQuery = true)
    BigDecimal calcularPrecioTicket(@Param("ticketId") Long ticketId);
}