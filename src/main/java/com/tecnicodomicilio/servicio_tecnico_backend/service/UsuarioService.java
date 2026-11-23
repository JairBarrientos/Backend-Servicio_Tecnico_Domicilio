package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.ActualizarPerfilRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.UsuarioResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Usuario;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AlmacenamientoService almacenamientoService;

    public UsuarioService(UsuarioRepository usuarioRepository, AlmacenamientoService almacenamientoService) {
        this.usuarioRepository = usuarioRepository;
        this.almacenamientoService = almacenamientoService;
    }

    public UsuarioResponse obtenerPerfil(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return mapearResponse(usuario);
    }

    public UsuarioResponse actualizarPerfil(String correo, ActualizarPerfilRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String nombres = request.getNombres() != null ? request.getNombres().trim() : "";
        String apellidos = request.getApellidos() != null ? request.getApellidos().trim() : "";
        String telefono = request.getTelefono() != null ? request.getTelefono().trim() : "";

        if (nombres.isEmpty() || apellidos.isEmpty()) {
            throw new RuntimeException("Los nombres y apellidos son obligatorios");
        }
        if (!telefono.matches("\\d{9}")) {
            throw new RuntimeException("El teléfono debe tener 9 dígitos");
        }

        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setTelefono(telefono);

        return mapearResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponse actualizarFoto(String correo, MultipartFile archivo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setFotoPerfil(almacenamientoService.guardarFotoPerfil(archivo));

        return mapearResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponse bloquear(Long id, String correoAdmin) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getCorreo().equals(correoAdmin)) {
            throw new RuntimeException("No puedes bloquear tu propia cuenta");
        }
        if ("ADMIN".equals(usuario.getRol())) {
            throw new RuntimeException("No se puede bloquear a otro administrador");
        }
        if ("BLOQUEADO".equals(usuario.getEstado())) {
            throw new RuntimeException("El usuario ya está bloqueado");
        }

        usuario.setEstado("BLOQUEADO");
        usuario.setFechaBloqueo(LocalDateTime.now());

        return mapearResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponse desbloquear(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuario.getEstado().equals("BLOQUEADO")) {
            throw new RuntimeException("El usuario no está bloqueado");
        }

        usuario.setEstado("ACTIVO");
        usuario.setIntentosRecuperacion(0);
        usuario.setFechaBloqueo(null);

        return mapearResponse(usuarioRepository.save(usuario));
    }

    private UsuarioResponse mapearResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .dni(usuario.getDni())
                .correo(usuario.getCorreo())
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol())
                .estado(usuario.getEstado())
                .verificado(usuario.getVerificado())
                .fotoPerfil(usuario.getFotoPerfil())
                .build();
    }
}