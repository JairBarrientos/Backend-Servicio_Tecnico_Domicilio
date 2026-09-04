package com.tecnicodomicilio.servicio_tecnico_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promociones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tipo_descuento", length = 20)
    @Builder.Default
    private String tipoDescuento = "PORCENTAJE";

    @Column(name = "valor_descuento", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDescuento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id")
    private CatalogoServicio servicio;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(name = "codigo_promocional", unique = true, length = 20)
    private String codigoPromocional;

    @Column(name = "usos_maximos")
    @Builder.Default
    private Integer usosMaximos = 0;

    @Column(name = "usos_actuales")
    @Builder.Default
    private Integer usosActuales = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}