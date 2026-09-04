package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.CambiarEstadoRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.TicketRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.TicketResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<?> crear(Authentication authentication, @RequestBody TicketRequest request) {
        try {
            TicketResponse response = ticketService.crear(authentication.getName(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> listar(Authentication authentication) {
        List<TicketResponse> lista = ticketService.listarPorCliente(authentication.getName());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerDetalle(@PathVariable Long id) {
        try {
            TicketResponse response = ticketService.obtenerDetalle(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, Authentication authentication,
                                            @RequestBody CambiarEstadoRequest request) {
        try {
            TicketResponse response = ticketService.cambiarEstado(id, authentication.getName(), request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/asignar")
    public ResponseEntity<?> asignarTecnico(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            Long tecnicoId = body.get("tecnicoId");
            TicketResponse response = ticketService.asignarTecnico(id, tecnicoId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Long id, Authentication authentication,
                                       @RequestBody Map<String, String> body) {
        try {
            String motivo = body.get("motivo");
            TicketResponse response = ticketService.cancelar(id, authentication.getName(), motivo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/calificar")
    public ResponseEntity<?> calificar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Integer calificacion = (Integer) body.get("calificacion");
            String comentario = (String) body.get("comentario");
            TicketResponse response = ticketService.calificar(id, calificacion, comentario);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}