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
public class TecnicoRevisionResponse {
    private Long tecnicoId;
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private String dni;
    private String especialidades;
    private Integer anosExperiencia;
    private Integer radioCobertura;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String estado;
    private String motivoRechazo;
    private LocalDateTime fechaEnvio;
    private Boolean tieneCv;
    private Boolean tieneDni;
}