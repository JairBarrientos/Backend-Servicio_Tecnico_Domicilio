package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.service.AtencionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/atencion")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAtencionController {

    private final AtencionService atencionService;

    public AdminAtencionController(AtencionService atencionService) {
        this.atencionService = atencionService;
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(defaultValue = "RECLAMO") String tipo,
                                     @RequestParam(defaultValue = "PENDIENTES") String filtro) {
        try {
            return ResponseEntity.ok(atencionService.listar(tipo, filtro));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{tipo}/{id}/responder")
    public ResponseEntity<?> responder(@PathVariable String tipo, @PathVariable Long id,
                                        @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(atencionService.responder(
                    tipo, id, body.get("respuesta"), body.get("estado"), body.get("solucion")));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}