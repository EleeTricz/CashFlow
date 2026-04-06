package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.dto.FgtsData;

import java.util.List;

public interface LancamentoFgtsService {
    void importarTodos();
    int importarDadosPdfFgts(Long empresaId, List<FgtsData> dadosExtraidos);
}
