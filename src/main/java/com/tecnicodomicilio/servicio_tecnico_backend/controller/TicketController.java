package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.CambiarEstadoRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.CotizacionRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.TicketDisponibleResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.TicketRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.TicketResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    private ResponseEntity<?> ejecutar(Supplier<TicketResponse> accion) {
        try {
            return ResponseEntity.ok(accion.get());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> crear(Authentication authentication, @RequestBody TicketRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ticketService.crear(authentication.getName(), request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> listar(Authentication authentication) {
        return ResponseEntity.ok(ticketService.listarPorCliente(authentication.getName()));
    }

    @GetMapping("/pendientes-tecnico")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<List<TicketResponse>> pendientesTecnico(Authentication authentication) {
        return ResponseEntity.ok(ticketService.listarPendientesTecnico(authentication.getName()));
    }

    @GetMapping("/mis-trabajos")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<List<TicketResponse>> misTrabajos(Authentication authentication,
                                                              @RequestParam(defaultValue = "false") boolean historial) {
        return ResponseEntity.ok(ticketService.listarTrabajos(authentication.getName(), historial));
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<List<TicketDisponibleResponse>> disponibles(
            Authentication authentication,
            @RequestParam(required = false) BigDecimal latitud,
            @RequestParam(required = false) BigDecimal longitud,
            @RequestParam(required = false) String categoria) {

        BigDecimal lat = latitud != null ? latitud : new BigDecimal("-12.0464");
        BigDecimal lon = longitud != null ? longitud : new BigDecimal("-77.0428");

        return ResponseEntity.ok(ticketService.listarDisponibles(lat, lon, categoria, authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detalle(@PathVariable Long id, Authentication authentication) {
        return ejecutar(() -> ticketService.obtenerDetalle(id, authentication.getName()));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, Authentication authentication,
                                            @RequestBody CambiarEstadoRequest request) {
        return ejecutar(() -> ticketService.cambiarEstado(id, authentication.getName(), request));
    }

    @PostMapping("/{id}/asignar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> asignar(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        return ejecutar(() -> ticketService.asignarTecnico(id, body.get("tecnicoId")));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> cancelar(@PathVariable Long id, Authentication authentication,
                                       @RequestBody Map<String, String> body) {
        return ejecutar(() -> ticketService.cancelar(id, authentication.getName(), body.get("motivo")));
    }

    @PostMapping("/{id}/calificar")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> calificar(@PathVariable Long id, Authentication authentication,
                                        @RequestBody Map<String, Object> body) {
        Object valor = body.get("calificacion");
        Integer calificacion = valor instanceof Number ? ((Number) valor).intValue() : null;
        String comentario = (String) body.get("comentario");

        return ejecutar(() -> ticketService.calificar(id, authentication.getName(), calificacion, comentario));
    }

    @PostMapping("/{id}/aceptar")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<?> aceptar(@PathVariable Long id, Authentication authentication) {
        return ejecutar(() -> ticketService.aceptar(id, authentication.getName()));
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<?> rechazar(@PathVariable Long id, Authentication authentication) {
        return ejecutar(() -> ticketService.rechazar(id, authentication.getName()));
    }

    @PostMapping("/{id}/contraoferta")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<?> contraofertar(@PathVariable Long id, Authentication authentication,
                                            @RequestBody Map<String, BigDecimal> body) {
        return ejecutar(() -> ticketService.contraofertar(id, authentication.getName(), body.get("precio")));
    }

    @PostMapping("/{id}/aceptar-contraoferta")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> aceptarContraoferta(@PathVariable Long id, Authentication authentication) {
        return ejecutar(() -> ticketService.aceptarContraoferta(id, authentication.getName()));
    }

    @PostMapping("/{id}/rechazar-contraoferta")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> rechazarContraoferta(@PathVariable Long id, Authentication authentication) {
        return ejecutar(() -> ticketService.rechazarContraoferta(id, authentication.getName()));
    }

    @PostMapping("/{id}/en-camino")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<?> enCamino(@PathVariable Long id, Authentication authentication) {
        return ejecutar(() -> ticketService.enCamino(id, authentication.getName()));
    }

    @PostMapping("/{id}/iniciar")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<?> iniciar(@PathVariable Long id, Authentication authentication) {
        return ejecutar(() -> ticketService.iniciar(id, authentication.getName()));
    }

    @PostMapping("/{id}/cotizar")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<?> cotizar(@PathVariable Long id, Authentication authentication,
                                      @RequestBody CotizacionRequest request) {
        return ejecutar(() -> ticketService.cotizar(id, authentication.getName(),
                request.getMonto(), request.getDetalle()));
    }

    @PostMapping("/{id}/aprobar-cotizacion")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> aprobarCotizacion(@PathVariable Long id, Authentication authentication) {
        return ejecutar(() -> ticketService.aprobarCotizacion(id, authentication.getName()));
    }

    @PostMapping("/{id}/rechazar-cotizacion")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> rechazarCotizacion(@PathVariable Long id, Authentication authentication) {
        return ejecutar(() -> ticketService.rechazarCotizacion(id, authentication.getName()));
    }

    @PostMapping("/{id}/programar-visita")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<?> programarVisita(@PathVariable Long id, Authentication authentication,
                                            @RequestBody Map<String, String> body) {
        return ejecutar(() -> ticketService.programarVisita(id, authentication.getName(),
                java.time.LocalDateTime.parse(body.get("fechaCita"))));
    }

    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<?> finalizar(@PathVariable Long id, Authentication authentication) {
        return ejecutar(() -> ticketService.finalizar(id, authentication.getName()));
    }

        @PostMapping("/{id}/cobrar-efectivo")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<?> cobrarEfectivo(@PathVariable Long id, Authentication authentication) {
        return ejecutar(() -> ticketService.cobrarEfectivo(id, authentication.getName()));
    }

    @PostMapping("/{id}/pagar-creditos")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> pagarCreditos(@PathVariable Long id, Authentication authentication) {
        return ejecutar(() -> ticketService.pagarConCreditos(id, authentication.getName()));
    }

    @PostMapping("/{id}/calificar-cliente")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<?> calificarCliente(@PathVariable Long id, Authentication authentication,
                                               @RequestBody Map<String, Object> body) {
        Object valor = body.get("calificacion");
        Integer calificacion = valor instanceof Number ? ((Number) valor).intValue() : null;
        String comentario = (String) body.get("comentario");

        return ejecutar(() -> ticketService.calificarCliente(id, authentication.getName(), calificacion, comentario));
    }
}