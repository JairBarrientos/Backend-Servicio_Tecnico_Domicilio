package com.tecnicodomicilio.servicio_tecnico_backend.repository;

import com.tecnicodomicilio.servicio_tecnico_backend.model.LogSistema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogSistemaRepository extends JpaRepository<LogSistema, Long> {
}