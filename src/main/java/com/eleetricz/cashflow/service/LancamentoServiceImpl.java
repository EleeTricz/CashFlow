package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.Competencia;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.Lancamento;
import com.eleetricz.cashflow.entity.TipoLancamento;
import com.eleetricz.cashflow.repository.LancamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class LancamentoServiceImpl implements LancamentoService{
    @Autowired private LancamentoRepository lancamentoRepository;

    @Override
    public Lancamento salvar(Lancamento lancamento) {
        return lancamentoRepository.save(lancamento);
    }

    @Override
    public List<Lancamento> listarPorEmpresa(Empresa empresa) {
        return lancamentoRepository.findByEmpresa(empresa);
    }

    @Override
    public List<Lancamento> listarPorCompetencia(Empresa empresa, Competencia competencia) {
        return lancamentoRepository.findByCompetenciaAndEmpresa(competencia, empresa);
    }

    @Override
    public BigDecimal calcularSaldoPorEmpresa(Empresa empresa) {
        List<Lancamento> lancamentos = listarPorEmpresa(empresa);
        return lancamentos.stream()
                .map(l -> l.getTipo() == TipoLancamento.ENTRADA
                        ? l.getValor()
                        : l.getValor().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    @Override
    public Map<String, BigDecimal> calcularSaldoPorCompetencia(Empresa empresa) {
        List<Lancamento> lancamentos = lancamentoRepository.findByEmpresa(empresa);

        Map<String, BigDecimal> saldoPorCompetencia = new LinkedHashMap<>();

        for (Lancamento lancamento : lancamentos) {
            Competencia comp = lancamento.getCompetencia();

            String chave = comp.getMes() + "/" + comp.getAno();

            BigDecimal valor = lancamento.getTipo().equals(TipoLancamento.ENTRADA)
                    ? lancamento.getValor()
                    : lancamento.getValor().negate();

            saldoPorCompetencia.merge(chave, valor, BigDecimal::add);
        }

        return saldoPorCompetencia;
    }

    @Override
    public List<Lancamento> buscarPorEmpresaOrdenadoPorData(Long empresaId, boolean asc) {
        Sort sort = Sort.by(asc ? Sort.Direction.ASC : Sort.Direction.DESC, "dataOcorrencia");
        return lancamentoRepository.findByEmpresaId(empresaId, sort);
    }

    @Override
    public Map<String, BigDecimal> calcularSaldoAcumuladoPorCompetencia(Empresa empresa) {
        List<Lancamento> lancamentos = lancamentoRepository.findByEmpresa(empresa);

        if (lancamentos == null || lancamentos.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, BigDecimal> saldoPorCompetencia = new TreeMap<>();

        for (Lancamento lancamento : lancamentos) {
            Competencia comp = lancamento.getCompetencia();
            if (comp == null || comp.getAno() <= 0 || comp.getMes() <= 0) {
                continue;
            }

            String chave = String.format("%04d-%02d", comp.getAno(), comp.getMes());

            BigDecimal valor = lancamento.getValor() != null ? lancamento.getValor() : BigDecimal.ZERO;

            if (TipoLancamento.SAIDA.equals(lancamento.getTipo())) {
                valor = valor.negate();
            }

            saldoPorCompetencia.merge(chave, valor, BigDecimal::add);
        }

        Map<String, BigDecimal> saldoAcumulado = new LinkedHashMap<>();
        BigDecimal acumulado = BigDecimal.ZERO;

        for (Map.Entry<String, BigDecimal> entry : saldoPorCompetencia.entrySet()) {
            acumulado = acumulado.add(entry.getValue());
            saldoAcumulado.put(entry.getKey(), acumulado);
        }

        return saldoAcumulado;
    }

    @Override
    public BigDecimal somarPorTipoEAno(Empresa empresa, TipoLancamento tipo, int ano) {
        return lancamentoRepository.somarPorTipoEAno(empresa, tipo, ano);
    }
}
