package com.tecnicodomicilio.servicio_tecnico_backend.repository;

import com.tecnicodomicilio.servicio_tecnico_backend.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByClienteId(Long clienteId);

    List<Ticket> findByTecnicoAsignadoId(Long tecnicoId);

    List<Ticket> findByEstado(String estado);

    List<Ticket> findByTecnicoAsignadoIdAndEstadoIn(Long tecnicoId, Collection<String> estados);

    long countByEstado(String estado);

    @Query(value = "SELECT fn_calcular_precio_ticket(:ticketId)", nativeQuery = true)
    BigDecimal calcularPrecioTicket(@Param("ticketId") Long ticketId);

    @Query(value = """
            SELECT t.id, t.codigo_ticket, t.titulo, t.descripcion, t.tipo_dispositivo,
                   cs.nombre_servicio, t.precio_ofertado, t.latitud, t.longitud, t.direccion_personalizada,
                   u.nombres, u.apellidos,
                   fn_calcular_distancia(:latTecnico, :lonTecnico, t.latitud, t.longitud) AS distancia_km,
                   t.cliente_id
            FROM tickets t
            JOIN usuarios u ON u.id = t.cliente_id
            LEFT JOIN catalogo_servicios cs ON cs.id = t.servicio_id
            WHERE t.estado IN ('PENDIENTE', 'PENDIENTE_ACEPTACION')
              AND t.latitud IS NOT NULL AND t.longitud IS NOT NULL
              AND (t.tecnico_asignado_id IS NULL OR t.tecnico_asignado_id = :tecnicoId)
              AND (:categoria IS NULL OR cs.categoria = :categoria)
            ORDER BY distancia_km ASC
            """, nativeQuery = true)
    List<Object[]> buscarTicketsDisponibles(@Param("latTecnico") BigDecimal latTecnico,
                                             @Param("lonTecnico") BigDecimal lonTecnico,
                                             @Param("categoria") String categoria,
                                             @Param("tecnicoId") Long tecnicoId);

    @Query("SELECT AVG(t.calificacionTecnico) FROM Ticket t WHERE t.cliente.id = :clienteId AND t.calificacionTecnico IS NOT NULL")
    Double promedioCalificacionCliente(@Param("clienteId") Long clienteId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.estado = 'FINALIZADO' AND t.fechaFinalizacion >= :desde")
    long contarFinalizadosDesde(@Param("desde") LocalDateTime desde);

    @Query("SELECT COALESCE(SUM(t.precioFinal), 0) FROM Ticket t WHERE t.estado = 'FINALIZADO' AND t.fechaFinalizacion >= :desde")
    BigDecimal ingresosDesde(@Param("desde") LocalDateTime desde);

    @Query("SELECT t.tecnicoAsignado.id, t.tecnicoAsignado.nombres, t.tecnicoAsignado.apellidos, COUNT(t), COALESCE(SUM(t.precioFinal), 0) " +
           "FROM Ticket t WHERE t.estado = 'FINALIZADO' AND t.fechaFinalizacion >= :desde AND t.tecnicoAsignado IS NOT NULL " +
           "GROUP BY t.tecnicoAsignado.id, t.tecnicoAsignado.nombres, t.tecnicoAsignado.apellidos ORDER BY COUNT(t) DESC")
    List<Object[]> rendimientoTecnicos(@Param("desde") LocalDateTime desde);
}