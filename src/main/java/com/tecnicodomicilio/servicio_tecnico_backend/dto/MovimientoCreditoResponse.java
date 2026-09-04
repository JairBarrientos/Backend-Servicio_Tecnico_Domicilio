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
public class MovimientoCreditoResponse {
    private Long id;
    private String tipo;
    private BigDecimal monto;
    private String descripcion;
    private BigDecimal saldoResultante;
    private LocalDateTime fechaCreacion;
}