package com.tecnicodomicilio.servicio_tecnico_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_ticket", unique = true, length = 30, insertable = false, updatable = false)
    private String codigoTicket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_asignado_id")
    private Usuario tecnicoAsignado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id")
    private CatalogoServicio servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direccion_id")
    private DireccionUsuario direccion;

    @Column(name = "direccion_personalizada", length = 200)
    private String direccionPersonalizada;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitud;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tipo_dispositivo", nullable = false, length = 50)
    private String tipoDispositivo;

    @Column(length = 50)
    private String marca;

    @Column(length = 50)
    private String modelo;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String urgencia = "MEDIA";

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "PENDIENTE";

    @Column(name = "fecha_creacion", updatable = false, insertable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_cita")
    private LocalDateTime fechaCita;

    @Column(name = "fecha_atencion")
    private LocalDateTime fechaAtencion;

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    @Column(columnDefinition = "TEXT")
    private String imagenes;

    @Column(name = "comentarios_internos", columnDefinition = "TEXT")
    private String comentariosInternos;

    @Column(name = "precio_visita", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal precioVisita = new BigDecimal("30.00");

    @Column(name = "precio_ofertado", precision = 10, scale = 2)
    private BigDecimal precioOfertado;

    @Column(name = "precio_contraoferta", precision = 10, scale = 2)
    private BigDecimal precioContraoferta;

    @Column(name = "precio_diagnostico", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal precioDiagnostico = BigDecimal.ZERO;

    @Column(name = "precio_repuestos", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal precioRepuestos = BigDecimal.ZERO;

    @Column(name = "precio_final", precision = 10, scale = 2)
    private BigDecimal precioFinal;

    @Column(name = "metodo_pago", length = 20)
    @Builder.Default
    private String metodoPago = "EFECTIVO";

    @Column(nullable = false)
    @Builder.Default
    private Boolean pagado = false;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "calificacion_cliente")
    private Integer calificacionCliente;

    @Column(name = "comentario_cliente", columnDefinition = "TEXT")
    private String comentarioCliente;

    @Column(name = "calificacion_tecnico")
    private Integer calificacionTecnico;

    @Column(name = "comentario_tecnico", columnDefinition = "TEXT")
    private String comentarioTecnico;

    @Column(name = "mensaje_cancelacion", length = 200)
    private String mensajeCancelacion;
}