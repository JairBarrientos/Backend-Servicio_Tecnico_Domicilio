package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.ActualizarPerfilRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.UsuarioResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> obtenerPerfil(Authentication authentication) {
        try {
            String correo = authentication.getName();
            UsuarioResponse response = usuarioService.obtenerPerfil(correo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/perfil")
    public ResponseEntity<?> actualizarPerfil(Authentication authentication, @RequestBody ActualizarPerfilRequest request) {
        try {
            String correo = authentication.getName();
            UsuarioResponse response = usuarioService.actualizarPerfil(correo, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/desbloquear")
    public ResponseEntity<?> desbloquear(@PathVariable Long id) {
        try {
            UsuarioResponse response = usuarioService.desbloquear(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/bloquear")
    public ResponseEntity<?> bloquear(@PathVariable Long id, Authentication authentication) {
        try {
            UsuarioResponse response = usuarioService.bloquear(id, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @org.springframework.web.bind.annotation.PostMapping(value = "/perfil/foto",
        consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirFoto(Authentication authentication,
                                        @org.springframework.web.bind.annotation.RequestParam("file")
                                        org.springframework.web.multipart.MultipartFile file) {
        try {
            return ResponseEntity.ok(usuarioService.actualizarFoto(authentication.getName(), file));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}