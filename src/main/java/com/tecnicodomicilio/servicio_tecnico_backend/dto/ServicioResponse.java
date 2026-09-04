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
public class ServicioResponse {
    private Long id;
    private String categoria;
    private String subcategoria;
    private String nombreServicio;
    private String descripcion;
    private BigDecimal precioBase;
    private BigDecimal precioMaximo;
    private String tiempoEstimado;
    private Boolean requierePiezas;
}