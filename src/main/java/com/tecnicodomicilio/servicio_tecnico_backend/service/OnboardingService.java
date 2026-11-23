package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.OnboardingPerfilRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.OnboardingResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.TecnicoRevisionResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.model.TecnicoDetalle;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Usuario;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.TecnicoDetalleRepository;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OnboardingService {

    public record Documento(byte[] contenido, String tipoContenido, String nombre) {}

    private static final List<String> ESTADOS = List.of("INCOMPLETO", "EN_REVISION", "APROBADO", "RECHAZADO");

    private final TecnicoDetalleRepository detalleRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlmacenamientoService almacenamientoService;

    public OnboardingService(TecnicoDetalleRepository detalleRepository, UsuarioRepository usuarioRepository,
                              AlmacenamientoService almacenamientoService) {
        this.detalleRepository = detalleRepository;
        this.usuarioRepository = usuarioRepository;
        this.almacenamientoService = almacenamientoService;
    }

    private Usuario tecnicoPorCorreo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!"TECNICO".equals(usuario.getRol())) {
            throw new RuntimeException("Solo los técnicos pueden verificar su cuenta");
        }
        return usuario;
    }

    private TecnicoDetalle detalleDe(Usuario tecnico) {
        return detalleRepository.findByTecnicoId(tecnico.getId())
                .orElseGet(() -> detalleRepository.save(TecnicoDetalle.builder().tecnico(tecnico).build()));
    }

    private TecnicoDetalle detalleDeTecnicoId(Long tecnicoId) {
        return detalleRepository.findByTecnicoId(tecnicoId)
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));
    }

    private void exigirEditable(TecnicoDetalle detalle) {
        if ("EN_REVISION".equals(detalle.getEstadoVerificacion())) {
            throw new RuntimeException("Tu solicitud está en revisión. Espera la respuesta del equipo");
        }
        if ("APROBADO".equals(detalle.getEstadoVerificacion())) {
            throw new RuntimeException("Tu cuenta ya fue aprobada");
        }
    }

    public OnboardingResponse obtener(String correo) {
        Usuario tecnico = tecnicoPorCorreo(correo);
        return mapear(tecnico, detalleDe(tecnico));
    }

    public OnboardingResponse guardarPerfil(String correo, OnboardingPerfilRequest request) {
        Usuario tecnico = tecnicoPorCorreo(correo);
        TecnicoDetalle detalle = detalleDe(tecnico);
        exigirEditable(detalle);

        String especialidades = request.getEspecialidades() != null ? request.getEspecialidades().trim() : "";
        if (especialidades.isEmpty()) {
            throw new RuntimeException("Escribe tus especialidades");
        }
        if (especialidades.length() > 200) {
            throw new RuntimeException("Las especialidades no pueden pasar de 200 caracteres");
        }
        if (request.getAnosExperiencia() == null || request.getAnosExperiencia() < 0 || request.getAnosExperiencia() > 60) {
            throw new RuntimeException("Los años de experiencia deben estar entre 0 y 60");
        }
        if (request.getRadioCobertura() == null || request.getRadioCobertura() < 1 || request.getRadioCobertura() > 50) {
            throw new RuntimeException("El radio de cobertura debe estar entre 1 y 50 km");
        }
        if (request.getLatitud() == null || request.getLongitud() == null) {
            throw new RuntimeException("Marca tu zona de trabajo en el mapa");
        }

        detalle.setEspecialidades(especialidades);
        detalle.setAnosExperiencia(request.getAnosExperiencia());
        detalle.setRadioCobertura(request.getRadioCobertura());
        tecnico.setLatitud(request.getLatitud());
        tecnico.setLongitud(request.getLongitud());

        usuarioRepository.save(tecnico);
        detalleRepository.save(detalle);

        return mapear(tecnico, detalle);
    }

    public OnboardingResponse subirCv(String correo, MultipartFile archivo) {
        Usuario tecnico = tecnicoPorCorreo(correo);
        TecnicoDetalle detalle = detalleDe(tecnico);
        exigirEditable(detalle);

        String anterior = detalle.getCvUrl();
        detalle.setCvUrl(almacenamientoService.guardarDocumento(archivo, "cv", true));
        detalleRepository.save(detalle);
        almacenamientoService.eliminarDocumento(anterior);

        return mapear(tecnico, detalle);
    }

    public OnboardingResponse subirDni(String correo, MultipartFile archivo) {
        Usuario tecnico = tecnicoPorCorreo(correo);
        TecnicoDetalle detalle = detalleDe(tecnico);
        exigirEditable(detalle);

        String anterior = detalle.getDniUrl();
        detalle.setDniUrl(almacenamientoService.guardarDocumento(archivo, "dni", false));
        detalleRepository.save(detalle);
        almacenamientoService.eliminarDocumento(anterior);

        return mapear(tecnico, detalle);
    }

    public OnboardingResponse enviar(String correo) {
        Usuario tecnico = tecnicoPorCorreo(correo);
        TecnicoDetalle detalle = detalleDe(tecnico);
        exigirEditable(detalle);

        if (!Boolean.TRUE.equals(mapear(tecnico, detalle).getListoParaEnviar())) {
            throw new RuntimeException("Completa tu perfil, tu CV y la foto de tu DNI antes de enviar");
        }

        detalle.setEstadoVerificacion("EN_REVISION");
        detalle.setMotivoRechazo(null);
        detalle.setFechaEnvioRevision(LocalDateTime.now());
        detalleRepository.save(detalle);

        return mapear(tecnico, detalle);
    }

    public List<TecnicoRevisionResponse> listarRevision(String estado) {
        if (!ESTADOS.contains(estado)) {
            throw new RuntimeException("Estado no válido");
        }

        return detalleRepository.findByEstadoVerificacionOrderByFechaEnvioRevisionAsc(estado).stream()
                .map(this::mapearRevision)
                .collect(Collectors.toList());
    }

    public TecnicoRevisionResponse detalleRevision(Long tecnicoId) {
        return mapearRevision(detalleDeTecnicoId(tecnicoId));
    }

    public TecnicoRevisionResponse aprobar(Long tecnicoId) {
        TecnicoDetalle detalle = detalleDeTecnicoId(tecnicoId);

        if (!"EN_REVISION".equals(detalle.getEstadoVerificacion())) {
            throw new RuntimeException("Solo se pueden aprobar solicitudes en revisión");
        }

        detalle.setEstadoVerificacion("APROBADO");
        detalle.setVerificadoEmpresa(true);
        detalle.setMotivoRechazo(null);

        return mapearRevision(detalleRepository.save(detalle));
    }

    public TecnicoRevisionResponse rechazar(Long tecnicoId, String motivo) {
        TecnicoDetalle detalle = detalleDeTecnicoId(tecnicoId);

        if (!"EN_REVISION".equals(detalle.getEstadoVerificacion())) {
            throw new RuntimeException("Solo se pueden rechazar solicitudes en revisión");
        }

        String texto = motivo != null ? motivo.trim() : "";
        if (texto.isEmpty()) {
            throw new RuntimeException("Escribe el motivo del rechazo");
        }
        if (texto.length() > 200) {
            throw new RuntimeException("El motivo no puede pasar de 200 caracteres");
        }

        detalle.setEstadoVerificacion("RECHAZADO");
        detalle.setMotivoRechazo(texto);

        return mapearRevision(detalleRepository.save(detalle));
    }

    public Documento documento(Long tecnicoId, String tipo) {
        TecnicoDetalle detalle = detalleDeTecnicoId(tecnicoId);

        String nombre;
        if ("cv".equals(tipo)) {
            nombre = detalle.getCvUrl();
        } else if ("dni".equals(tipo)) {
            nombre = detalle.getDniUrl();
        } else {
            throw new RuntimeException("Tipo de documento no válido");
        }

        if (nombre == null) {
            throw new RuntimeException("El técnico no subió este documento");
        }

        return new Documento(almacenamientoService.leerDocumento(nombre),
                almacenamientoService.tipoContenido(nombre), nombre);
    }

    private OnboardingResponse mapear(Usuario tecnico, TecnicoDetalle detalle) {
        boolean perfilCompleto = detalle.getEspecialidades() != null && !detalle.getEspecialidades().isBlank()
                && tecnico.getLatitud() != null && tecnico.getLongitud() != null;
        boolean tieneCv = detalle.getCvUrl() != null;
        boolean tieneDni = detalle.getDniUrl() != null;

        return OnboardingResponse.builder()
                .estado(detalle.getEstadoVerificacion())
                .motivoRechazo(detalle.getMotivoRechazo())
                .especialidades(detalle.getEspecialidades())
                .anosExperiencia(detalle.getAnosExperiencia())
                .latitud(tecnico.getLatitud())
                .longitud(tecnico.getLongitud())
                .radioCobertura(detalle.getRadioCobertura())
                .perfilCompleto(perfilCompleto)
                .tieneCv(tieneCv)
                .tieneDni(tieneDni)
                .listoParaEnviar(perfilCompleto && tieneCv && tieneDni)
                .build();
    }

    private TecnicoRevisionResponse mapearRevision(TecnicoDetalle detalle) {
        Usuario tecnico = detalle.getTecnico();

        return TecnicoRevisionResponse.builder()
                .tecnicoId(tecnico.getId())
                .nombres(tecnico.getNombres())
                .apellidos(tecnico.getApellidos())
                .correo(tecnico.getCorreo())
                .telefono(tecnico.getTelefono())
                .dni(tecnico.getDni())
                .especialidades(detalle.getEspecialidades())
                .anosExperiencia(detalle.getAnosExperiencia())
                .radioCobertura(detalle.getRadioCobertura())
                .latitud(tecnico.getLatitud())
                .longitud(tecnico.getLongitud())
                .estado(detalle.getEstadoVerificacion())
                .motivoRechazo(detalle.getMotivoRechazo())
                .fechaEnvio(detalle.getFechaEnvioRevision())
                .tieneCv(detalle.getCvUrl() != null)
                .tieneDni(detalle.getDniUrl() != null)
                .build();
    }
}