package com.tecnicodomicilio.servicio_tecnico_backend.repository;

import com.tecnicodomicilio.servicio_tecnico_backend.model.TecnicoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TecnicoDetalleRepository extends JpaRepository<TecnicoDetalle, Long> {

    Optional<TecnicoDetalle> findByTecnicoId(Long tecnicoId);

    @Query(value = """
            SELECT u.id, u.nombres, u.apellidos, td.especialidades, td.calificacion_promedio,
                   td.total_servicios, td.tarifa_visita,
                   fn_calcular_distancia(:latitud, :longitud, u.latitud, u.longitud) AS distancia_km
            FROM usuarios u
            JOIN tecnicos_detalles td ON td.tecnico_id = u.id
            WHERE u.rol = 'TECNICO'
              AND u.estado = 'ACTIVO'
              AND fn_calcular_distancia(:latitud, :longitud, u.latitud, u.longitud) <= td.radio_cobertura
            ORDER BY distancia_km ASC
            """, nativeQuery = true)
    List<Object[]> buscarTecnicosCercanos(@Param("latitud") BigDecimal latitud,
                                           @Param("longitud") BigDecimal longitud);

    @Query(value = """
            SELECT u.id
            FROM usuarios u
            JOIN tecnicos_detalles td ON td.tecnico_id = u.id
            WHERE u.rol = 'TECNICO'
              AND u.estado = 'ACTIVO'
              AND u.latitud IS NOT NULL AND u.longitud IS NOT NULL
              AND fn_calcular_distancia(:latitud, :longitud, u.latitud, u.longitud) <= td.radio_cobertura
              AND fn_tecnico_disponible(u.id, :fechaHora) = true
            ORDER BY fn_calcular_distancia(:latitud, :longitud, u.latitud, u.longitud) ASC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Long> buscarTecnicoMasCercanoDisponible(@Param("latitud") BigDecimal latitud,
                                                       @Param("longitud") BigDecimal longitud,
                                                       @Param("fechaHora") LocalDateTime fechaHora);
}