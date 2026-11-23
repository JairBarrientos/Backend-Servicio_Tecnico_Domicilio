package com.tecnicodomicilio.servicio_tecnico_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {
    private Long id;
    private String codigoTicket;
    private Long clienteId;
    private String clienteNombre;
    private Long tecnicoAsignadoId;
    private String tecnicoNombre;
    private String tituloServicio;
    private Boolean requiereDiagnostico;
    private String titulo;
    private String descripcion;
    private String tipoDispositivo;
    private String marca;
    private String modelo;
    private String urgencia;
    private String direccionPersonalizada;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaCita;
    private LocalDateTime fechaFinalizacion;
    private BigDecimal precioOfertado;
    private BigDecimal precioContraoferta;
    private BigDecimal cotizacionMonto;
    private String cotizacionDetalle;
    private BigDecimal precioFinal;
    private Integer calificacionCliente;
    private Integer calificacionTecnico;
    private Boolean pagado;
    private String metodoPago;
    private Long facturaId;
}