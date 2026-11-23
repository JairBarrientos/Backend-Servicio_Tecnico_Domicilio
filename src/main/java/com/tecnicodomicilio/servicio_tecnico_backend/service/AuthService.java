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
        validarRegistro(request);

        String correo = texto(request.getCorreo());
        String dni = texto(request.getDni());

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new RuntimeException("El correo ya está registrado");
        }
        if (usuarioRepository.existsByDni(dni)) {
            throw new RuntimeException("El DNI ya está registrado");
        }

        String codigo = String.format("%06d", new java.security.SecureRandom().nextInt(1_000_000));

        Usuario usuario = Usuario.builder()
                .nombres(texto(request.getNombres()))
                .apellidos(texto(request.getApellidos()))
                .dni(dni)
                .correo(correo)
                .password(passwordEncoder.encode(request.getPassword()))
                .telefono(texto(request.getTelefono()))
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

    public RegistroResponse reenviarVerificacion(RecuperarRequest request) {
        String correo = texto(request.getCorreo());

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Correo no encontrado"));

        if (Boolean.TRUE.equals(usuario.getVerificado())) {
            throw new RuntimeException("Tu cuenta ya está verificada. Inicia sesión");
        }

        if (usuario.getFechaCodigo() != null
                && usuario.getFechaCodigo().plusSeconds(60).isAfter(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Espera un minuto antes de pedir otro código");
        }

        String codigo = String.format("%06d", new java.security.SecureRandom().nextInt(1_000_000));

        usuario.setCodigoVerificacion(codigo);
        usuario.setFechaCodigo(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);

        emailService.enviarCodigoVerificacion(usuario.getCorreo(), codigo);

        return RegistroResponse.builder()
                .id(usuario.getId())
                .correo(usuario.getCorreo())
                .mensaje("Te enviamos un código nuevo")
                .build();
    }

    private void validarRegistro(RegistroRequest request) {
        if (!"CLIENTE".equals(request.getRol()) && !"TECNICO".equals(request.getRol())) {
            throw new RuntimeException("Tipo de cuenta no válido");
        }

        String nombres = texto(request.getNombres());
        String apellidos = texto(request.getApellidos());

        if (nombres.isEmpty() || apellidos.isEmpty()) {
            throw new RuntimeException("Escribe tus nombres y apellidos");
        }
        if (nombres.length() > 100 || apellidos.length() > 100) {
            throw new RuntimeException("Tus nombres o apellidos son demasiado largos");
        }
        if (!texto(request.getDni()).matches("\\d{8}")) {
            throw new RuntimeException("El DNI debe tener 8 dígitos");
        }
        if (!texto(request.getCorreo()).matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new RuntimeException("Escribe un correo válido");
        }
        if (!texto(request.getTelefono()).matches("\\d{9}")) {
            throw new RuntimeException("El teléfono debe tener 9 dígitos");
        }

        PasswordPolicy.validar(request.getPassword());
    }

    private String texto(String valor) {
        return valor != null ? valor.trim() : "";
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
        String correo = request.getCorreo() != null ? request.getCorreo().trim() : "";

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Correo no encontrado"));

        validarCuentaParaRecuperar(usuario);

        if (usuario.getFechaCodigo() != null
                && usuario.getFechaCodigo().plusSeconds(60).isAfter(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Espera un minuto antes de pedir otro código");
        }

        String codigo = String.format("%06d", new java.security.SecureRandom().nextInt(1_000_000));

        usuario.setCodigoVerificacion(codigo);
        usuario.setFechaCodigo(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);

        emailService.enviarCodigoRecuperacion(usuario.getCorreo(), codigo);

        return RegistroResponse.builder()
                .id(usuario.getId())
                .correo(usuario.getCorreo())
                .mensaje("Código de recuperación enviado a tu correo")
                .build();
    }

    public RegistroResponse restablecer(RestablecerRequest request) {
        String correo = request.getCorreo() != null ? request.getCorreo().trim() : "";

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Correo no encontrado"));

        validarCuentaParaRecuperar(usuario);
        PasswordPolicy.validar(request.getNuevaPassword());

        if (usuario.getCodigoVerificacion() == null || usuario.getFechaCodigo() == null) {
            throw new RuntimeException("Solicita un código de recuperación primero");
        }

        if (usuario.getFechaCodigo().plusMinutes(15).isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("El código expiró, solicita uno nuevo");
        }

        if (!usuario.getCodigoVerificacion().equals(request.getCodigo())) {
            int intentos = usuario.getIntentosRecuperacion() + 1;
            usuario.setIntentosRecuperacion(intentos);
            usuarioRepository.save(usuario);

            if (intentos >= 3) {
                throw new RuntimeException("Código incorrecto. Tu cuenta fue bloqueada por demasiados intentos. Contacta con soporte");
            }

            int quedan = 3 - intentos;
            throw new RuntimeException("Código incorrecto. Te quedan " + quedan
                    + (quedan == 1 ? " intento" : " intentos"));
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

    private void validarCuentaParaRecuperar(Usuario usuario) {
        if ("BLOQUEADO".equals(usuario.getEstado())) {
            throw new RuntimeException("Tu cuenta está bloqueada. Contacta con soporte");
        }
        if (!"ACTIVO".equals(usuario.getEstado())) {
            throw new RuntimeException("Tu cuenta aún no está verificada. Verifica tu correo primero");
        }
    }
}