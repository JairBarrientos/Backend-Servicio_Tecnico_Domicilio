package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.model.Factura;
import com.tecnicodomicilio.servicio_tecnico_backend.service.FacturaPdfService;
import com.tecnicodomicilio.servicio_tecnico_backend.service.FacturaService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/facturas")
@CrossOrigin(origins = "*")
public class FacturaController {

    private final FacturaService facturaService;
    private final FacturaPdfService facturaPdfService;

    public FacturaController(FacturaService facturaService, FacturaPdfService facturaPdfService) {
        this.facturaService = facturaService;
        this.facturaPdfService = facturaPdfService;
    }

    @PostMapping("/generar/{ticketId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generar(@PathVariable Long ticketId) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(facturaService.generar(ticketId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<?> obtenerPorTicket(@PathVariable Long ticketId, Authentication authentication) {
        try {
            return ResponseEntity.ok(facturaService.obtenerPorTicket(ticketId, authentication.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> descargarPdf(@PathVariable Long id, Authentication authentication) {
        try {
            Factura factura = facturaService.obtenerAutorizada(id, authentication.getName());
            byte[] pdfBytes = facturaPdfService.generarPdf(factura);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=factura-" + factura.getNumeroFactura() + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generando el PDF");
        }
    }
}