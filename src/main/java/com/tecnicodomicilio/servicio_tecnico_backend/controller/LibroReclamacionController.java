package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.ReclamacionRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.ReclamacionResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.service.LibroReclamacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reclamaciones")
@CrossOrigin(origins = "*")
public class LibroReclamacionController {

    private final LibroReclamacionService libroReclamacionService;

    public LibroReclamacionController(LibroReclamacionService libroReclamacionService) {
        this.libroReclamacionService = libroReclamacionService;
    }

    @PostMapping
    public ResponseEntity<?> crear(Authentication authentication, @RequestBody ReclamacionRequest request) {
        try {
            ReclamacionResponse response = libroReclamacionService.crear(authentication.getName(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<?> consultarPorCodigo(@PathVariable String codigo) {
        try {
            ReclamacionResponse response = libroReclamacionService.consultarPorCodigo(codigo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}