package com.tecnicodomicilio.servicio_tecnico_backend.repository;

import com.tecnicodomicilio.servicio_tecnico_backend.model.HistorialEstadoTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEstadoTicketRepository extends JpaRepository<HistorialEstadoTicket, Long> {
    List<HistorialEstadoTicket> findByTicketId(Long ticketId);
}