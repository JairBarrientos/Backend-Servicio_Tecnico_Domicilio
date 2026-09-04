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
public class MensajeSoporteResponse {
    private Long id;
    private Long remitenteId;
    private String remitenteNombre;
    private String remitenteRol;
    private String contenido;
    private String tipoMensaje;
    private String archivoUrl;
    private Boolean leido;
    private LocalDateTime fechaEnvio;
}