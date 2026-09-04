package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.LoginRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.LoginResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.RecuperarRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.RegistroRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.RegistroResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.RestablecerRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.VerificarRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody RegistroRequest request) {
    try {
        RegistroResponse response = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
    }

    @PostMapping("/verificar")
    public ResponseEntity<?> verificar(@RequestBody VerificarRequest request) {
    try {
        RegistroResponse response = authService.verificar(request);
        return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
    }

    @PostMapping("/recuperar")
    public ResponseEntity<?> recuperar(@RequestBody RecuperarRequest request) {
        try {
            RegistroResponse response = authService.recuperar(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/restablecer")
    public ResponseEntity<?> restablecer(@RequestBody RestablecerRequest request) {
        try {
            RegistroResponse response = authService.restablecer(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}