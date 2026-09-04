package com.tecnicodomicilio.servicio_tecnico_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequest {
    private Long servicioId;
    private Long direccionId;
    private String direccionPersonalizada;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String titulo;
    private String descripcion;
    private String tipoDispositivo;
    private String marca;
    private String modelo;
    private String urgencia;
}