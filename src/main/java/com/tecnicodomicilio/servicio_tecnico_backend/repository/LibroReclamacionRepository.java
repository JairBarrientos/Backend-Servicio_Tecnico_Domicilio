package com.tecnicodomicilio.servicio_tecnico_backend.repository;

import com.tecnicodomicilio.servicio_tecnico_backend.model.LibroReclamacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LibroReclamacionRepository extends JpaRepository<LibroReclamacion, Long> {
    Optional<LibroReclamacion> findByCodigoSeguimiento(String codigoSeguimiento);
}