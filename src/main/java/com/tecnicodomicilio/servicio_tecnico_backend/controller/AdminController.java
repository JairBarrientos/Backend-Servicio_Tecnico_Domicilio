package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.AdminResumenResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.RendimientoTecnicoResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.UsuarioResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/resumen")
    public ResponseEntity<AdminResumenResponse> resumen() {
        return ResponseEntity.ok(adminService.resumen());
    }

    @GetMapping("/rendimiento-tecnicos")
    public ResponseEntity<List<RendimientoTecnicoResponse>> rendimiento() {
        return ResponseEntity.ok(adminService.rendimientoMensual());
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponse>> usuarios(@RequestParam String rol) {
        return ResponseEntity.ok(adminService.listarUsuarios(rol));
    }
}