package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.Descricao;

import java.util.List;

public interface DescricaoService {
    Descricao salvar(Descricao descricao);
    List<Descricao> listarTodas();
}
