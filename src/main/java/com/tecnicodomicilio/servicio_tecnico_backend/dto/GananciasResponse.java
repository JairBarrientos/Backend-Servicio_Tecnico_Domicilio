package com.tecnicodomicilio.servicio_tecnico_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GananciasResponse {
    private String rango;
    private Integer serviciosCompletados;
    private BigDecimal totalGanado;
    private BigDecimal totalCobrado;
    private BigDecimal totalPorCobrar;
    private Double calificacionPromedio;
    private List<BarraGanancia> barras;
    private List<TicketResponse> tickets;
}