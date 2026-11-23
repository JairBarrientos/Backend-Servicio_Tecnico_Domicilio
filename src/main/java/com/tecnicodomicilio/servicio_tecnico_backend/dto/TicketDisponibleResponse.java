package com.tecnicodomicilio.servicio_tecnico_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDisponibleResponse {
    private Long id;
    private String codigoTicket;
    private String titulo;
    private String descripcion;
    private String tipoDispositivo;
    private String categoriaServicio;
    private BigDecimal precioOfertado;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String direccionPersonalizada;
    private String clienteNombre;
    private Double clientePuntaje;
    private Double distanciaKm;
}