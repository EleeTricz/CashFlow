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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

}
