package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.ServicioRequest;
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

    public List<ServicioResponse> listarTodos() {
        return catalogoServicioRepository.findAll().stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    public ServicioResponse crear(ServicioRequest request) {
        validar(request);

        String nombre = request.getNombreServicio().trim();
        if (catalogoServicioRepository.existsByNombreServicio(nombre)) {
            throw new RuntimeException("Ya existe un servicio con ese nombre");
        }

        CatalogoServicio servicio = CatalogoServicio.builder()
                .categoria(request.getCategoria().trim())
                .subcategoria(request.getSubcategoria())
                .nombreServicio(nombre)
                .descripcion(request.getDescripcion())
                .precioBase(request.getPrecioBase())
                .precioMaximo(request.getPrecioMaximo())
                .tiempoEstimado(request.getTiempoEstimado())
                .requierePiezas(request.getRequierePiezas() != null ? request.getRequierePiezas() : true)
                .imagenUrl(request.getImagenUrl())
                .activo(true)
                .build();

        return mapearResponse(catalogoServicioRepository.save(servicio));
    }

    public ServicioResponse actualizar(Long id, ServicioRequest request) {
        validar(request);

        CatalogoServicio servicio = catalogoServicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        String nombre = request.getNombreServicio().trim();
        if (!nombre.equals(servicio.getNombreServicio())
                && catalogoServicioRepository.existsByNombreServicio(nombre)) {
            throw new RuntimeException("Ya existe un servicio con ese nombre");
        }

        servicio.setCategoria(request.getCategoria().trim());
        servicio.setSubcategoria(request.getSubcategoria());
        servicio.setNombreServicio(nombre);
        servicio.setDescripcion(request.getDescripcion());
        servicio.setPrecioBase(request.getPrecioBase());
        servicio.setPrecioMaximo(request.getPrecioMaximo());
        servicio.setTiempoEstimado(request.getTiempoEstimado());
        if (request.getRequierePiezas() != null) {
            servicio.setRequierePiezas(request.getRequierePiezas());
        }
        if (request.getImagenUrl() != null) {
            servicio.setImagenUrl(request.getImagenUrl());
        }

        return mapearResponse(catalogoServicioRepository.save(servicio));
    }

    public ServicioResponse cambiarEstado(Long id, boolean activo) {
        CatalogoServicio servicio = catalogoServicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        servicio.setActivo(activo);
        return mapearResponse(catalogoServicioRepository.save(servicio));
    }

    private void validar(ServicioRequest request) {
        if (request.getNombreServicio() == null || request.getNombreServicio().isBlank()) {
            throw new RuntimeException("El nombre es obligatorio");
        }
        if (request.getCategoria() == null || request.getCategoria().isBlank()) {
            throw new RuntimeException("La categoría es obligatoria");
        }
        if (request.getPrecioBase() == null || request.getPrecioBase().signum() <= 0) {
            throw new RuntimeException("El precio base debe ser mayor a 0");
        }
        if (request.getPrecioMaximo() != null && request.getPrecioMaximo().compareTo(request.getPrecioBase()) < 0) {
            throw new RuntimeException("El precio máximo no puede ser menor al precio base");
        }
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
                .imagenUrl(servicio.getImagenUrl())
                .activo(servicio.getActivo())
                .build();
    }
}