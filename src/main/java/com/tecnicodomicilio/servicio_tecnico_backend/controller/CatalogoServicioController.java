package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.ServicioRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.ServicioResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.service.AlmacenamientoService;
import com.tecnicodomicilio.servicio_tecnico_backend.service.CatalogoServicioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/servicios")
@CrossOrigin(origins = "*")
public class CatalogoServicioController {

    private final CatalogoServicioService catalogoServicioService;
    private final AlmacenamientoService almacenamientoService;

    public CatalogoServicioController(CatalogoServicioService catalogoServicioService,
                                       AlmacenamientoService almacenamientoService) {
        this.catalogoServicioService = catalogoServicioService;
        this.almacenamientoService = almacenamientoService;
    }

    @GetMapping
    public List<ServicioResponse> listar() {
        return catalogoServicioService.listarActivos();
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ServicioResponse> listarTodos() {
        return catalogoServicioService.listarTodos();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(@RequestBody ServicioRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(catalogoServicioService.crear(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody ServicioRequest request) {
        try {
            return ResponseEntity.ok(catalogoServicioService.actualizar(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        try {
            return ResponseEntity.ok(catalogoServicioService.cambiarEstado(id, activo));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping(value = "/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> subirImagen(@RequestParam("file") MultipartFile file) {
        try {
            String url = almacenamientoService.guardarImagenServicio(file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}