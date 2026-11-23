package com.tecnicodomicilio.servicio_tecnico_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionResponse {
    private String tipoRegistro;
    private Long id;
    private String referencia;
    private String categoria;
    private String titulo;
    private String descripcion;
    private String prioridad;
    private String estado;
    private LocalDateTime fechaCreacion;
    private String respuesta;
    private String solucion;
    private String solicitante;
    private String solicitanteCorreo;
    private String ticketCodigo;
}