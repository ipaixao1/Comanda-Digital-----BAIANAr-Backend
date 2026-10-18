package com.baianar.comandadigital.service;

import com.baianar.comandadigital.dto.request.CriarPedidoRequest;
import com.baianar.comandadigital.dto.request.PedidoItemRequest;
import com.baianar.comandadigital.dto.response.PedidoResponse;
import com.baianar.comandadigital.dto.response.PedidoStatusResponse;
import com.baianar.comandadigital.entity.*;
import com.baianar.comandadigital.entity.enums.MotivoMovimentacao;
import com.baianar.comandadigital.entity.enums.StatusPedido;
import com.baianar.comandadigital.entity.enums.TipoMovimentacao;
import com.baianar.comandadigital.exception.BusinessException;
import com.baianar.comandadigital.exception.ForbiddenOperationException;
import com.baianar.comandadigital.exception.ResourceNotFoundException;
import com.baianar.comandadigital.repository.PedidoRepository;
import com.baianar.comandadigital.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * RF-015 a RF-020, RN03, RN04.
 * Toda a lógica de pedidos (criação pelo cliente e gestão pela cozinha/admin) mora aqui,
 * pois o controle de estoque (baixa automática / estorno) é transacional e precisa ser único.
 */
@Service
@RequiredArgsConstructor
public class PedidoService {

    private static final Map<StatusPedido, StatusPedido> PROXIMO_STATUS = Map.of(
            StatusPedido.RECEBIDO, StatusPedido.CONFIRMADO,
            StatusPedido.CONFIRMADO, StatusPedido.EM_PREPARO,
            StatusPedido.EM_PREPARO, StatusPedido.PRONTO,
            StatusPedido.PRONTO, StatusPedido.SAIU_ENTREGA,
            StatusPedido.SAIU_ENTREGA, StatusPedido.FINALIZADO
    );

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PratoService pratoService;
    private final FichaTecnicaService fichaTecnicaService;
    private final EstoqueService estoqueService;

    // ---------------------------------------------------------------------
    // RF-015/RF-006: criação do pedido pelo cliente (checkout)
    // ---------------------------------------------------------------------

    /** RN03: valida disponibilidade de estoque de cada ingrediente antes de aceitar o pedido. */
    @Transactional
    public PedidoResponse criar(Long clienteId, CriarPedidoRequest request) {
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + clienteId));

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .status(StatusPedido.RECEBIDO)
                .enderecoEntrega(request.enderecoEntrega())
                .observacoes(request.observacoes())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (PedidoItemRequest itemReq : request.itens()) {
            Prato prato = pratoService.buscarEntidade(itemReq.pratoId());

            PedidoItem item = PedidoItem.builder()
                    .pedido(pedido)
                    .prato(prato)
                    .quantidade(itemReq.quantidade())
                    .precoUnitario(prato.getPrecoVenda())
                    .observacoes(itemReq.observacoes())
                    .build();

            pedido.getItens().add(item);
            total = total.add(prato.getPrecoVenda().multiply(BigDecimal.valueOf(itemReq.quantidade())));
        }

        // RN03: confere se há estoque suficiente de cada ingrediente para o pedido inteiro.
        validarDisponibilidadeEstoque(pedido);

        pedido.setValorTotal(total.setScale(2, RoundingMode.HALF_UP));
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    // ---------------------------------------------------------------------
    // RF-008/RF-007: consultas do cliente
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<PedidoResponse> listarMeusPedidos(Long clienteId, Pageable pageable) {
        return pedidoRepository.findByClienteIdOrderByCreatedAtDesc(clienteId, pageable).map(PedidoResponse::from);
    }

    @Transactional(readOnly = true)
    public PedidoStatusResponse buscarStatus(Long pedidoId, Long clienteId) {
        Pedido pedido = buscarEntidade(pedidoId);
        if (!pedido.getCliente().getId().equals(clienteId)) {
            throw new ForbiddenOperationException("Este pedido não pertence ao cliente autenticado.");
        }
        return new PedidoStatusResponse(pedido.getId(), pedido.getStatus().name());
    }

    // ---------------------------------------------------------------------
    // RF-019/RF-020: listagem e detalhe (admin/cozinha)
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<PedidoResponse> listar(StatusPedido status, LocalDateTime inicio, LocalDateTime fim, Pageable pageable) {
        return pedidoRepository.buscarComFiltros(status, inicio, fim, pageable).map(PedidoResponse::from);
    }

    @Transactional(readOnly = true)
    public PedidoResponse detalhar(Long id) {
        return PedidoResponse.from(buscarEntidade(id));
    }

    public Pedido buscarEntidade(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + id));
    }

    // ---------------------------------------------------------------------
    // RF-016/RF-017: ciclo de status + baixa automática
    // ---------------------------------------------------------------------

    /**
     * RF-016: avança o pedido para o próximo status do ciclo.
     * RF-017: ao mudar para CONFIRMADO, dá baixa automática no estoque conforme a ficha técnica × quantidade,
     * tudo na mesma transação (RN03 garante que, se chegou até aqui, há estoque suficiente).
     */
    @Transactional
    public PedidoResponse mudarStatus(Long id, StatusPedido novoStatus) {
        Pedido pedido = buscarEntidade(id);
        StatusPedido statusAtual = pedido.getStatus();

        if (statusAtual == StatusPedido.CANCELADO || statusAtual == StatusPedido.FINALIZADO) {
            throw new BusinessException("Pedido " + statusAtual + " não pode mais mudar de status.");
        }

        StatusPedido esperado = PROXIMO_STATUS.get(statusAtual);
        if (esperado != novoStatus) {
            throw new BusinessException("Transição inválida: de " + statusAtual + " só é possível avançar para " + esperado + ".");
        }

        if (novoStatus == StatusPedido.CONFIRMADO) {
            darBaixaEstoque(pedido);
        }

        pedido.setStatus(novoStatus);
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    /**
     * RF-018/RN04: cancela o pedido. Antes de EM_PREPARO ninguém mexeu no estoque ainda;
     * de CONFIRMADO em diante, estorna (devolve) tudo que foi baixado automaticamente.
     */
    @Transactional
    public PedidoResponse cancelar(Long id, String motivo) {
        Pedido pedido = buscarEntidade(id);
        StatusPedido statusAtual = pedido.getStatus();

        if (statusAtual == StatusPedido.CANCELADO || statusAtual == StatusPedido.FINALIZADO) {
            throw new BusinessException("Pedido " + statusAtual + " não pode mais ser cancelado.");
        }

        // RN04: a partir de EM_PREPARO, o endpoint já é restrito a GERENTE/ADMIN (ver @PreAuthorize do controller).
        boolean estoqueJaFoiBaixado = statusAtual.ordinal() >= StatusPedido.CONFIRMADO.ordinal();
        if (estoqueJaFoiBaixado) {
            estornarEstoque(pedido);
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        pedido.setMotivoCancelamento(motivo);
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    // ---------------------------------------------------------------------
    // Internals — ficha técnica x estoque
    // ---------------------------------------------------------------------

    private void validarDisponibilidadeEstoque(Pedido pedido) {
        for (PedidoItem item : pedido.getItens()) {
            FichaTecnica ficha = fichaTecnicaService.buscarEntidadePorPrato(item.getPrato().getId());
            int rendimento = ficha.getRendimento() != null && ficha.getRendimento() > 0 ? ficha.getRendimento() : 1;

            for (FichaTecnicaItem fti : ficha.getItens()) {
                BigDecimal necessario = calcularConsumo(fti, item.getQuantidade(), rendimento);
                BigDecimal saldoAtual = estoqueService.saldoAtual(fti.getIngrediente().getId());

                if (saldoAtual.compareTo(necessario) < 0) {
                    throw new BusinessException(
                            "RN03: estoque insuficiente de '" + fti.getIngrediente().getNome() +
                                    "' para concluir o pedido (necessário " + necessario + ", disponível " + saldoAtual + ").");
                }
            }
        }
    }

    private void darBaixaEstoque(Pedido pedido) {
        for (PedidoItem item : pedido.getItens()) {
            FichaTecnica ficha = fichaTecnicaService.buscarEntidadePorPrato(item.getPrato().getId());
            int rendimento = ficha.getRendimento() != null && ficha.getRendimento() > 0 ? ficha.getRendimento() : 1;

            for (FichaTecnicaItem fti : ficha.getItens()) {
                BigDecimal consumo = calcularConsumo(fti, item.getQuantidade(), rendimento);
                estoqueService.registrar(
                        fti.getIngrediente(), TipoMovimentacao.SAIDA, consumo,
                        MotivoMovimentacao.VENDA, null, null,
                        fti.getIngrediente().getCustoUnitario(), null, pedido
                );
            }
        }
    }

    private void estornarEstoque(Pedido pedido) {
        for (PedidoItem item : pedido.getItens()) {
            FichaTecnica ficha = fichaTecnicaService.buscarEntidadePorPrato(item.getPrato().getId());
            int rendimento = ficha.getRendimento() != null && ficha.getRendimento() > 0 ? ficha.getRendimento() : 1;

            for (FichaTecnicaItem fti : ficha.getItens()) {
                BigDecimal consumo = calcularConsumo(fti, item.getQuantidade(), rendimento);
                estoqueService.registrar(
                        fti.getIngrediente(), TipoMovimentacao.ESTORNO, consumo,
                        MotivoMovimentacao.ESTORNO, null, null,
                        fti.getIngrediente().getCustoUnitario(), null, pedido
                );
            }
        }
    }

    /** Quantidade de ingrediente consumida = (qtd da ficha × fator_correcao / rendimento) × quantidade do item no pedido. */
    private BigDecimal calcularConsumo(FichaTecnicaItem fti, int quantidadePedida, int rendimento) {
        BigDecimal consumoPorPorcao = fti.getQuantidade()
                .multiply(fti.getFatorCorrecao())
                .divide(BigDecimal.valueOf(rendimento), 4, RoundingMode.HALF_UP);
        return consumoPorPorcao.multiply(BigDecimal.valueOf(quantidadePedida));
    }
}
