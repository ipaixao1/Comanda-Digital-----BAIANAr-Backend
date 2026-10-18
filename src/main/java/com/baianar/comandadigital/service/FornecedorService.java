package com.baianar.comandadigital.service;

import com.baianar.comandadigital.dto.request.FornecedorProdutoRequest;
import com.baianar.comandadigital.dto.request.FornecedorRequest;
import com.baianar.comandadigital.dto.response.CotacaoResponse;
import com.baianar.comandadigital.dto.response.FornecedorProdutoResponse;
import com.baianar.comandadigital.dto.response.FornecedorResponse;
import com.baianar.comandadigital.entity.Fornecedor;
import com.baianar.comandadigital.entity.FornecedorProduto;
import com.baianar.comandadigital.entity.Ingrediente;
import com.baianar.comandadigital.entity.enums.StatusFornecedor;
import com.baianar.comandadigital.entity.enums.UnidadeMedida;
import com.baianar.comandadigital.exception.BusinessException;
import com.baianar.comandadigital.exception.ConflictException;
import com.baianar.comandadigital.exception.ResourceNotFoundException;
import com.baianar.comandadigital.repository.FornecedorProdutoRepository;
import com.baianar.comandadigital.repository.FornecedorRepository;
import com.baianar.comandadigital.util.CnpjValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** RF-021 a RF-023, RN07. */
@Service
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final FornecedorProdutoRepository fornecedorProdutoRepository;
    private final IngredienteService ingredienteService;

    public Page<FornecedorResponse> listar(Pageable pageable) {
        return fornecedorRepository.findAll(pageable).map(FornecedorResponse::from);
    }

    public FornecedorResponse buscarPorId(Long id) {
        return FornecedorResponse.from(buscarEntidade(id));
    }

    public Fornecedor buscarEntidade(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor não encontrado: " + id));
    }

    @Transactional
    public FornecedorResponse criar(FornecedorRequest request) {
        if (!CnpjValidator.isValid(request.cnpj())) {
            throw new BusinessException("CNPJ inválido.");
        }
        if (fornecedorRepository.existsByCnpj(request.cnpj())) {
            throw new ConflictException("Já existe um fornecedor cadastrado com este CNPJ.");
        }

        Fornecedor fornecedor = Fornecedor.builder()
                .razaoSocial(request.razaoSocial())
                .cnpj(request.cnpj())
                .telefone(request.telefone())
                .email(request.email())
                .status(StatusFornecedor.ATIVO)
                .build();

        return FornecedorResponse.from(fornecedorRepository.save(fornecedor));
    }

    @Transactional
    public FornecedorResponse atualizar(Long id, FornecedorRequest request) {
        Fornecedor fornecedor = buscarEntidade(id);

        if (!fornecedor.getCnpj().equals(request.cnpj())) {
            if (!CnpjValidator.isValid(request.cnpj())) {
                throw new BusinessException("CNPJ inválido.");
            }
            if (fornecedorRepository.existsByCnpj(request.cnpj())) {
                throw new ConflictException("Já existe um fornecedor cadastrado com este CNPJ.");
            }
            fornecedor.setCnpj(request.cnpj());
        }

        fornecedor.setRazaoSocial(request.razaoSocial());
        fornecedor.setTelefone(request.telefone());
        fornecedor.setEmail(request.email());

        return FornecedorResponse.from(fornecedorRepository.save(fornecedor));
    }

    /** RN06: exclusão lógica. */
    @Transactional
    public void desativar(Long id) {
        Fornecedor fornecedor = buscarEntidade(id);
        fornecedor.setStatus(StatusFornecedor.INATIVO);
        fornecedorRepository.save(fornecedor);
    }

    /** RF-022: catálogo de produtos de um fornecedor. */
    @Transactional(readOnly = true)
    public List<FornecedorProdutoResponse> listarCatalogo(Long fornecedorId) {
        return fornecedorProdutoRepository.findByFornecedorId(fornecedorId).stream()
                .map(FornecedorProdutoResponse::from)
                .toList();
    }

    @Transactional
    public FornecedorProdutoResponse adicionarAoCatalogo(Long fornecedorId, FornecedorProdutoRequest request) {
        Fornecedor fornecedor = buscarEntidade(fornecedorId);
        Ingrediente ingrediente = ingredienteService.buscarEntidade(request.ingredienteId());

        FornecedorProduto fornecedorProduto = fornecedorProdutoRepository
                .findByFornecedorIdAndIngredienteId(fornecedorId, request.ingredienteId())
                .orElseGet(() -> FornecedorProduto.builder().fornecedor(fornecedor).ingrediente(ingrediente).build());

        fornecedorProduto.setPreco(request.preco());
        fornecedorProduto.setUnidadeVenda(UnidadeMedida.valueOf(request.unidadeVenda().toUpperCase()));

        return FornecedorProdutoResponse.from(fornecedorProdutoRepository.save(fornecedorProduto));
    }

    /** RF-023: cotação comparativa — todos os fornecedores que vendem o ingrediente, ordenados por preço. */
    @Transactional(readOnly = true)
    public CotacaoResponse cotacao(Long ingredienteId) {
        Ingrediente ingrediente = ingredienteService.buscarEntidade(ingredienteId);

        List<CotacaoResponse.Oferta> ofertas = fornecedorProdutoRepository
                .findByIngredienteIdOrderByPrecoAsc(ingredienteId).stream()
                .map(fp -> new CotacaoResponse.Oferta(
                        fp.getFornecedor().getId(), fp.getFornecedor().getRazaoSocial(),
                        fp.getPreco(), fp.getUnidadeVenda().name()))
                .toList();

        return new CotacaoResponse(ingrediente.getId(), ingrediente.getNome(), ofertas);
    }
}
