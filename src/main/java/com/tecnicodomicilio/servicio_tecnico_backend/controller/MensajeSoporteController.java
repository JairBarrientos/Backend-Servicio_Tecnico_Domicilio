package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.MensajeSoporteRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.MensajeSoporteResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.service.MensajeSoporteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/soporte/{soporteId}/mensajes")
@CrossOrigin(origins = "*")
public class MensajeSoporteController {

    private final MensajeSoporteService mensajeSoporteService;

    public MensajeSoporteController(MensajeSoporteService mensajeSoporteService) {
        this.mensajeSoporteService = mensajeSoporteService;
    }

    @GetMapping
    public List<MensajeSoporteResponse> listar(@PathVariable Long soporteId) {
        return mensajeSoporteService.listar(soporteId);
    }

    @PostMapping
    public ResponseEntity<?> enviar(@PathVariable Long soporteId, Authentication authentication,
                                     @RequestBody MensajeSoporteRequest request) {
        try {
            MensajeSoporteResponse response = mensajeSoporteService.enviar(soporteId, authentication.getName(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}