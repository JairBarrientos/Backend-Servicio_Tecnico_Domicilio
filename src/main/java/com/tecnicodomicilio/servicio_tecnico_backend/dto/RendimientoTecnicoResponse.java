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
public class RendimientoTecnicoResponse {
    private Long tecnicoId;
    private String nombre;
    private long serviciosCompletados;
    private BigDecimal ingresos;
}