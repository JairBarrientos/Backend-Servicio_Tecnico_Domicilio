package com.tecnicodomicilio.servicio_tecnico_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DireccionRequest {
    private String nombreDireccion;
    private String direccion;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String referencia;
    private Boolean esPrincipal;
}