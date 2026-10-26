package com.baianar.comandadigital.util;

import com.baianar.comandadigital.entity.Pedido;
import com.baianar.comandadigital.entity.PedidoItem;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Estimativa de tempo do pedido (jornada do cliente).
 *
 * <p><b>Preparo</b> = o prato mais demorado do pedido (a cozinha prepara os itens em paralelo)
 * + um acréscimo por unidade extra (mais porções = mais trabalho), limitado a um teto.
 * O tempo de cada prato vem de {@code prato.tempo_preparo_min}; se não estiver cadastrado, usa o padrão.</p>
 *
 * <p><b>Entrega</b> = tempo de deslocamento até o cliente. O sistema ainda não tem coordenadas do endereço
 * nem integração com mapas, então o deslocamento é um valor fixo ({@link #TEMPO_ENTREGA_MIN}).
 * Para calcular por distância, é aqui que a regra deve ser trocada.</p>
 *
 * <p><b>Faixa da loja</b> (estilo iFood, "Entrega 20–30 min"): mediana do tempo de preparo dos pratos ativos
 * + deslocamento, arredondada de 5 em 5 min, ± {@link #MARGEM_FAIXA_MIN}. Usa a mediana (e não a média) para que
 * poucos pratos muito demorados não inflem o tempo mostrado para a loja toda.</p>
 *
 * <p>Tudo é calculado na hora, sem coluna nova no banco.</p>
 */
public record EstimativaPedido(int preparoMin, int entregaMin, int totalMin) {

    /** Usado quando o prato não tem tempo de preparo cadastrado. */
    public static final int TEMPO_PADRAO_PREPARO_MIN = 15;
    /** Minutos somados para cada unidade além da primeira (somando todos os itens do pedido). */
    public static final int ACRESCIMO_POR_UNIDADE_EXTRA_MIN = 2;
    /** Teto do acréscimo por quantidade, para pedidos grandes não explodirem a estimativa. */
    public static final int ACRESCIMO_MAXIMO_MIN = 20;
    /** Deslocamento estimado do entregador até o cliente. */
    public static final int TEMPO_ENTREGA_MIN = 15;
    /** Meia-largura da faixa exibida na loja ("20–30 min" = centro 25 ± 5). */
    public static final int MARGEM_FAIXA_MIN = 5;

    public static EstimativaPedido calcular(Pedido pedido) {
        int maiorTempo = 0;
        int unidades = 0;

        for (PedidoItem item : pedido.getItens()) {
            Integer tempoPrato = item.getPrato().getTempoPreparoMin();
            int tempo = (tempoPrato != null && tempoPrato > 0) ? tempoPrato : TEMPO_PADRAO_PREPARO_MIN;
            maiorTempo = Math.max(maiorTempo, tempo);
            unidades += item.getQuantidade() != null ? item.getQuantidade() : 0;
        }

        if (maiorTempo == 0) {
            maiorTempo = TEMPO_PADRAO_PREPARO_MIN; // pedido sem itens (não deveria acontecer)
        }

        int acrescimo = Math.min(Math.max(unidades - 1, 0) * ACRESCIMO_POR_UNIDADE_EXTRA_MIN, ACRESCIMO_MAXIMO_MIN);
        int preparo = maiorTempo + acrescimo;
        return new EstimativaPedido(preparo, TEMPO_ENTREGA_MIN, preparo + TEMPO_ENTREGA_MIN);
    }

    /**
     * Faixa de entrega da loja a partir dos tempos de preparo dos pratos ativos.
     *
     * @return {mínimo, máximo} em minutos
     */
    public static int[] faixaLoja(List<Integer> temposPreparo) {
        List<Integer> validos = temposPreparo.stream()
                .filter(t -> t != null && t > 0)
                .sorted()
                .toList();

        int mediana;
        if (validos.isEmpty()) {
            mediana = TEMPO_PADRAO_PREPARO_MIN;
        } else if (validos.size() % 2 == 1) {
            mediana = validos.get(validos.size() / 2);
        } else {
            mediana = Math.round((validos.get(validos.size() / 2 - 1) + validos.get(validos.size() / 2)) / 2f);
        }

        int centro = Math.round((mediana + TEMPO_ENTREGA_MIN) / 5f) * 5;
        return new int[]{Math.max(centro - MARGEM_FAIXA_MIN, 5), centro + MARGEM_FAIXA_MIN};
    }

    /** Momento previsto de chegada (criação do pedido + total). Fica nulo se o pedido ainda não tem data. */
    public LocalDateTime previsaoEntregaEm(LocalDateTime criadoEm) {
        return criadoEm == null ? null : criadoEm.plusMinutes(totalMin);
    }
}
