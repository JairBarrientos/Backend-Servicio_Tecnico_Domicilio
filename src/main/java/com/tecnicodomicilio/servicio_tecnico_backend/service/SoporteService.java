package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.SoporteRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.SoporteResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Soporte;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Usuario;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.SoporteRepository;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SoporteService {

    private final SoporteRepository soporteRepository;
    private final UsuarioRepository usuarioRepository;

    public SoporteService(SoporteRepository soporteRepository, UsuarioRepository usuarioRepository) {
        this.soporteRepository = soporteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public SoporteResponse crear(String correoCliente, SoporteRequest request) {
        Usuario cliente = usuarioRepository.findByCorreo(correoCliente)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Soporte soporte = Soporte.builder()
                .cliente(cliente)
                .tipoConsulta(request.getTipoConsulta())
                .asunto(request.getAsunto())
                .descripcion(request.getDescripcion())
                .prioridad(request.getPrioridad() != null ? request.getPrioridad() : "MEDIA")
                .estado("ABIERTO")
                .esAnonimo(request.getEsAnonimo() != null ? request.getEsAnonimo() : false)
                .build();

        Soporte guardado = soporteRepository.save(soporte);
        return mapearResponse(guardado);
    }

    public List<SoporteResponse> listarPorCliente(String correoCliente) {
        Usuario cliente = usuarioRepository.findByCorreo(correoCliente)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return soporteRepository.findByClienteId(cliente.getId()).stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    private SoporteResponse mapearResponse(Soporte soporte) {
        return SoporteResponse.builder()
                .id(soporte.getId())
                .tipoConsulta(soporte.getTipoConsulta())
                .asunto(soporte.getAsunto())
                .descripcion(soporte.getDescripcion())
                .prioridad(soporte.getPrioridad())
                .estado(soporte.getEstado())
                .fechaCreacion(soporte.getFechaCreacion())
                .respuesta(soporte.getRespuesta())
                .solucion(soporte.getSolucion())
                .build();
    }
}