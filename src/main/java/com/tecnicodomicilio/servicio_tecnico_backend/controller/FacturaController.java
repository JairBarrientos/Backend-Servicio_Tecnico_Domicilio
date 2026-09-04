package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.FacturaResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Factura;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.FacturaRepository;
import com.tecnicodomicilio.servicio_tecnico_backend.service.FacturaPdfService;
import com.tecnicodomicilio.servicio_tecnico_backend.service.FacturaService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/facturas")
@CrossOrigin(origins = "*")
public class FacturaController {

    private final FacturaRepository facturaRepository;
    private final FacturaPdfService facturaPdfService;
    private final FacturaService facturaService;

    public FacturaController(FacturaRepository facturaRepository, FacturaPdfService facturaPdfService,
                              FacturaService facturaService) {
        this.facturaRepository = facturaRepository;
        this.facturaPdfService = facturaPdfService;
        this.facturaService = facturaService;
    }

    @PostMapping("/generar/{ticketId}")
    public ResponseEntity<?> generar(@PathVariable Long ticketId) {
        try {
            FacturaResponse response = facturaService.generar(ticketId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<?> obtenerPorTicket(@PathVariable Long ticketId) {
        try {
            FacturaResponse response = facturaService.obtenerPorTicket(ticketId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> descargarPdf(@PathVariable Long id) {
        try {
            Factura factura = facturaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

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