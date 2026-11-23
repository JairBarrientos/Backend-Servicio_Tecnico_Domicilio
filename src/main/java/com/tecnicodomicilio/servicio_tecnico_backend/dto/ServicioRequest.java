package com.tecnicodomicilio.servicio_tecnico_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioRequest {
    private String categoria;
    private String subcategoria;
    private String nombreServicio;
    private String descripcion;
    private BigDecimal precioBase;
    private BigDecimal precioMaximo;
    private String tiempoEstimado;
    private Boolean requierePiezas;
    private String imagenUrl;
}