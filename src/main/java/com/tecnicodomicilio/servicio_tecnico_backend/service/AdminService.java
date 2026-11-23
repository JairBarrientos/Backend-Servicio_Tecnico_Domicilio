package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.AdminResumenResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.RendimientoTecnicoResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.UsuarioResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.LibroReclamacionRepository;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.TicketRepository;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final TicketRepository ticketRepository;
    private final LibroReclamacionRepository reclamacionRepository;

    public AdminService(UsuarioRepository usuarioRepository, TicketRepository ticketRepository,
                         LibroReclamacionRepository reclamacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.ticketRepository = ticketRepository;
        this.reclamacionRepository = reclamacionRepository;
    }

    private LocalDateTime inicioDeMes() {
        return LocalDate.now().withDayOfMonth(1).atStartOfDay();
    }

    public AdminResumenResponse resumen() {
        LocalDateTime desde = inicioDeMes();

        long enProceso = ticketRepository.countByEstado("ASIGNADO")
                + ticketRepository.countByEstado("EN_CAMINO")
                + ticketRepository.countByEstado("EN_REPARACION");

        long pendientes = ticketRepository.countByEstado("PENDIENTE")
                + ticketRepository.countByEstado("PENDIENTE_ACEPTACION");

        return AdminResumenResponse.builder()
                .totalClientes(usuarioRepository.countByRol("CLIENTE"))
                .totalTecnicos(usuarioRepository.countByRol("TECNICO"))
                .ticketsPendientes(pendientes)
                .ticketsEnProceso(enProceso)
                .ticketsFinalizadosMes(ticketRepository.contarFinalizadosDesde(desde))
                .ingresosMes(ticketRepository.ingresosDesde(desde))
                .reclamosPendientes(reclamacionRepository.countByEstado("PENDIENTE"))
                .build();
    }

    public List<RendimientoTecnicoResponse> rendimientoMensual() {
        return ticketRepository.rendimientoTecnicos(inicioDeMes()).stream()
                .map(fila -> RendimientoTecnicoResponse.builder()
                        .tecnicoId(((Number) fila[0]).longValue())
                        .nombre(fila[1] + " " + fila[2])
                        .serviciosCompletados(((Number) fila[3]).longValue())
                        .ingresos((BigDecimal) fila[4])
                        .build())
                .collect(Collectors.toList());
    }

    public List<UsuarioResponse> listarUsuarios(String rol) {
        return usuarioRepository.findByRol(rol).stream()
                .map(u -> UsuarioResponse.builder()
                        .id(u.getId())
                        .nombres(u.getNombres())
                        .apellidos(u.getApellidos())
                        .dni(u.getDni())
                        .correo(u.getCorreo())
                        .telefono(u.getTelefono())
                        .rol(u.getRol())
                        .estado(u.getEstado())
                        .verificado(u.getVerificado())
                        .fotoPerfil(u.getFotoPerfil())
                        .build())
                .collect(Collectors.toList());
    }
}