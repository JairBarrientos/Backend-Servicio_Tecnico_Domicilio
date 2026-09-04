package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.DireccionRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.DireccionResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.model.DireccionUsuario;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Usuario;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.DireccionUsuarioRepository;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DireccionService {

    private final DireccionUsuarioRepository direccionRepository;
    private final UsuarioRepository usuarioRepository;

    public DireccionService(DireccionUsuarioRepository direccionRepository, UsuarioRepository usuarioRepository) {
        this.direccionRepository = direccionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<DireccionResponse> listar(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return direccionRepository.findByUsuarioId(usuario.getId()).stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    public DireccionResponse agregar(String correo, DireccionRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        DireccionUsuario direccion = DireccionUsuario.builder()
                .usuario(usuario)
                .nombreDireccion(request.getNombreDireccion())
                .direccion(request.getDireccion())
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .referencia(request.getReferencia())
                .esPrincipal(request.getEsPrincipal() != null ? request.getEsPrincipal() : false)
                .build();

        DireccionUsuario guardada = direccionRepository.save(direccion);
        return mapearResponse(guardada);
    }

    private DireccionResponse mapearResponse(DireccionUsuario direccion) {
        return DireccionResponse.builder()
                .id(direccion.getId())
                .nombreDireccion(direccion.getNombreDireccion())
                .direccion(direccion.getDireccion())
                .latitud(direccion.getLatitud())
                .longitud(direccion.getLongitud())
                .referencia(direccion.getReferencia())
                .esPrincipal(direccion.getEsPrincipal())
                .build();
    }
}