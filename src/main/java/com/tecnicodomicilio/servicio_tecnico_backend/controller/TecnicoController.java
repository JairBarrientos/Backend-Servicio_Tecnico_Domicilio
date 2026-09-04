package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.TecnicoCercanoResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.service.TecnicoService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/tecnicos")
@CrossOrigin(origins = "*")
public class TecnicoController {

    private final TecnicoService tecnicoService;

    public TecnicoController(TecnicoService tecnicoService) {
        this.tecnicoService = tecnicoService;
    }

    @GetMapping("/cercanos")
    public List<TecnicoCercanoResponse> buscarCercanos(@RequestParam BigDecimal latitud,
                                                         @RequestParam BigDecimal longitud) {
        return tecnicoService.buscarCercanos(latitud, longitud);
    }
}