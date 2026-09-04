package com.tecnicodomicilio.servicio_tecnico_backend.repository;

import com.tecnicodomicilio.servicio_tecnico_backend.model.MovimientoCredito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoCreditoRepository extends JpaRepository<MovimientoCredito, Long> {
    List<MovimientoCredito> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
}