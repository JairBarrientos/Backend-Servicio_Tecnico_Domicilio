package com.tecnicodomicilio.servicio_tecnico_backend.repository;

import com.tecnicodomicilio.servicio_tecnico_backend.model.Soporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SoporteRepository extends JpaRepository<Soporte, Long> {
    List<Soporte> findByClienteId(Long clienteId);
}