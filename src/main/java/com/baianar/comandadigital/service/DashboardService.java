package com.baianar.comandadigital.service;

import com.baianar.comandadigital.dto.response.*;
import com.baianar.comandadigital.entity.Prato;
import com.baianar.comandadigital.entity.enums.StatusPrato;
import com.baianar.comandadigital.repository.FichaTecnicaRepository;
import com.baianar.comandadigital.repository.PedidoRepository;
import com.baianar.comandadigital.repository.PratoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** RF-034 a RF-037: dashboard administrativo. Apenas consultas via repository (RNF). */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PedidoRepository pedidoRepository;
    private final PratoRepository pratoRepository;
    private final FichaTecnicaRepository fichaTecnicaRepository;
    private final FichaTecnicaService fichaTecnicaService;
    private final EstoqueService estoqueService;

    /** RF-034: KPIs do dia. */
    public DashboardResumoResponse resumoDoDia() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fim = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal faturamento = pedidoRepository.somaFaturamento(inicio, fim);
        long totalPedidos = pedidoRepository.contaPedidos(inicio, fim);

        BigDecimal ticketMedio = totalPedidos > 0
                ? faturamento.divide(BigDecimal.valueOf(totalPedidos), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new DashboardResumoResponse(faturamento, totalPedidos, ticketMedio, foodCostMedioAtivos());
    }

    /** RF-035: top 5 pratos mais vendidos no período informado (padrão: últimos 30 dias). */
    public List<TopPratoResponse> topPratos(LocalDateTime inicio, LocalDateTime fim, int limite) {
        LocalDateTime dataInicio = inicio != null ? inicio : LocalDateTime.now().minusDays(30);
        LocalDateTime dataFim = fim != null ? fim : LocalDateTime.now();

        return pedidoRepository.topPratosVendidos(dataInicio, dataFim, PageRequest.of(0, limite)).stream()
                .map(row -> new TopPratoResponse((Long) row[0], (String) row[1], ((Number) row[2]).longValue()))
                .toList();
    }

    /** RF-036: alertas de estoque crítico, exibidos direto no dashboard. */
    public List<EstoqueSaldoResponse> alertasEstoque() {
        return estoqueService.alertasMinimo();
    }

    /** RF-037: faturamento diário em um período, para gráfico de linha. */
    public List<VendaPeriodoResponse> vendasPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        LocalDateTime dataInicio = inicio != null ? inicio : LocalDateTime.now().minusDays(30);
        LocalDateTime dataFim = fim != null ? fim : LocalDateTime.now();

        return pedidoRepository.vendasPorPeriodo(dataInicio, dataFim).stream()
                .map(row -> new VendaPeriodoResponse(((java.sql.Date) row[0]).toLocalDate(), (BigDecimal) row[1]))
                .toList();
    }

    private BigDecimal foodCostMedioAtivos() {
        List<Prato> ativos = pratoRepository.findAll().stream()
                .filter(p -> p.getStatus() == StatusPrato.ATIVO)
                .filter(p -> fichaTecnicaRepository.findByPratoId(p.getId()).isPresent())
                .toList();

        if (ativos.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal soma = ativos.stream()
                .map(p -> fichaTecnicaService.calcularCustoPorPrato(p.getId()).foodCostPercent())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return soma.divide(BigDecimal.valueOf(ativos.size()), 2, RoundingMode.HALF_UP);
    }
}
