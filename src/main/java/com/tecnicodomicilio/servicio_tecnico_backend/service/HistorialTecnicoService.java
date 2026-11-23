package com.tecnicodomicilio.servicio_tecnico_backend.service;

import com.tecnicodomicilio.servicio_tecnico_backend.dto.BarraGanancia;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.GananciasResponse;
import com.tecnicodomicilio.servicio_tecnico_backend.dto.TicketResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class HistorialTecnicoService {

    private static final String[] DIAS = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
    private static final String[] MESES = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

    private final TicketService ticketService;

    public HistorialTecnicoService(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    public GananciasResponse obtener(String correo, String rangoSolicitado) {
        String rango = rangoSolicitado != null ? rangoSolicitado.toUpperCase() : "SEMANA";
        LocalDate hoy = LocalDate.now();
        LocalDate lunes = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate desde = switch (rango) {
            case "SEMANA" -> lunes;
            case "MES" -> hoy.withDayOfMonth(1);
            case "TODO" -> null;
            default -> throw new RuntimeException("Rango no válido. Usa SEMANA, MES o TODO");
        };

        List<TicketResponse> finalizados = ticketService.listarTrabajos(correo, true).stream()
                .filter(t -> t.getFechaFinalizacion() != null)
                .filter(t -> desde == null || !t.getFechaFinalizacion().toLocalDate().isBefore(desde))
                .sorted(Comparator.comparing(TicketResponse::getFechaFinalizacion, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        BigDecimal ganado = total(finalizados, t -> true);
        BigDecimal cobrado = total(finalizados, t -> Boolean.TRUE.equals(t.getPagado()));

        OptionalDouble promedio = finalizados.stream()
                .filter(t -> t.getCalificacionCliente() != null)
                .mapToInt(TicketResponse::getCalificacionCliente)
                .average();

        return GananciasResponse.builder()
                .rango(rango)
                .serviciosCompletados(finalizados.size())
                .totalGanado(ganado)
                .totalCobrado(cobrado)
                .totalPorCobrar(ganado.subtract(cobrado))
                .calificacionPromedio(promedio.isPresent()
                        ? Math.round(promedio.getAsDouble() * 10.0) / 10.0
                        : null)
                .barras(barras(rango, finalizados, hoy, lunes))
                .tickets(finalizados)
                .build();
    }

    private List<BarraGanancia> barras(String rango, List<TicketResponse> tickets, LocalDate hoy, LocalDate lunes) {
        List<BarraGanancia> barras = new ArrayList<>();

        if ("SEMANA".equals(rango)) {
            for (int i = 0; i < 7; i++) {
                LocalDate dia = lunes.plusDays(i);
                barras.add(barra(DIAS[i], tickets, fecha -> fecha.equals(dia)));
            }
        } else if ("MES".equals(rango)) {
            int semanas = (hoy.lengthOfMonth() + 6) / 7;
            for (int s = 0; s < semanas; s++) {
                int semana = s;
                barras.add(barra("Sem " + (s + 1), tickets, fecha -> (fecha.getDayOfMonth() - 1) / 7 == semana));
            }
        } else {
            YearMonth actual = YearMonth.from(hoy);
            for (int i = 5; i >= 0; i--) {
                YearMonth mes = actual.minusMonths(i);
                barras.add(barra(MESES[mes.getMonthValue() - 1], tickets, fecha -> YearMonth.from(fecha).equals(mes)));
            }
        }

        return barras;
    }

    private BarraGanancia barra(String etiqueta, List<TicketResponse> tickets, Predicate<LocalDate> condicion) {
        BigDecimal total = total(tickets, t -> condicion.test(t.getFechaFinalizacion().toLocalDate()));
        return BarraGanancia.builder().etiqueta(etiqueta).total(total).build();
    }

    private BigDecimal total(List<TicketResponse> tickets, Predicate<TicketResponse> condicion) {
        return tickets.stream()
                .filter(condicion)
                .map(t -> t.getPrecioFinal() != null ? t.getPrecioFinal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}