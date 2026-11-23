package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.service.HistorialTecnicoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tecnicos/historial")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('TECNICO')")
public class HistorialTecnicoController {

    private final HistorialTecnicoService historialTecnicoService;

    public HistorialTecnicoController(HistorialTecnicoService historialTecnicoService) {
        this.historialTecnicoService = historialTecnicoService;
    }

    @GetMapping
    public ResponseEntity<?> obtener(Authentication authentication,
                                      @RequestParam(defaultValue = "SEMANA") String rango) {
        try {
            return ResponseEntity.ok(historialTecnicoService.obtener(authentication.getName(), rango));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}