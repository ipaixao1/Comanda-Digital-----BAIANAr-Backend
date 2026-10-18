package com.baianar.comandadigital.service;

import com.baianar.comandadigital.dto.request.PedidoCompraItemRequest;
import com.baianar.comandadigital.dto.request.PedidoCompraRequest;
import com.baianar.comandadigital.dto.response.PedidoCompraItemResponse;
import com.baianar.comandadigital.dto.response.PedidoCompraResponse;
import com.baianar.comandadigital.entity.*;
import com.baianar.comandadigital.entity.enums.MotivoMovimentacao;
import com.baianar.comandadigital.entity.enums.StatusPedidoCompra;
import com.baianar.comandadigital.entity.enums.TipoMovimentacao;
import com.baianar.comandadigital.exception.BusinessException;
import com.baianar.comandadigital.exception.ResourceNotFoundException;
import com.baianar.comandadigital.repository.FornecedorProdutoRepository;
import com.baianar.comandadigital.repository.PedidoCompraRepository;
import com.baianar.comandadigital.repository.UsuarioRepository;
import com.baianar.comandadigital.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** RF-024 a RF-026, RN05: pedidos de compra ao fornecedor e recebimento de mercadoria. */
@Service
@RequiredArgsConstructor
public class CompraService {

    private final PedidoCompraRepository pedidoCompraRepository;
    private final FornecedorProdutoRepository fornecedorProdutoRepository;
    private final UsuarioRepository usuarioRepository;
    private final FornecedorService fornecedorService;
    private final IngredienteService ingredienteService;
    private final EstoqueService estoqueService;

    @Transactional(readOnly = true)
    public Page<PedidoCompraResponse> listar(Pageable pageable) {
        return pedidoCompraRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PedidoCompraResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    private PedidoCompra buscarEntidade(Long id) {
        return pedidoCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de compra não encontrado: " + id));
    }

    /** RF-024: cria pedido de compra como RASCUNHO, usando o preço cadastrado no catálogo do fornecedor. */
    @Transactional
    public PedidoCompraResponse criar(PedidoCompraRequest request) {
        Fornecedor fornecedor = fornecedorService.buscarEntidade(request.fornecedorId());
        Usuario usuarioAtual = usuarioAtual();

        PedidoCompra pedidoCompra = PedidoCompra.builder()
                .fornecedor(fornecedor)
                .status(StatusPedidoCompra.RASCUNHO)
                .usuario(usuarioAtual)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (PedidoCompraItemRequest itemReq : request.itens()) {
            Ingrediente ingrediente = ingredienteService.buscarEntidade(itemReq.ingredienteId());

            FornecedorProduto catalogo = fornecedorProdutoRepository
                    .findByFornecedorIdAndIngredienteId(fornecedor.getId(), ingrediente.getId())
                    .orElseThrow(() -> new BusinessException(
                            "'" + ingrediente.getNome() + "' não está cadastrado no catálogo deste fornecedor."));

            BigDecimal subtotal = itemReq.quantidade().multiply(catalogo.getPreco()).setScale(2, RoundingMode.HALF_UP);

            PedidoCompraItem item = PedidoCompraItem.builder()
                    .pedidoCompra(pedidoCompra)
                    .ingrediente(ingrediente)
                    .quantidade(itemReq.quantidade())
                    .precoUnitario(catalogo.getPreco())
                    .subtotal(subtotal)
                    .build();

            pedidoCompra.getItens().add(item);
            total = total.add(subtotal);
        }

        pedidoCompra.setValorTotal(total);
        return toResponse(pedidoCompraRepository.save(pedidoCompra));
    }

    /** RF-024: avança o status (RASCUNHO > ENVIADO). */
    @Transactional
    public PedidoCompraResponse enviar(Long id) {
        PedidoCompra pedidoCompra = buscarEntidade(id);
        if (pedidoCompra.getStatus() != StatusPedidoCompra.RASCUNHO) {
            throw new BusinessException("Apenas pedidos em RASCUNHO podem ser enviados.");
        }
        pedidoCompra.setStatus(StatusPedidoCompra.ENVIADO);
        return toResponse(pedidoCompraRepository.save(pedidoCompra));
    }

    @Transactional
    public void cancelar(Long id) {
        PedidoCompra pedidoCompra = buscarEntidade(id);
        if (pedidoCompra.getStatus() == StatusPedidoCompra.RECEBIDO) {
            throw new BusinessException("Um pedido de compra já recebido não pode ser cancelado.");
        }
        pedidoCompra.setStatus(StatusPedidoCompra.CANCELADO);
        pedidoCompraRepository.save(pedidoCompra);
    }

    /**
     * RF-025/RN05: registra o recebimento — cria entrada no estoque para cada item
     * e atualiza o custo unitário do ingrediente para o preço pago (RF-012 recalcula o food cost dos pratos
     * automaticamente na próxima consulta, pois o custo é sempre calculado em tempo real).
     */
    @Transactional
    public PedidoCompraResponse receber(Long id) {
        PedidoCompra pedidoCompra = buscarEntidade(id);

        if (pedidoCompra.getStatus() == StatusPedidoCompra.RECEBIDO) {
            throw new BusinessException("Este pedido de compra já foi recebido.");
        }
        if (pedidoCompra.getStatus() == StatusPedidoCompra.CANCELADO) {
            throw new BusinessException("Não é possível receber um pedido de compra cancelado.");
        }

        for (PedidoCompraItem item : pedidoCompra.getItens()) {
            estoqueService.registrar(
                    item.getIngrediente(), TipoMovimentacao.ENTRADA, item.getQuantidade(),
                    MotivoMovimentacao.COMPRA, null, null,
                    item.getPrecoUnitario(), pedidoCompra, null
            );
            // RN05: custo unitário do ingrediente atualiza para o novo preço pago.
            ingredienteService.atualizarCustoUnitario(item.getIngrediente().getId(), item.getPrecoUnitario());
        }

        pedidoCompra.setStatus(StatusPedidoCompra.RECEBIDO);
        return toResponse(pedidoCompraRepository.save(pedidoCompra));
    }

    private Usuario usuarioAtual() {
        return usuarioRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado."));
    }

    private PedidoCompraResponse toResponse(PedidoCompra pc) {
        List<PedidoCompraItemResponse> itens = pc.getItens().stream()
                .map(i -> new PedidoCompraItemResponse(
                        i.getId(), i.getIngrediente().getId(), i.getIngrediente().getNome(),
                        i.getQuantidade(), i.getPrecoUnitario(), i.getSubtotal()))
                .toList();

        return new PedidoCompraResponse(
                pc.getId(), pc.getFornecedor().getId(), pc.getFornecedor().getRazaoSocial(),
                pc.getStatus().name(), pc.getValorTotal(), itens, pc.getCreatedAt()
        );
    }
}
