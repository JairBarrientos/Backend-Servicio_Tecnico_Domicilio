package com.tecnicodomicilio.servicio_tecnico_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_soporte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeSoporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "soporte_id", nullable = false)
    private Soporte soporte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remitente_id", nullable = false)
    private Usuario remitente;

    @Column(name = "remitente_rol", nullable = false, length = 20)
    private String remitenteRol;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha_envio", updatable = false, insertable = false)
    private LocalDateTime fechaEnvio;

    @Column(nullable = false)
    @Builder.Default
    private Boolean leido = false;

    @Column(name = "tipo_mensaje", length = 20)
    @Builder.Default
    private String tipoMensaje = "TEXTO";

    @Column(name = "archivo_url", length = 255)
    private String archivoUrl;
}