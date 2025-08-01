package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.dto.DasData;
import com.eleetricz.cashflow.entity.LancamentoDas;

import java.util.List;

public interface LancamentoDasService {
    void importarTodos();
    LancamentoDas salvar(LancamentoDas lancamentoDas);
    boolean registroJaExiste(Long empresaId, String competencia, String numeroDocumento);
    int importarDadosPdfDas(Long empresaId, List<DasData> dadosExtraidos);
}
