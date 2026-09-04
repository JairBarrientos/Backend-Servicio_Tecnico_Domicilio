package com.tecnicodomicilio.servicio_tecnico_backend.repository;

import com.tecnicodomicilio.servicio_tecnico_backend.model.ConfiguracionGeneral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracionGeneralRepository extends JpaRepository<ConfiguracionGeneral, Long> {
    Optional<ConfiguracionGeneral> findByClave(String clave);
}