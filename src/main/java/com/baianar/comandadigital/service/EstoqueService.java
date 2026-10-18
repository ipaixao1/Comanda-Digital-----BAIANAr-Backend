package com.baianar.comandadigital.service;

import com.baianar.comandadigital.dto.request.MovimentacaoManualRequest;
import com.baianar.comandadigital.dto.response.EstoqueSaldoResponse;
import com.baianar.comandadigital.dto.response.MovimentacaoResponse;
import com.baianar.comandadigital.entity.EstoqueMovimentacao;
import com.baianar.comandadigital.entity.Ingrediente;
import com.baianar.comandadigital.entity.Pedido;
import com.baianar.comandadigital.entity.PedidoCompra;
import com.baianar.comandadigital.entity.Usuario;
import com.baianar.comandadigital.entity.enums.MotivoMovimentacao;
import com.baianar.comandadigital.entity.enums.TipoMovimentacao;
import com.baianar.comandadigital.exception.BusinessException;
import com.baianar.comandadigital.exception.ResourceNotFoundException;
import com.baianar.comandadigital.repository.EstoqueMovimentacaoRepository;
import com.baianar.comandadigital.repository.IngredienteRepository;
import com.baianar.comandadigital.repository.UsuarioRepository;
import com.baianar.comandadigital.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * RF-027 a RF-033: controle de estoque.
 * Ponto único de escrita de {@link EstoqueMovimentacao} — usado também pelo módulo
 * de Compras (entrada automática) e pelo módulo de Pedidos (baixa/estorno automáticos).
 */
@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final IngredienteRepository ingredienteRepository;
    private final EstoqueMovimentacaoRepository movimentacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public BigDecimal saldoAtual(Long ingredienteId) {
        BigDecimal saldo = movimentacaoRepository.saldoPorIngrediente(ingredienteId);
        return saldo != null ? saldo : BigDecimal.ZERO;
    }

    /** RF-031: saldo em tempo real de todos os ingredientes. */
    public List<EstoqueSaldoResponse> saldoGeral() {
        return ingredienteRepository.findAll().stream()
                .map(this::toSaldoResponse)
                .toList();
    }

    /** RF-032: ingredientes abaixo do estoque mínimo. */
    public List<EstoqueSaldoResponse> alertasMinimo() {
        return saldoGeral().stream()
                .filter(EstoqueSaldoResponse::abaixoDoMinimo)
                .toList();
    }

    /** RF-026: histórico de preços de compra (ingrediente x fornecedor ao longo do tempo). */
    @Transactional(readOnly = true)
    public List<MovimentacaoResponse> historicoPrecos(Long ingredienteId) {
        return movimentacaoRepository.historicoPrecosCompra(ingredienteId).stream()
                .map(MovimentacaoResponse::from)
                .toList();
    }

    /** RF-033: movimentações paginadas (RNF08). */
    @Transactional(readOnly = true)
    public Page<MovimentacaoResponse> listarMovimentacoes(Pageable pageable) {
        return movimentacaoRepository.findAllByOrderByCreatedAtDesc(pageable).map(MovimentacaoResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponse> listarMovimentacoesPorIngrediente(Long ingredienteId, Pageable pageable) {
        return movimentacaoRepository.findByIngredienteIdOrderByCreatedAtDesc(ingredienteId, pageable).map(MovimentacaoResponse::from);
    }

    /** RF-030: saída manual (perda) — motivo obrigatório. RF-028: entrada manual/ajuste. */
    @Transactional
    public MovimentacaoResponse registrarMovimentacaoManual(MovimentacaoManualRequest request) {
        Ingrediente ingrediente = ingredienteRepository.findById(request.ingredienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente não encontrado: " + request.ingredienteId()));

        TipoMovimentacao tipo = TipoMovimentacao.valueOf(request.tipo().toUpperCase());
        if (tipo != TipoMovimentacao.ENTRADA && tipo != TipoMovimentacao.SAIDA) {
            throw new BusinessException("Movimentação manual aceita apenas os tipos ENTRADA ou SAIDA.");
        }

        MotivoMovimentacao motivo = MotivoMovimentacao.valueOf(request.motivo().toUpperCase());

        if (tipo == TipoMovimentacao.SAIDA) {
            BigDecimal saldoAtual = saldoAtual(ingrediente.getId());
            if (saldoAtual.compareTo(request.quantidade()) < 0) {
                throw new BusinessException("Saldo insuficiente de '" + ingrediente.getNome() + "' para registrar esta saída.");
            }
        }

        EstoqueMovimentacao movimentacao = registrar(
                ingrediente, tipo, request.quantidade(), motivo,
                request.lote(), request.validade(),
                request.custoUnitario() != null ? request.custoUnitario() : ingrediente.getCustoUnitario(),
                null, null
        );

        return MovimentacaoResponse.from(movimentacao);
    }

    /**
     * Ponto único de gravação de movimentação de estoque, usado por este serviço
     * e também diretamente pelos serviços de Compra (entrada) e Pedido (saída/estorno).
     */
    @Transactional
    public EstoqueMovimentacao registrar(Ingrediente ingrediente, TipoMovimentacao tipo, BigDecimal quantidade,
                                          MotivoMovimentacao motivo, String lote, java.time.LocalDate validade,
                                          BigDecimal custoUnitario, PedidoCompra pedidoCompra, Pedido pedido) {
        Usuario usuarioAtual = usuarioRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado."));

        EstoqueMovimentacao movimentacao = EstoqueMovimentacao.builder()
                .ingrediente(ingrediente)
                .tipo(tipo)
                .quantidade(quantidade)
                .motivo(motivo)
                .lote(lote)
                .validade(validade)
                .custoUnitario(custoUnitario)
                .pedidoCompra(pedidoCompra)
                .pedido(pedido)
                .usuario(usuarioAtual)
                .build();

        return movimentacaoRepository.save(movimentacao);
    }

    private EstoqueSaldoResponse toSaldoResponse(Ingrediente ingrediente) {
        BigDecimal saldo = saldoAtual(ingrediente.getId());
        boolean abaixoMinimo = saldo.compareTo(ingrediente.getEstoqueMinimo()) < 0;
        return new EstoqueSaldoResponse(
                ingrediente.getId(), ingrediente.getNome(), ingrediente.getSku(),
                ingrediente.getUnidadePadrao().name(), saldo, ingrediente.getEstoqueMinimo(), abaixoMinimo
        );
    }
}
