package com.tecnicodomicilio.servicio_tecnico_backend.repository;

import com.tecnicodomicilio.servicio_tecnico_backend.model.DireccionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DireccionUsuarioRepository extends JpaRepository<DireccionUsuario, Long> {
    List<DireccionUsuario> findByUsuarioId(Long usuarioId);
}