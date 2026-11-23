package com.tecnicodomicilio.servicio_tecnico_backend.controller;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.OnboardingPerfilRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.OnboardingResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.service.OnboardingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.function.Supplier;

@RestController
@RequestMapping("/api/tecnicos/onboarding")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('TECNICO')")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    private ResponseEntity<?> ejecutar(Supplier<OnboardingResponse> accion) {
        try {
            return ResponseEntity.ok(accion.get());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> obtener(Authentication authentication) {
        return ejecutar(() -> onboardingService.obtener(authentication.getName()));
    }

    @PutMapping("/perfil")
    public ResponseEntity<?> guardarPerfil(Authentication authentication,
                                            @RequestBody OnboardingPerfilRequest request) {
        return ejecutar(() -> onboardingService.guardarPerfil(authentication.getName(), request));
    }

    @PostMapping(value = "/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirCv(Authentication authentication, @RequestParam("file") MultipartFile file) {
        return ejecutar(() -> onboardingService.subirCv(authentication.getName(), file));
    }

    @PostMapping(value = "/dni", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirDni(Authentication authentication, @RequestParam("file") MultipartFile file) {
        return ejecutar(() -> onboardingService.subirDni(authentication.getName(), file));
    }

    @PostMapping("/enviar")
    public ResponseEntity<?> enviar(Authentication authentication) {
        return ejecutar(() -> onboardingService.enviar(authentication.getName()));
    }
}