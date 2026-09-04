package com.tecnicodomicilio.servicio_tecnico_backend.repository;

import com.tecnicodomicilio.servicio_tecnico_backend.model.CatalogoServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogoServicioRepository extends JpaRepository<CatalogoServicio, Long> {
    List<CatalogoServicio> findByActivoTrue();
    List<CatalogoServicio> findByCategoria(String categoria);
}