package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.MovimientoCreditoResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.SaldoCreditosResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.service.CreditoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/creditos")
@CrossOrigin(origins = "*")
public class CreditoController {

    private final CreditoService creditoService;

    public CreditoController(CreditoService creditoService) {
        this.creditoService = creditoService;
    }

    @GetMapping("/saldo")
    public ResponseEntity<?> consultarSaldo(Authentication authentication) {
        try {
            SaldoCreditosResponse response = creditoService.consultarSaldo(authentication.getName());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/movimientos")
    public ResponseEntity<List<MovimientoCreditoResponse>> listarMovimientos(Authentication authentication) {
        return ResponseEntity.ok(creditoService.listarMovimientos(authentication.getName()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/abonar")
    public ResponseEntity<?> abonar(@RequestBody Map<String, Object> body) {
        try {
            Long usuarioId = Long.valueOf(body.get("usuarioId").toString());
            Long ticketId = body.get("ticketId") != null ? Long.valueOf(body.get("ticketId").toString()) : null;
            BigDecimal monto = new BigDecimal(body.get("monto").toString());
            String descripcion = (String) body.get("descripcion");

            MovimientoCreditoResponse response = creditoService.abonar(usuarioId, ticketId, monto, descripcion);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/usar")
    public ResponseEntity<?> usar(Authentication authentication, @RequestBody Map<String, Object> body) {
        try {
            Long ticketId = Long.valueOf(body.get("ticketId").toString());
            BigDecimal monto = new BigDecimal(body.get("monto").toString());
            String descripcion = (String) body.get("descripcion");

            MovimientoCreditoResponse response = creditoService.usar(authentication.getName(), ticketId, monto, descripcion);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}