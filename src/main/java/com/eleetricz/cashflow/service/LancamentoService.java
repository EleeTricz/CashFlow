package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.dto.LancamentoRecorrenteDTO;
import com.eleetricz.cashflow.entity.Competencia;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.Lancamento;
import com.eleetricz.cashflow.entity.TipoLancamento;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface LancamentoService {
    Lancamento salvar(Lancamento lancamento);
    List<Lancamento> listarPorEmpresa(Empresa empresa);
    List<Lancamento> listarPorCompetencia(Empresa empresa, Competencia competencia);
    BigDecimal calcularSaldoPorEmpresa(Empresa empresa);
    Map<String, BigDecimal> calcularSaldoPorCompetencia(Empresa empresa);
    List<Lancamento> buscarPorEmpresaOrdenadoPorData(Long empresaId, boolean asc);
    Map<String, BigDecimal> calcularSaldoAcumuladoPorCompetencia(Empresa empresa);
    BigDecimal somarPorTipoEAno(Empresa empresa, TipoLancamento tipo, int ano);
    void gerarLancamentosRecorrentes(LancamentoRecorrenteDTO dto);
    void excluirLancamento(Long id);
}
