package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.AtencionResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.model.LibroReclamacion;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Soporte;
import com.tecnicodomicilio.servicio_tecnico_backend.model.Usuario;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.LibroReclamacionRepository;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.SoporteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AtencionService {

    private static final List<String> ESTADOS_RECLAMO = List.of("EN_REVISION", "RESUELTO", "RECHAZADO", "CERRADO");
    private static final List<String> ESTADOS_SOPORTE = List.of("EN_PROCESO", "RESUELTO", "CERRADO");

    private final LibroReclamacionRepository reclamacionRepository;
    private final SoporteRepository soporteRepository;

    public AtencionService(LibroReclamacionRepository reclamacionRepository, SoporteRepository soporteRepository) {
        this.reclamacionRepository = reclamacionRepository;
        this.soporteRepository = soporteRepository;
    }

    public List<AtencionResponse> listar(String tipoSolicitado, String filtro) {
        String tipo = tipoSolicitado != null ? tipoSolicitado.toUpperCase() : "";
        boolean soloPendientes = !"TODOS".equalsIgnoreCase(filtro);

        if ("RECLAMO".equals(tipo)) {
            List<LibroReclamacion> lista = soloPendientes
                    ? reclamacionRepository.findByEstadoInOrderByIdDesc(List.of("PENDIENTE", "EN_REVISION"))
                    : reclamacionRepository.findAllByOrderByIdDesc();
            return lista.stream().map(this::deReclamo).collect(Collectors.toList());
        }

        if ("SOPORTE".equals(tipo)) {
            List<Soporte> lista = soloPendientes
                    ? soporteRepository.findByEstadoInOrderByIdDesc(List.of("ABIERTO", "EN_PROCESO"))
                    : soporteRepository.findAllByOrderByIdDesc();
            return lista.stream().map(this::deSoporte).collect(Collectors.toList());
        }

        throw new RuntimeException("Tipo no válido. Usa RECLAMO o SOPORTE");
    }

    public AtencionResponse responder(String tipoSolicitado, Long id, String respuesta, String estado, String solucion) {
        String tipo = tipoSolicitado != null ? tipoSolicitado.toUpperCase() : "";

        String texto = respuesta != null ? respuesta.trim() : "";
        if (texto.isEmpty()) {
            throw new RuntimeException("Escribe la respuesta");
        }
        if (texto.length() > 2000) {
            throw new RuntimeException("La respuesta no puede pasar de 2000 caracteres");
        }

        if ("RECLAMO".equals(tipo)) {
            if (estado == null || !ESTADOS_RECLAMO.contains(estado)) {
                throw new RuntimeException("Estado no válido para un reclamo");
            }

            LibroReclamacion reclamo = reclamacionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reclamo no encontrado"));

            if ("CERRADO".equals(reclamo.getEstado())) {
                throw new RuntimeException("Este reclamo ya fue cerrado");
            }

            reclamo.setRespuesta(texto);
            reclamo.setEstado(estado);
            if (!"EN_REVISION".equals(estado)) {
                reclamo.setFechaResolucion(LocalDateTime.now());
            }

            return deReclamo(reclamacionRepository.save(reclamo));
        }

        if ("SOPORTE".equals(tipo)) {
            if (estado == null || !ESTADOS_SOPORTE.contains(estado)) {
                throw new RuntimeException("Estado no válido para una consulta de soporte");
            }

            String detalleSolucion = solucion != null ? solucion.trim() : "";
            if (detalleSolucion.length() > 2000) {
                throw new RuntimeException("La solución no puede pasar de 2000 caracteres");
            }

            Soporte consulta = soporteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Consulta no encontrada"));

            if ("CERRADO".equals(consulta.getEstado())) {
                throw new RuntimeException("Esta consulta ya fue cerrada");
            }

            consulta.setRespuesta(texto);
            if (!detalleSolucion.isEmpty()) {
                consulta.setSolucion(detalleSolucion);
            }
            consulta.setEstado(estado);
            consulta.setFechaActualizacion(LocalDateTime.now());
            if ("RESUELTO".equals(estado) || "CERRADO".equals(estado)) {
                consulta.setFechaCierre(LocalDateTime.now());
            }

            return deSoporte(soporteRepository.save(consulta));
        }

        throw new RuntimeException("Tipo no válido. Usa RECLAMO o SOPORTE");
    }

    private AtencionResponse deReclamo(LibroReclamacion reclamo) {
        boolean anonimo = Boolean.TRUE.equals(reclamo.getEsAnonimo());
        Usuario cliente = reclamo.getCliente();

        return AtencionResponse.builder()
                .tipoRegistro("RECLAMO")
                .id(reclamo.getId())
                .referencia(reclamo.getCodigoSeguimiento())
                .categoria(reclamo.getTipoReclamo())
                .titulo(reclamo.getTitulo())
                .descripcion(reclamo.getDescripcion())
                .prioridad(reclamo.getPrioridad())
                .estado(reclamo.getEstado())
                .fechaCreacion(reclamo.getFechaCreacion())
                .respuesta(reclamo.getRespuesta())
                .solicitante(anonimo ? "Anónimo" : nombre(cliente))
                .solicitanteCorreo(anonimo || cliente == null ? null : cliente.getCorreo())
                .ticketCodigo(reclamo.getTicket() != null ? reclamo.getTicket().getCodigoTicket() : null)
                .build();
    }

    private AtencionResponse deSoporte(Soporte consulta) {
        boolean anonimo = Boolean.TRUE.equals(consulta.getEsAnonimo());
        Usuario cliente = consulta.getCliente();

        return AtencionResponse.builder()
                .tipoRegistro("SOPORTE")
                .id(consulta.getId())
                .referencia("SOP-" + consulta.getId())
                .categoria(consulta.getTipoConsulta())
                .titulo(consulta.getAsunto())
                .descripcion(consulta.getDescripcion())
                .prioridad(consulta.getPrioridad())
                .estado(consulta.getEstado())
                .fechaCreacion(consulta.getFechaCreacion())
                .respuesta(consulta.getRespuesta())
                .solucion(consulta.getSolucion())
                .solicitante(anonimo ? "Anónimo" : nombre(cliente))
                .solicitanteCorreo(anonimo || cliente == null ? null : cliente.getCorreo())
                .build();
    }

    private String nombre(Usuario usuario) {
        if (usuario == null) return "Usuario eliminado";
        return (usuario.getNombres() + " " + usuario.getApellidos()).trim();
    }
}