package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.MensajeSoporteRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.MensajeSoporteResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.model.MensajeSoporte;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Soporte;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Usuario;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.MensajeSoporteRepository;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.SoporteRepository;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MensajeSoporteService {

    private final MensajeSoporteRepository mensajeSoporteRepository;
    private final SoporteRepository soporteRepository;
    private final UsuarioRepository usuarioRepository;

    public MensajeSoporteService(MensajeSoporteRepository mensajeSoporteRepository,
                                  SoporteRepository soporteRepository,
                                  UsuarioRepository usuarioRepository) {
        this.mensajeSoporteRepository = mensajeSoporteRepository;
        this.soporteRepository = soporteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<MensajeSoporteResponse> listar(Long soporteId) {
        return mensajeSoporteRepository.findBySoporteIdOrderByFechaEnvioAsc(soporteId).stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    public MensajeSoporteResponse enviar(Long soporteId, String correoRemitente, MensajeSoporteRequest request) {
        Soporte soporte = soporteRepository.findById(soporteId)
                .orElseThrow(() -> new RuntimeException("Ticket de soporte no encontrado"));

        Usuario remitente = usuarioRepository.findByCorreo(correoRemitente)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        MensajeSoporte mensaje = MensajeSoporte.builder()
                .soporte(soporte)
                .remitente(remitente)
                .remitenteRol(remitente.getRol())
                .contenido(request.getContenido())
                .tipoMensaje(request.getTipoMensaje() != null ? request.getTipoMensaje() : "TEXTO")
                .archivoUrl(request.getArchivoUrl())
                .leido(false)
                .build();

        MensajeSoporte guardado = mensajeSoporteRepository.save(mensaje);
        return mapearResponse(guardado);
    }

    private MensajeSoporteResponse mapearResponse(MensajeSoporte mensaje) {
        return MensajeSoporteResponse.builder()
                .id(mensaje.getId())
                .remitenteId(mensaje.getRemitente().getId())
                .remitenteNombre(mensaje.getRemitente().getNombres() + " " + mensaje.getRemitente().getApellidos())
                .remitenteRol(mensaje.getRemitenteRol())
                .contenido(mensaje.getContenido())
                .tipoMensaje(mensaje.getTipoMensaje())
                .archivoUrl(mensaje.getArchivoUrl())
                .leido(mensaje.getLeido())
                .fechaEnvio(mensaje.getFechaEnvio())
                .build();
    }
}