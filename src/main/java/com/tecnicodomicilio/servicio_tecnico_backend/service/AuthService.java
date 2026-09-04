package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.config.JwtUtil;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.LoginRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.LoginResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.RecuperarRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.RegistroRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.RegistroResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.RestablecerRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.VerificarRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Usuario;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        if (!usuario.getEstado().equals("ACTIVO")) {
            throw new RuntimeException("Cuenta no activa, estado actual: " + usuario.getEstado());
        }

        String token = jwtUtil.generarToken(usuario.getId(), usuario.getCorreo(), usuario.getRol());

        return LoginResponse.builder()
                .token(token)
                .id(usuario.getId())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol())
                .build();
    }

    public RegistroResponse registrar(RegistroRequest request) {
    if (usuarioRepository.existsByCorreo(request.getCorreo())) {
        throw new RuntimeException("El correo ya está registrado");
    }
    if (usuarioRepository.existsByDni(request.getDni())) {
        throw new RuntimeException("El DNI ya está registrado");
    }

    String codigo = String.format("%06d", new java.util.Random().nextInt(999999));

    Usuario usuario = Usuario.builder()
            .nombres(request.getNombres())
            .apellidos(request.getApellidos())
            .dni(request.getDni())
            .correo(request.getCorreo())
            .password(passwordEncoder.encode(request.getPassword()))
            .telefono(request.getTelefono())
            .rol(request.getRol())
            .estado("PENDIENTE")
            .verificado(false)
            .codigoVerificacion(codigo)
            .fechaCodigo(java.time.LocalDateTime.now())
            .build();

    Usuario guardado = usuarioRepository.save(usuario);

    emailService.enviarCodigoVerificacion(guardado.getCorreo(), codigo);

    return RegistroResponse.builder()
            .id(guardado.getId())
            .correo(guardado.getCorreo())
            .mensaje("Usuario registrado. Revisa tu correo para el código de verificación.")
            .build();
    }

    public RegistroResponse verificar(VerificarRequest request) {
    Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    if (usuario.getVerificado()) {
        throw new RuntimeException("La cuenta ya está verificada");
    }

    if (usuario.getFechaCodigo().plusMinutes(15).isBefore(java.time.LocalDateTime.now())) {
        throw new RuntimeException("El código expiró, solicita uno nuevo");
    }

    if (!usuario.getCodigoVerificacion().equals(request.getCodigo())) {
        throw new RuntimeException("Código incorrecto");
    }

    usuario.setVerificado(true);
    usuario.setEstado("ACTIVO");
    usuario.setCodigoVerificacion(null);
    usuarioRepository.save(usuario);

    return RegistroResponse.builder()
            .id(usuario.getId())
            .correo(usuario.getCorreo())
            .mensaje("Cuenta verificada correctamente")
            .build();
    }

    public RegistroResponse recuperar(RecuperarRequest request) {
    Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
            .orElseThrow(() -> new RuntimeException("Correo no encontrado"));

    String codigo = String.format("%06d", new java.util.Random().nextInt(999999));

    usuario.setCodigoVerificacion(codigo);
    usuario.setFechaCodigo(java.time.LocalDateTime.now());
    usuarioRepository.save(usuario);

    emailService.enviarCodigoVerificacion(usuario.getCorreo(), codigo);

    return RegistroResponse.builder()
            .id(usuario.getId())
            .correo(usuario.getCorreo())
            .mensaje("Código de recuperación enviado a tu correo")
            .build();
}

public RegistroResponse restablecer(RestablecerRequest request) {
    Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
            .orElseThrow(() -> new RuntimeException("Correo no encontrado"));

    if (usuario.getFechaCodigo().plusMinutes(15).isBefore(java.time.LocalDateTime.now())) {
        throw new RuntimeException("El código expiró, solicita uno nuevo");
    }

    if (!usuario.getCodigoVerificacion().equals(request.getCodigo())) {
        usuario.setIntentosRecuperacion(usuario.getIntentosRecuperacion() + 1);
        usuarioRepository.save(usuario);
        throw new RuntimeException("Código incorrecto");
    }

    usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
    usuario.setCodigoVerificacion(null);
    usuario.setIntentosRecuperacion(0);
    usuarioRepository.save(usuario);

    return RegistroResponse.builder()
            .id(usuario.getId())
            .correo(usuario.getCorreo())
            .mensaje("Contraseña restablecida correctamente")
            .build();
    }
}