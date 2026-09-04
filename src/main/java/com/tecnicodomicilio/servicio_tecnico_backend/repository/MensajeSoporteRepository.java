package com.tecnicodomicilio.servicio_tecnico_backend.repository;

import com.tecnicodomicilio.servicio_tecnico_backend.model.MensajeSoporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeSoporteRepository extends JpaRepository<MensajeSoporte, Long> {
    List<MensajeSoporte> findBySoporteIdOrderByFechaEnvioAsc(Long soporteId);
}