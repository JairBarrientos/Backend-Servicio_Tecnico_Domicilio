package com.tecnicodomicilio.servicio_tecnico_backend.repository;

import com.tecnicodomicilio.servicio_tecnico_backend.model.TicketRepuesto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepuestoRepository extends JpaRepository<TicketRepuesto, Long> {
    List<TicketRepuesto> findByTicketId(Long ticketId);
}