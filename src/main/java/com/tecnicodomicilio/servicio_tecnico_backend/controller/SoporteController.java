package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.SoporteRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.SoporteResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.service.SoporteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/soporte")
@CrossOrigin(origins = "*")
public class SoporteController {

    private final SoporteService soporteService;

    public SoporteController(SoporteService soporteService) {
        this.soporteService = soporteService;
    }

    @PostMapping
    public ResponseEntity<?> crear(Authentication authentication, @RequestBody SoporteRequest request) {
        try {
            SoporteResponse response = soporteService.crear(authentication.getName(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<SoporteResponse>> listar(Authentication authentication) {
        List<SoporteResponse> lista = soporteService.listarPorCliente(authentication.getName());
        return ResponseEntity.ok(lista);
    }
}