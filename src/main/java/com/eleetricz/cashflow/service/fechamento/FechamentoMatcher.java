package com.eleetricz.cashflow.service.fechamento;

import com.eleetricz.cashflow.entity.Lancamento;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class FechamentoMatcher {

    private FechamentoMatcher() {}

    static Set<Long> indexByDescricaoId(List<Lancamento> realizados) {
        Set<Long> idx = new HashSet<>();
        for (Lancamento l : realizados) {
            if (l.getDescricao() != null && l.getDescricao().getId() != null) {
                idx.add(l.getDescricao().getId());
            }
        }
        return idx;
    }

    static Set<DescricaoTipoKey> indexByDescricaoIdAndTipo(List<Lancamento> realizados) {
        Set<DescricaoTipoKey> idx = new HashSet<>();
        for (Lancamento l : realizados) {
            if (l.getDescricao() != null && l.getDescricao().getId() != null && l.getDescricao().getTipo() != null) {
                idx.add(new DescricaoTipoKey(l.getDescricao().getId(), l.getDescricao().getTipo().name()));
            }
        }
        return idx;
    }

    record DescricaoTipoKey(Long descricaoId, String tipo) {}
}

