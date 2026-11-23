package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.MensajeChatRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.service.MensajeChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets/{ticketId}/mensajes")
@CrossOrigin(origins = "*")
public class MensajeChatController {

    private final MensajeChatService mensajeChatService;

    public MensajeChatController(MensajeChatService mensajeChatService) {
        this.mensajeChatService = mensajeChatService;
    }

    @GetMapping
    public ResponseEntity<?> listar(@PathVariable Long ticketId, Authentication authentication) {
        try {
            return ResponseEntity.ok(mensajeChatService.listar(ticketId, authentication.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> enviar(@PathVariable Long ticketId, Authentication authentication,
                                     @RequestBody MensajeChatRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(mensajeChatService.enviar(ticketId, authentication.getName(), request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}