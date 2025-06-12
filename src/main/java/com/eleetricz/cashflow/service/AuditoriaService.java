package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.LancamentoEsperado;

import java.util.List;

public interface AuditoriaService {
    List<LancamentoEsperado> verificarPendencias(Long empresaId, String competencia);
}
