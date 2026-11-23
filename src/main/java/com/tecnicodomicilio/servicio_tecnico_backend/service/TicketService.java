package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.CambiarEstadoRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.TicketDisponibleResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.TicketRequest;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.TicketResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.model.*;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;
    private final CatalogoServicioRepository catalogoServicioRepository;
    private final DireccionUsuarioRepository direccionUsuarioRepository;
    private final TecnicoDetalleRepository tecnicoDetalleRepository;
    private final FacturaRepository facturaRepository;
    private final FacturaService facturaService;
    private final CreditoService creditoService;

    public TicketService(TicketRepository ticketRepository, UsuarioRepository usuarioRepository,
                          CatalogoServicioRepository catalogoServicioRepository,
                          DireccionUsuarioRepository direccionUsuarioRepository,
                          TecnicoDetalleRepository tecnicoDetalleRepository,
                          FacturaRepository facturaRepository,
                          FacturaService facturaService,
                          CreditoService creditoService) {
        this.ticketRepository = ticketRepository;
        this.usuarioRepository = usuarioRepository;
        this.catalogoServicioRepository = catalogoServicioRepository;
        this.direccionUsuarioRepository = direccionUsuarioRepository;
        this.tecnicoDetalleRepository = tecnicoDetalleRepository;
        this.facturaRepository = facturaRepository;
        this.facturaService = facturaService;
        this.creditoService = creditoService;
    }

    private Usuario usuarioPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private Ticket ticketPorId(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
    }

    private boolean esCliente(Ticket ticket, Usuario usuario) {
        return ticket.getCliente().getId().equals(usuario.getId());
    }

    private boolean esTecnico(Ticket ticket, Usuario usuario) {
        return ticket.getTecnicoAsignado() != null
                && ticket.getTecnicoAsignado().getId().equals(usuario.getId());
    }

    private void exigirCliente(Ticket ticket, Usuario usuario) {
        if (!esCliente(ticket, usuario)) {
            throw new RuntimeException("No tienes permiso sobre este ticket");
        }
    }

    private void exigirTecnico(Ticket ticket, Usuario usuario) {
        if (!esTecnico(ticket, usuario)) {
            throw new RuntimeException("Este ticket no está asignado a ti");
        }
    }

    private void exigirEstado(Ticket ticket, String... permitidos) {
        for (String estado : permitidos) {
            if (estado.equals(ticket.getEstado())) return;
        }
        throw new RuntimeException("Acción no permitida: el ticket está en estado " + ticket.getEstado());
    }

    private void exigirDisponibleParaTecnico(Ticket ticket, Usuario tecnico) {
        if (ticket.getTecnicoAsignado() != null && !esTecnico(ticket, tecnico)) {
            throw new RuntimeException("Este ticket está reservado para otro técnico");
        }
    }

        private void exigirTecnicoAprobado(Usuario tecnico) {
        boolean aprobado = tecnicoDetalleRepository.findByTecnicoId(tecnico.getId())
                .map(detalle -> "APROBADO".equals(detalle.getEstadoVerificacion()))
                .orElse(false);

        if (!aprobado) {
            throw new RuntimeException("Tu cuenta de técnico aún no está aprobada");
        }
    }

    private void exigirPendienteDeCobro(Ticket ticket) {
        exigirEstado(ticket, "FINALIZADO");
        if (Boolean.TRUE.equals(ticket.getPagado())) {
            throw new RuntimeException("Este servicio ya fue pagado");
        }
        if (ticket.getPrecioFinal() == null || ticket.getPrecioFinal().signum() <= 0) {
            throw new RuntimeException("El ticket no tiene un total para cobrar");
        }
    }

    private void marcarPagado(Ticket ticket, String metodo) {
        ticket.setPagado(true);
        ticket.setMetodoPago(metodo);
        ticket.setFechaPago(LocalDateTime.now());
    }

    private void validarCalificacion(Integer calificacion) {
        if (calificacion == null || calificacion < 1 || calificacion > 5) {
            throw new RuntimeException("La calificación debe ser de 1 a 5");
        }
    }

    private BigDecimal precioAcordado(Ticket ticket) {
        if (ticket.getPrecioOfertado() != null) return ticket.getPrecioOfertado();
        return ticket.getPrecioVisita() != null ? ticket.getPrecioVisita() : BigDecimal.ZERO;
    }

    public TicketResponse crear(String correoCliente, TicketRequest request) {
        Usuario cliente = usuarioPorCorreo(correoCliente);

        CatalogoServicio servicio = null;
        if (request.getServicioId() != null) {
            servicio = catalogoServicioRepository.findById(request.getServicioId())
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        }

        BigDecimal precioOfertado = request.getPrecioOfertado();
        if (servicio != null) {
            if (precioOfertado == null) {
                precioOfertado = servicio.getPrecioBase();
            } else if (precioOfertado.compareTo(servicio.getPrecioBase()) < 0) {
                throw new RuntimeException("La oferta no puede ser menor al precio base (S/. " + servicio.getPrecioBase() + ")");
            }
        }

        DireccionUsuario direccion = null;
        if (request.getDireccionId() != null) {
            direccion = direccionUsuarioRepository.findById(request.getDireccionId())
                    .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));
        }

        Ticket ticket = Ticket.builder()
                .cliente(cliente)
                .servicio(servicio)
                .direccion(direccion)
                .direccionPersonalizada(request.getDireccionPersonalizada())
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .tipoDispositivo(request.getTipoDispositivo())
                .marca(request.getMarca())
                .modelo(request.getModelo())
                .urgencia(request.getUrgencia() != null ? request.getUrgencia() : "MEDIA")
                .precioOfertado(precioOfertado)
                .estado("PENDIENTE")
                .build();

        if (request.getLatitud() != null && request.getLongitud() != null) {
            tecnicoDetalleRepository.buscarTecnicoMasCercanoDisponible(
                    request.getLatitud(), request.getLongitud(), LocalDateTime.now()
            ).ifPresent(tecnicoId -> {
                Usuario tecnico = usuarioRepository.findById(tecnicoId).orElse(null);
                if (tecnico != null) {
                    ticket.setTecnicoAsignado(tecnico);
                    ticket.setEstado("PENDIENTE_ACEPTACION");
                }
            });
        }

        return mapearResponse(ticketRepository.save(ticket));
    }

    public List<TicketResponse> listarPorCliente(String correoCliente) {
        Usuario cliente = usuarioPorCorreo(correoCliente);

        return ticketRepository.findByClienteId(cliente.getId()).stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    public TicketResponse obtenerDetalle(Long ticketId, String correo) {
        Ticket ticket = ticketPorId(ticketId);
        Usuario usuario = usuarioPorCorreo(correo);

        boolean admin = "ADMIN".equals(usuario.getRol());
        boolean abiertoParaTecnicos = "TECNICO".equals(usuario.getRol())
                && ticket.getTecnicoAsignado() == null
                && "PENDIENTE".equals(ticket.getEstado());

        if (!admin && !esCliente(ticket, usuario) && !esTecnico(ticket, usuario) && !abiertoParaTecnicos) {
            throw new RuntimeException("No tienes permiso para ver este ticket");
        }

        return mapearResponse(ticket);
    }

    public TicketResponse cambiarEstado(Long ticketId, String correoUsuario, CambiarEstadoRequest request) {
        Ticket ticket = ticketPorId(ticketId);
        usuarioPorCorreo(correoUsuario);

        ticket.setEstado(request.getNuevoEstado());
        return mapearResponse(ticketRepository.save(ticket));
    }

    public TicketResponse asignarTecnico(Long ticketId, Long tecnicoId) {
        Ticket ticket = ticketPorId(ticketId);

        Usuario tecnico = usuarioRepository.findById(tecnicoId)
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));

        ticket.setTecnicoAsignado(tecnico);
        ticket.setEstado("ASIGNADO");

        return mapearResponse(ticketRepository.save(ticket));
    }

    public TicketResponse cancelar(Long ticketId, String correo, String motivo) {
        Ticket ticket = ticketPorId(ticketId);
        Usuario cliente = usuarioPorCorreo(correo);

        exigirCliente(ticket, cliente);
        exigirEstado(ticket, "PENDIENTE", "PENDIENTE_ACEPTACION", "ASIGNADO", "EN_CAMINO");

        String texto = motivo != null && motivo.length() > 200 ? motivo.substring(0, 200) : motivo;
        ticket.setEstado("CANCELADO");
        ticket.setMensajeCancelacion(texto);

        return mapearResponse(ticketRepository.save(ticket));
    }

    public TicketResponse calificar(Long ticketId, String correo, Integer calificacion, String comentario) {
        Ticket ticket = ticketPorId(ticketId);
        Usuario cliente = usuarioPorCorreo(correo);

        exigirCliente(ticket, cliente);
        exigirEstado(ticket, "FINALIZADO");
        validarCalificacion(calificacion);

        if (ticket.getCalificacionCliente() != null) {
            throw new RuntimeException("Este servicio ya fue calificado");
        }

        ticket.setCalificacionCliente(calificacion);
        ticket.setComentarioCliente(comentario);

        return mapearResponse(ticketRepository.save(ticket));
    }

    public TicketResponse calificarCliente(Long ticketId, String correo, Integer calificacion, String comentario) {
        Ticket ticket = ticketPorId(ticketId);
        Usuario tecnico = usuarioPorCorreo(correo);

        exigirTecnico(ticket, tecnico);
        exigirEstado(ticket, "FINALIZADO");
        validarCalificacion(calificacion);

        if (ticket.getCalificacionTecnico() != null) {
            throw new RuntimeException("Ya calificaste a este cliente");
        }

        ticket.setCalificacionTecnico(calificacion);
        ticket.setComentarioTecnico(comentario);

        return mapearResponse(ticketRepository.save(ticket));
    }

    public List<TicketResponse> listarPendientesTecnico(String correoTecnico) {
        Usuario tecnico = usuarioPorCorreo(correoTecnico);

        return ticketRepository.findByTecnicoAsignadoId(tecnico.getId()).stream()
                .filter(t -> "PENDIENTE_ACEPTACION".equals(t.getEstado()) && t.getPrecioContraoferta() == null)
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

        public List<TicketDisponibleResponse> listarDisponibles(BigDecimal latTecnico, BigDecimal lonTecnico,
                                                             String categoria, String correoTecnico) {
        Usuario tecnico = usuarioPorCorreo(correoTecnico);
        exigirTecnicoAprobado(tecnico);

        List<Object[]> filas = ticketRepository.buscarTicketsDisponibles(
                latTecnico, lonTecnico, categoria, tecnico.getId());
        List<TicketDisponibleResponse> respuesta = new ArrayList<>();

        for (Object[] fila : filas) {
            Long clienteId = ((Number) fila[13]).longValue();
            Double promedio = ticketRepository.promedioCalificacionCliente(clienteId);
            Double puntaje = promedio != null ? Math.round(promedio * 10.0) / 10.0 : null;

            respuesta.add(TicketDisponibleResponse.builder()
                    .id(((Number) fila[0]).longValue())
                    .codigoTicket((String) fila[1])
                    .titulo((String) fila[2])
                    .descripcion((String) fila[3])
                    .tipoDispositivo((String) fila[4])
                    .categoriaServicio((String) fila[5])
                    .precioOfertado((BigDecimal) fila[6])
                    .latitud((BigDecimal) fila[7])
                    .longitud((BigDecimal) fila[8])
                    .direccionPersonalizada((String) fila[9])
                    .clienteNombre(fila[10] + " " + fila[11])
                    .distanciaKm(((Number) fila[12]).doubleValue())
                    .clientePuntaje(puntaje)
                    .build());
        }

        return respuesta;
    }

    public List<TicketResponse> listarTrabajos(String correoTecnico, boolean historial) {
        Usuario tecnico = usuarioPorCorreo(correoTecnico);

        List<String> estados = historial
                ? List.of("FINALIZADO")
                : List.of("ASIGNADO", "EN_CAMINO", "EN_REPARACION", "COTIZACION_PENDIENTE", "FINALIZADO");

        return ticketRepository.findByTecnicoAsignadoIdAndEstadoIn(tecnico.getId(), estados).stream()
                .filter(t -> historial
                        || !"FINALIZADO".equals(t.getEstado())
                        || !Boolean.TRUE.equals(t.getPagado()))
                .sorted(Comparator.comparing(Ticket::getFechaCreacion,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

        public TicketResponse aceptar(Long ticketId, String correo) {
        Ticket ticket = ticketPorId(ticketId);
        Usuario tecnico = usuarioPorCorreo(correo);

        exigirTecnicoAprobado(tecnico);
        exigirEstado(ticket, "PENDIENTE", "PENDIENTE_ACEPTACION");
        exigirDisponibleParaTecnico(ticket, tecnico);

        ticket.setTecnicoAsignado(tecnico);
        ticket.setPrecioContraoferta(null);
        ticket.setEstado("ASIGNADO");

        return mapearResponse(ticketRepository.save(ticket));
    }

    public TicketResponse rechazar(Long ticketId, String correo) {
        Ticket ticket = ticketPorId(ticketId);
        Usuario tecnico = usuarioPorCorreo(correo);

        if (esTecnico(ticket, tecnico) && "PENDIENTE_ACEPTACION".equals(ticket.getEstado())) {
            ticket.setTecnicoAsignado(null);
            ticket.setPrecioContraoferta(null);
            ticket.setEstado("PENDIENTE");
            ticket = ticketRepository.save(ticket);
        }

        return mapearResponse(ticket);
    }

        public TicketResponse contraofertar(Long ticketId, String correo, BigDecimal nuevoPrecio) {
        Ticket ticket = ticketPorId(ticketId);
        Usuario tecnico = usuarioPorCorreo(correo);

        exigirTecnicoAprobado(tecnico);
        if (nuevoPrecio == null || nuevoPrecio.signum() <= 0) {
            throw new RuntimeException("Ingresa un precio válido");
        }
        exigirEstado(ticket, "PENDIENTE", "PENDIENTE_ACEPTACION");
        exigirDisponibleParaTecnico(ticket, tecnico);

        ticket.setTecnicoAsignado(tecnico);
        ticket.setPrecioContraoferta(nuevoPrecio);
        ticket.setEstado("PENDIENTE_ACEPTACION");

        return mapearResponse(ticketRepository.save(ticket));
    }

    public TicketResponse aceptarContraoferta(Long ticketId, String correo) {
        Ticket ticket = ticketPorId(ticketId);
        Usuario cliente = usuarioPorCorreo(correo);

        exigirCliente(ticket, cliente);
        exigirEstado(ticket, "PENDIENTE_ACEPTACION");
        if (ticket.getPrecioContraoferta() == null) {
            throw new RuntimeException("No hay una contraoferta para aceptar");
        }

        ticket.setPrecioOfertado(ticket.getPrecioContraoferta());
        ticket.setPrecioContraoferta(null);
        ticket.setEstado("ASIGNADO");

        return mapearResponse(ticketRepository.save(ticket));
    }

    public TicketResponse rechazarContraoferta(Long ticketId, String correo) {
        Ticket ticket = ticketPorId(ticketId);
        Usuario cliente = usuarioPorCorreo(correo);

        exigirCliente(ticket, cliente);
        exigirEstado(ticket, "PENDIENTE_ACEPTACION");
        if (ticket.getPrecioContraoferta() == null) {
            throw new RuntimeException("No hay una contraoferta para rechazar");
        }

        ticket.setPrecioContraoferta(null);
        ticket.setTecnicoAsignado(null);
        ticket.setEstado("PENDIENTE");

        return mapearResponse(ticketRepository.save(ticket));
    }

    public TicketResponse enCamino(Long ticketId, String correo) {
        Ticket ticket = ticketPorId(ticketId);
        exigirTecnico(ticket, usuarioPorCorreo(correo));
        exigirEstado(ticket, "ASIGNADO");

        ticket.setEstado("EN_CAMINO");
        return mapearResponse(ticketRepository.save(ticket));
    }

    public TicketResponse iniciar(Long ticketId, String correo) {
        Ticket ticket = ticketPorId(ticketId);
        exigirTecnico(ticket, usuarioPorCorreo(correo));
        exigirEstado(ticket, "ASIGNADO", "EN_CAMINO");

        ticket.setEstado("EN_REPARACION");
        if (ticket.getFechaAtencion() == null) {
            ticket.setFechaAtencion(LocalDateTime.now());
        }

        return mapearResponse(ticketRepository.save(ticket));
    }

    public TicketResponse cotizar(Long ticketId, String correo, BigDecimal monto, String detalle) {
        Ticket ticket = ticketPorId(ticketId);
        exigirTecnico(ticket, usuarioPorCorreo(correo));
        exigirEstado(ticket, "EN_REPARACION");

        if (ticket.getCotizacionMonto() != null) {
            throw new RuntimeException("Ya enviaste una cotización para este ticket");
        }
        if (monto == null || monto.signum() <= 0) {
            throw new RuntimeException("Ingresa un monto válido");
        }
        if (monto.compareTo(precioAcordado(ticket)) < 0) {
            throw new RuntimeException("La cotización total no puede ser menor al diagnóstico acordado (S/. " + precioAcordado(ticket) + ")");
        }
        if (detalle == null || detalle.isBlank()) {
            throw new RuntimeException("Describe qué incluye la cotización");
        }

        ticket.setCotizacionMonto(monto);
        ticket.setCotizacionDetalle(detalle.trim());
        ticket.setEstado("COTIZACION_PENDIENTE");

        return mapearResponse(ticketRepository.save(ticket));
    }

    public TicketResponse aprobarCotizacion(Long ticketId, String correo) {
        Ticket ticket = ticketPorId(ticketId);
        exigirCliente(ticket, usuarioPorCorreo(correo));
        exigirEstado(ticket, "COTIZACION_PENDIENTE");

        ticket.setEstado("EN_REPARACION");
        return mapearResponse(ticketRepository.save(ticket));
    }

    public TicketResponse rechazarCotizacion(Long ticketId, String correo) {
        Ticket ticket = ticketPorId(ticketId);
        exigirCliente(ticket, usuarioPorCorreo(correo));
        exigirEstado(ticket, "COTIZACION_PENDIENTE");

        ticket.setPrecioFinal(precioAcordado(ticket));
        ticket.setEstado("FINALIZADO");
        ticket.setFechaFinalizacion(LocalDateTime.now());

        return mapearResponse(ticketRepository.save(ticket));
    }

    public TicketResponse programarVisita(Long ticketId, String correo, LocalDateTime fechaCita) {
        Ticket ticket = ticketPorId(ticketId);
        exigirTecnico(ticket, usuarioPorCorreo(correo));
        exigirEstado(ticket, "ASIGNADO", "EN_REPARACION");

        if (fechaCita == null || fechaCita.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Elige una fecha y hora futuras");
        }

        ticket.setFechaCita(fechaCita);
        ticket.setEstado("ASIGNADO");

        return mapearResponse(ticketRepository.save(ticket));
    }

    public TicketResponse finalizar(Long ticketId, String correo) {
        Ticket ticket = ticketPorId(ticketId);
        exigirTecnico(ticket, usuarioPorCorreo(correo));
        exigirEstado(ticket, "EN_REPARACION");

        BigDecimal total = ticket.getCotizacionMonto() != null
                ? ticket.getCotizacionMonto()
                : precioAcordado(ticket);

        ticket.setPrecioFinal(total);
        ticket.setEstado("FINALIZADO");
        ticket.setFechaFinalizacion(LocalDateTime.now());

        return mapearResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse cobrarEfectivo(Long ticketId, String correo) {
        Ticket ticket = ticketPorId(ticketId);
        exigirTecnico(ticket, usuarioPorCorreo(correo));
        exigirPendienteDeCobro(ticket);

        marcarPagado(ticket, "EFECTIVO");
        ticket = ticketRepository.save(ticket);
        facturaService.emitir(ticket);

        return mapearResponse(ticket);
    }

    @Transactional
    public TicketResponse pagarConCreditos(Long ticketId, String correo) {
        Ticket ticket = ticketPorId(ticketId);
        Usuario cliente = usuarioPorCorreo(correo);

        exigirCliente(ticket, cliente);
        exigirPendienteDeCobro(ticket);

        BigDecimal total = ticket.getPrecioFinal();
        BigDecimal saldo = cliente.getSaldoCreditos() != null ? cliente.getSaldoCreditos() : BigDecimal.ZERO;

        if (saldo.compareTo(total) < 0) {
            throw new RuntimeException("Saldo de créditos insuficiente. Tu saldo es S/. " + saldo.toPlainString());
        }

        creditoService.usar(correo, ticketId, total, "Pago del servicio " + ticket.getCodigoTicket());

        marcarPagado(ticket, "CREDITOS");
        ticket = ticketRepository.save(ticket);
        facturaService.emitir(ticket);

        return mapearResponse(ticket);
    }

    private TicketResponse mapearResponse(Ticket ticket) {
        Long facturaId = null;
        if ("FINALIZADO".equals(ticket.getEstado()) && Boolean.TRUE.equals(ticket.getPagado())) {
            facturaId = facturaRepository.findByTicketId(ticket.getId()).map(Factura::getId).orElse(null);
        }

        return TicketResponse.builder()
                .id(ticket.getId())
                .codigoTicket(ticket.getCodigoTicket())
                .clienteId(ticket.getCliente().getId())
                .clienteNombre(ticket.getCliente().getNombres() + " " + ticket.getCliente().getApellidos())
                .tecnicoAsignadoId(ticket.getTecnicoAsignado() != null ? ticket.getTecnicoAsignado().getId() : null)
                .tecnicoNombre(ticket.getTecnicoAsignado() != null
                        ? ticket.getTecnicoAsignado().getNombres() + " " + ticket.getTecnicoAsignado().getApellidos()
                        : null)
                .tituloServicio(ticket.getServicio() != null ? ticket.getServicio().getNombreServicio() : null)
                .requiereDiagnostico(ticket.getServicio() != null ? ticket.getServicio().getRequierePiezas() : null)
                .titulo(ticket.getTitulo())
                .descripcion(ticket.getDescripcion())
                .tipoDispositivo(ticket.getTipoDispositivo())
                .marca(ticket.getMarca())
                .modelo(ticket.getModelo())
                .urgencia(ticket.getUrgencia())
                .direccionPersonalizada(ticket.getDireccionPersonalizada())
                .latitud(ticket.getLatitud())
                .longitud(ticket.getLongitud())
                .estado(ticket.getEstado())
                .fechaCreacion(ticket.getFechaCreacion())
                .fechaCita(ticket.getFechaCita())
                .fechaFinalizacion(ticket.getFechaFinalizacion())
                .precioOfertado(ticket.getPrecioOfertado())
                .precioContraoferta(ticket.getPrecioContraoferta())
                .cotizacionMonto(ticket.getCotizacionMonto())
                .cotizacionDetalle(ticket.getCotizacionDetalle())
                .precioFinal(ticket.getPrecioFinal())
                .calificacionCliente(ticket.getCalificacionCliente())
                .calificacionTecnico(ticket.getCalificacionTecnico())
                .pagado(ticket.getPagado())
                .metodoPago(ticket.getMetodoPago())
                .facturaId(facturaId)
                .build();
    }
}