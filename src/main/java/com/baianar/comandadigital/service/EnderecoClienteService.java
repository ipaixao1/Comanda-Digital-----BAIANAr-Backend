package com.baianar.comandadigital.service;

import com.baianar.comandadigital.dto.request.EnderecoRequest;
import com.baianar.comandadigital.dto.response.EnderecoResponse;
import com.baianar.comandadigital.entity.EnderecoCliente;
import com.baianar.comandadigital.entity.Usuario;
import com.baianar.comandadigital.exception.BusinessException;
import com.baianar.comandadigital.exception.ResourceNotFoundException;
import com.baianar.comandadigital.repository.EnderecoClienteRepository;
import com.baianar.comandadigital.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Endereços de entrega do cliente logado. Regras:
 * - o primeiro endereço vira o padrão automaticamente;
 * - só um endereço padrão por cliente;
 * - ao excluir o padrão, o endereço mais recente que sobrar passa a ser o padrão;
 * - cada cliente só enxerga e altera os próprios endereços.
 */
@Service
@RequiredArgsConstructor
public class EnderecoClienteService {

    static final int LIMITE_POR_CLIENTE = 10;

    private final EnderecoClienteRepository repository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<EnderecoResponse> listar(Long clienteId) {
        return repository.findByClienteIdOrderByPadraoDescIdAsc(clienteId).stream()
                .map(EnderecoResponse::from)
                .toList();
    }

    @Transactional
    public EnderecoResponse criar(Long clienteId, EnderecoRequest req) {
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + clienteId));
        long total = repository.countByClienteId(clienteId);
        if (total >= LIMITE_POR_CLIENTE) {
            throw new BusinessException("Limite de " + LIMITE_POR_CLIENTE + " endereços atingido. Exclua um para adicionar outro.");
        }

        boolean padrao = total == 0 || Boolean.TRUE.equals(req.padrao());
        if (padrao) repository.limparPadrao(clienteId);

        EnderecoCliente e = EnderecoCliente.builder().cliente(cliente).padrao(padrao).build();
        preencher(e, req);
        return EnderecoResponse.from(repository.save(e));
    }

    @Transactional
    public EnderecoResponse atualizar(Long clienteId, Long id, EnderecoRequest req) {
        EnderecoCliente e = buscar(clienteId, id);
        if (Boolean.TRUE.equals(req.padrao()) && !e.isPadrao()) {
            repository.limparPadrao(clienteId);
            e = buscar(clienteId, id); // o clear do contexto soltou a instância anterior
            e.setPadrao(true);
        }
        // desmarcar o padrão pela edição não é permitido: sempre deve existir um padrão
        preencher(e, req);
        return EnderecoResponse.from(repository.save(e));
    }

    @Transactional
    public void remover(Long clienteId, Long id) {
        EnderecoCliente e = buscar(clienteId, id);
        boolean eraPadrao = e.isPadrao();
        repository.delete(e);
        repository.flush();
        if (eraPadrao) {
            repository.findFirstByClienteIdOrderByIdDesc(clienteId).ifPresent(novo -> {
                novo.setPadrao(true);
                repository.save(novo);
            });
        }
    }

    @Transactional
    public EnderecoResponse definirPadrao(Long clienteId, Long id) {
        buscar(clienteId, id); // garante que é do cliente
        repository.limparPadrao(clienteId);
        EnderecoCliente e = buscar(clienteId, id);
        e.setPadrao(true);
        return EnderecoResponse.from(repository.save(e));
    }

    private EnderecoCliente buscar(Long clienteId, Long id) {
        return repository.findByIdAndClienteId(id, clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado: " + id));
    }

    private void preencher(EnderecoCliente e, EnderecoRequest req) {
        String cep = req.cep().replaceAll("\\D", "");
        e.setNome(req.nome().trim());
        e.setCep(cep.substring(0, 5) + "-" + cep.substring(5));
        e.setRua(req.rua().trim());
        e.setNumero(req.numero().trim());
        e.setComplemento(req.complemento() == null || req.complemento().isBlank() ? null : req.complemento().trim());
        e.setBairro(req.bairro().trim());
        e.setCidade(req.cidade().trim());
        e.setEstado(req.estado().trim().toUpperCase());
    }
}
