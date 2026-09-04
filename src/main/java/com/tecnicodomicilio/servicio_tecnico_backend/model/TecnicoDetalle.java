package com.tecnicodomicilio.servicio_tecnico_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tecnicos_detalles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TecnicoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id", nullable = false, unique = true)
    private Usuario tecnico;

    @Column(length = 200)
    private String especialidades;

    @Column(columnDefinition = "TEXT")
    private String certificaciones;

    @Column(name = "anos_experiencia")
    @Builder.Default
    private Integer anosExperiencia = 0;

    @Column(name = "tarifa_visita", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal tarifaVisita = new BigDecimal("30.00");

    @Column(name = "tarifa_diagnostico", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal tarifaDiagnostico = BigDecimal.ZERO;

    @Column(name = "radio_cobertura")
    @Builder.Default
    private Integer radioCobertura = 10;

    @Column(name = "disponibilidad_json", columnDefinition = "jsonb")
    private String disponibilidadJson;

    @Column(name = "calificacion_promedio", precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal calificacionPromedio = BigDecimal.ZERO;

    @Column(name = "total_servicios")
    @Builder.Default
    private Integer totalServicios = 0;

    @Column(name = "verificado_empresa")
    @Builder.Default
    private Boolean verificadoEmpresa = false;

    @Column(name = "documento_identidad", length = 50)
    private String documentoIdentidad;

    @Column(name = "fecha_contratacion", updatable = false, insertable = false)
    private LocalDateTime fechaContratacion;
}