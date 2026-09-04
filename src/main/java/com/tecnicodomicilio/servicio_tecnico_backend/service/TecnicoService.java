package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.TecnicoCercanoResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.repository.TecnicoDetalleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TecnicoService {

    private final TecnicoDetalleRepository tecnicoDetalleRepository;

    public TecnicoService(TecnicoDetalleRepository tecnicoDetalleRepository) {
        this.tecnicoDetalleRepository = tecnicoDetalleRepository;
    }

    public List<TecnicoCercanoResponse> buscarCercanos(BigDecimal latitud, BigDecimal longitud) {
        List<Object[]> resultados = tecnicoDetalleRepository.buscarTecnicosCercanos(latitud, longitud);

        return resultados.stream().map(fila -> TecnicoCercanoResponse.builder()
                .id(((Number) fila[0]).longValue())
                .nombres((String) fila[1])
                .apellidos((String) fila[2])
                .especialidades((String) fila[3])
                .calificacionPromedio((BigDecimal) fila[4])
                .totalServicios(((Number) fila[5]).intValue())
                .tarifaVisita((BigDecimal) fila[6])
                .distanciaKm(((Number) fila[7]).doubleValue())
                .build()
        ).collect(Collectors.toList());
    }
}