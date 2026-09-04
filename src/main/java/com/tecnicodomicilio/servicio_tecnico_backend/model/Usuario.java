package com.tecnicodomicilio.servicio_tecnico_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 25)
    private String nombres;

    @Column(nullable = false, length = 25)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @Column(nullable = false, unique = true, length = 60)
    private String correo;

    @Column(nullable = false, length = 60)
    private String password;

    @Column(length = 9)
    private String telefono;

    @Column(nullable = false, length = 20)
    private String rol;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false)
    @Builder.Default
    private Boolean verificado = false;

    @Column(name = "codigo_verificacion", length = 6)
    private String codigoVerificacion;

    @Column(name = "fecha_codigo")
    private LocalDateTime fechaCodigo;

    @Column(name = "intentos_recuperacion")
    @Builder.Default
    private Integer intentosRecuperacion = 0;

    @Column(name = "fecha_bloqueo")
    private LocalDateTime fechaBloqueo;

    @Column(name = "foto_perfil", length = 225)
    private String fotoPerfil;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitud;

    @Column(name = "saldo_creditos", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal saldoCreditos = BigDecimal.ZERO;

    @Column(name = "fecha_registro", updatable = false, insertable = false)
    private LocalDateTime fechaRegistro;
}