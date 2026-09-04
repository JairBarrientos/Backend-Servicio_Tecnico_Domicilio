package com.tecnicodomicilio.servicio_tecnico_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "catalogo_servicios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(length = 50)
    private String subcategoria;

    @Column(name = "nombre_servicio", nullable = false, unique = true, length = 100)
    private String nombreServicio;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "precio_maximo", precision = 10, scale = 2)
    private BigDecimal precioMaximo;

    @Column(name = "tiempo_estimado", length = 20)
    private String tiempoEstimado;

    @Column(name = "requiere_piezas")
    @Builder.Default
    private Boolean requierePiezas = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_creacion", updatable = false, insertable = false)
    private LocalDateTime fechaCreacion;
}