package com.tecnicodomicilio.servicio_tecnico_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgramarVisitaRequest {
    private LocalDateTime fechaCita;
}