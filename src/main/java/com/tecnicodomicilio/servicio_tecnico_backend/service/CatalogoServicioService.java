package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.ServicioResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.model.CatalogoServicio;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.CatalogoServicioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogoServicioService {

    private final CatalogoServicioRepository catalogoServicioRepository;

    public CatalogoServicioService(CatalogoServicioRepository catalogoServicioRepository) {
        this.catalogoServicioRepository = catalogoServicioRepository;
    }

    public List<ServicioResponse> listarActivos() {
        return catalogoServicioRepository.findByActivoTrue().stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    private ServicioResponse mapearResponse(CatalogoServicio servicio) {
        return ServicioResponse.builder()
                .id(servicio.getId())
                .categoria(servicio.getCategoria())
                .subcategoria(servicio.getSubcategoria())
                .nombreServicio(servicio.getNombreServicio())
                .descripcion(servicio.getDescripcion())
                .precioBase(servicio.getPrecioBase())
                .precioMaximo(servicio.getPrecioMaximo())
                .tiempoEstimado(servicio.getTiempoEstimado())
                .requierePiezas(servicio.getRequierePiezas())
                .build();
    }
}