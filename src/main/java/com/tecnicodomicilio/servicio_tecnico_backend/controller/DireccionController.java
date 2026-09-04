package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.DireccionRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.DireccionResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.service.DireccionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/direcciones")
@CrossOrigin(origins = "*")
public class DireccionController {

    private final DireccionService direccionService;

    public DireccionController(DireccionService direccionService) {
        this.direccionService = direccionService;
    }

    @GetMapping
    public ResponseEntity<List<DireccionResponse>> listar(Authentication authentication) {
        List<DireccionResponse> lista = direccionService.listar(authentication.getName());
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<?> agregar(Authentication authentication, @RequestBody DireccionRequest request) {
        try {
            DireccionResponse response = direccionService.agregar(authentication.getName(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}