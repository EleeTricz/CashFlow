package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.LancamentoDas;

public interface LancamentoDasService {
    void importarTodos();
    LancamentoDas salvar(LancamentoDas lancamentoDas);
    boolean registroJaExiste(Long empresaId, String competencia, String numeroDocumento);
}
