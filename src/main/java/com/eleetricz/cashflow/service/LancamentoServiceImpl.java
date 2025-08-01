package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.dto.LancamentoRecorrenteDTO;
import com.eleetricz.cashflow.dto.ReceitaCompraResumoDTO;
import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LancamentoServiceImpl implements LancamentoService{
    private final LancamentoRepository lancamentoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CompetenciaRepository competenciaRepository;
    private final DescricaoRepository descricaoRepository;

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

    @Override
    public void gerarLancamentosRecorrentes(LancamentoRecorrenteDTO dto) {
        YearMonth inicio = YearMonth.parse(dto.getCompetenciaInicio());
        YearMonth fim = YearMonth.parse(dto.getCompetenciaFim());

        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Descricao descricao = descricaoRepository.findById(dto.getDescricaoId())
                .orElseThrow(() -> new RuntimeException("Descrição não encontrada"));

        for (YearMonth ym = inicio; !ym.isAfter(fim); ym = ym.plusMonths(1)) {
            final int mes = ym.getMonthValue();
            final int ano = ym.getYear();

            LocalDate dataOcorrencia = ym.atEndOfMonth();

            Competencia competencia = competenciaRepository
                    .findByMesAndAnoAndEmpresa(mes, ano, empresa)
                    .orElseGet(() -> {
                        Competencia nova = new Competencia();
                        nova.setMes(mes);
                        nova.setAno(ano);
                        nova.setEmpresa(empresa);
                        return competenciaRepository.save(nova);
                    });


            Lancamento lanc = new Lancamento();
            lanc.setValor(dto.getValor());
            lanc.setTipo(TipoLancamento.valueOf(dto.getTipo()));
            lanc.setEmpresa(empresa);
            lanc.setUsuario(usuario);
            lanc.setDescricao(descricao);
            lanc.setCompetencia(competencia);
            lanc.setCompetenciaReferida(competencia);
            lanc.setDataOcorrencia(dataOcorrencia);

            lancamentoRepository.save(lanc);
        }
    }

    @Override
    public void excluirLancamento(Long id) {
        Lancamento lancamento = lancamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lançamento não encontrado"));
        lancamentoRepository.delete(lancamento);
    }

    @Override
    public List<ReceitaCompraResumoDTO> getResumoReceitaCompraAnual(Long empresaId, int ano) {
        List<ReceitaCompraResumoDTO> resumo = new ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            BigDecimal receitas = lancamentoRepository.totalPorDescricaoNomeMesAnoEmpresa("RECEITAS DE VENDAS", mes, ano, empresaId);
            BigDecimal compras = lancamentoRepository.totalPorDescricaoNomeMesAnoEmpresa("COMPRAS A VISTA", mes, ano, empresaId);
            resumo.add(new ReceitaCompraResumoDTO(mes, receitas, compras));
        }
        return resumo;
    }

    @Override
    @Transactional
    public void deletarLancamentosPorEmpresa(Long empresaId) {
        lancamentoRepository.deleteByEmpresaId(empresaId);
    }

    @Override
    public List<Integer> getAnosComLancamentos(Long empresaId){
      return lancamentoRepository.findAnosComLancamentos(empresaId);
    }
}
