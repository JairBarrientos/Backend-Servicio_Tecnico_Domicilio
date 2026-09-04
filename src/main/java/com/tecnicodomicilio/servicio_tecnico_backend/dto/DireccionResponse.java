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
public class DireccionResponse {
    private Long id;
    private String nombreDireccion;
    private String direccion;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String referencia;
    private Boolean esPrincipal;
}