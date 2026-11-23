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
public class OnboardingResponse {
    private String estado;
    private String motivoRechazo;
    private String especialidades;
    private Integer anosExperiencia;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private Integer radioCobertura;
    private Boolean perfilCompleto;
    private Boolean tieneCv;
    private Boolean tieneDni;
    private Boolean listoParaEnviar;
}