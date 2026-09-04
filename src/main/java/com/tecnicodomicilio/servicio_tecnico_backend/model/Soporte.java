package com.tecnicodomicilio.servicio_tecnico_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "soporte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Soporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnico;

    @Column(name = "tipo_consulta", nullable = false, length = 30)
    private String tipoConsulta;

    @Column(nullable = false, length = 100)
    private String asunto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 20)
    @Builder.Default
    private String prioridad = "MEDIA";

    @Column(length = 20)
    @Builder.Default
    private String estado = "ABIERTO";

    @Column(name = "fecha_creacion", updatable = false, insertable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(columnDefinition = "TEXT")
    private String respuesta;

    @Column(columnDefinition = "TEXT")
    private String solucion;

    @Column(name = "calificacion_atencion")
    private Integer calificacionAtencion;

    @Column(name = "comentario_cliente", columnDefinition = "TEXT")
    private String comentarioCliente;

    @Column(name = "archivos_adjuntos", columnDefinition = "TEXT")
    private String archivosAdjuntos;

    @Column(name = "es_anonimo")
    @Builder.Default
    private Boolean esAnonimo = false;
}