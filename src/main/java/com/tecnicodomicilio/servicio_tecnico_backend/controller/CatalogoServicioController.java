package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.ServicioResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.service.CatalogoServicioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@CrossOrigin(origins = "*")
public class CatalogoServicioController {

    private final CatalogoServicioService catalogoServicioService;

    public CatalogoServicioController(CatalogoServicioService catalogoServicioService) {
        this.catalogoServicioService = catalogoServicioService;
    }

    @GetMapping
    public List<ServicioResponse> listar() {
        return catalogoServicioService.listarActivos();
    }
}