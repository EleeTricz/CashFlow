package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.Competencia;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.Lancamento;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface LancamentoService {
    Lancamento salvar(Lancamento lancamento);
    List<Lancamento> listarPorEmpresa(Empresa empresa);
    List<Lancamento> listarPorCompetencia(Empresa empresa, Competencia competencia);
    BigDecimal calcularSaldoPorEmpresa(Empresa empresa);
    Map<String, BigDecimal> calcularSaldoPorCompetencia(Empresa empresa);
    public List<Lancamento> buscarPorEmpresaOrdenadoPorData(Long empresaId, boolean asc);
}
