package com.tecnicodomicilio.servicio_tecnico_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CotizacionRequest {
    private BigDecimal monto;
    private String detalle;
}