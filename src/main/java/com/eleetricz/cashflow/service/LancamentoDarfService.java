package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.dto.DarfData;

import java.util.List;

public interface LancamentoDarfService {
    void importarTodos();
    int importarDadosPdfDarf(Long empresaId, List<DarfData> dadosExtraidos);
}
