package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.LancamentoEsperado;

import java.util.List;

public interface LancamentoEsperadoService {
    List<LancamentoEsperado> listarPorEmpresa(Long empresaId);
    LancamentoEsperado salvar(LancamentoEsperado esperado);
    void excluir(Long id);
    LancamentoEsperado buscarPorId(Long id);
}
