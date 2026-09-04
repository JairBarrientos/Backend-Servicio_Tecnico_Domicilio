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
public class TecnicoCercanoResponse {
    private Long id;
    private String nombres;
    private String apellidos;
    private String especialidades;
    private BigDecimal calificacionPromedio;
    private Integer totalServicios;
    private BigDecimal tarifaVisita;
    private Double distanciaKm;
}