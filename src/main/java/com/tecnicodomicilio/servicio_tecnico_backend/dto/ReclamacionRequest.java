package com.tecnicodomicilio.servicio_tecnico_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReclamacionRequest {
    private Long ticketId;
    private Long tecnicoId;
    private String tipoReclamo;
    private String titulo;
    private String descripcion;
    private String prioridad;
    private Boolean esAnonimo;
}