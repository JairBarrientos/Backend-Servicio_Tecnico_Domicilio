package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.TecnicoRevisionResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.service.OnboardingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/admin/tecnicos")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTecnicosController {

    private final OnboardingService onboardingService;

    public AdminTecnicosController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    private ResponseEntity<?> ejecutar(Supplier<TecnicoRevisionResponse> accion) {
        try {
            return ResponseEntity.ok(accion.get());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(defaultValue = "EN_REVISION") String estado) {
        try {
            List<TecnicoRevisionResponse> lista = onboardingService.listarRevision(estado);
            return ResponseEntity.ok(lista);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detalle(@PathVariable Long id) {
        return ejecutar(() -> onboardingService.detalleRevision(id));
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable Long id) {
        return ejecutar(() -> onboardingService.aprobar(id));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ejecutar(() -> onboardingService.rechazar(id, body.get("motivo")));
    }

    @GetMapping("/{id}/documento/{tipo}")
    public ResponseEntity<?> documento(@PathVariable Long id, @PathVariable String tipo) {
        try {
            OnboardingService.Documento documento = onboardingService.documento(id, tipo);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(documento.tipoContenido()))
                    .body(documento.contenido());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}