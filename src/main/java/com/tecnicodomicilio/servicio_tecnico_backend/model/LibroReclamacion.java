package com.tecnicodomicilio.servicio_tecnico_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "libro_reclamaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibroReclamacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnico;

    @Column(name = "tipo_reclamo", nullable = false, length = 30)
    private String tipoReclamo;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 20)
    @Builder.Default
    private String prioridad = "MEDIA";

    @Column(length = 20)
    @Builder.Default
    private String estado = "PENDIENTE";

    @Column(name = "fecha_creacion", updatable = false, insertable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(columnDefinition = "TEXT")
    private String respuesta;

    @Column(name = "calificacion_solucion")
    private Integer calificacionSolucion;

    @Column(name = "archivos_adjuntos", columnDefinition = "TEXT")
    private String archivosAdjuntos;

    @Column(name = "es_anonimo")
    @Builder.Default
    private Boolean esAnonimo = false;

    @Column(name = "codigo_seguimiento", nullable = false, unique = true, length = 20)
    private String codigoSeguimiento;
}