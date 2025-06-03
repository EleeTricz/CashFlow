package com.eleetricz.cashflow.parcelamento;

import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ParcelaServiceImpl implements ParcelaService {
    private final ParcelaRepository parcelaRepository;
    private final DescricaoRepository descricaoRepository;
    private final LancamentoRepository lancamentoRepository;
    private final CompetenciaRepository competenciaRepository;
    private final ParcelamentoRepository parcelamentoRepository;

    @Override
    public List<Parcela> listarTodas() {
        return parcelaRepository.findAll();
    }

    @Override
    public Parcela buscarPorId(Long id) {
        return parcelaRepository.findById(id).orElse(null);
    }

    public void registrarPagamentoComLancamento(Long idParcela, BigDecimal valorBase, BigDecimal valorEncargos,
                                                LocalDate dataPagamento, String competenciaStr, String competenciaReferidaStr) {

        Parcela parcela = parcelaRepository.findById(idParcela)
                .orElseThrow(() -> new RuntimeException("Parcela não encontrada"));

        if (parcela.isPaga()) {
            throw new RuntimeException("Parcela " +parcela.getNumeroParcela() + "já está paga.");
        }

        parcela.setPaga(true);
        parcela.setDataPagamento(dataPagamento);

        parcela.setValorEncargos(valorEncargos);

        BigDecimal totalPago = valorBase.add(valorEncargos);
        parcela.setValorPago(totalPago);

        Descricao descricaoBase = parcela.getParcelamento().getDescricaoBase();
        String descricaoFormatada = descricaoBase.getNome() + " PARCELAMENTO";

        int num   = parcela.getNumeroParcela();
        int total = parcela.getParcelamento().getTotalParcelas();

        Descricao descricao = descricaoRepository.findByNomeIgnoreCase(descricaoFormatada)
                .orElseGet(() -> {
                    Descricao nova = new Descricao();
                    nova.setNome(descricaoFormatada);
                    return descricaoRepository.save(nova);
                });

        Empresa empresa = parcela.getParcelamento().getEmpresa();

        Competencia competencia = parseOrCreateCompetencia(competenciaStr, empresa);
        Competencia competenciaReferida = parseOrCreateCompetencia(competenciaReferidaStr, empresa);

        // Lançamento base
        if (valorBase != null && valorBase.compareTo(BigDecimal.ZERO) > 0) {
            Lancamento lancamentoBase = new Lancamento();
            lancamentoBase.setDataOcorrencia(dataPagamento);
            lancamentoBase.setValor(valorBase);
            lancamentoBase.setTipo(TipoLancamento.SAIDA);
            lancamentoBase.setDescricao(descricao);
            lancamentoBase.setEmpresa(empresa);
            lancamentoBase.setCompetencia(competencia);
            lancamentoBase.setCompetenciaReferida(competenciaReferida);
            lancamentoBase.setParcela(parcela);
            lancamentoBase.setObservacao(num + "/" + total);
            lancamentoRepository.save(lancamentoBase);

            parcela.getLancamentos().add(lancamentoBase);
        }

        // Lançamento encargos
        if (valorEncargos != null && valorEncargos.compareTo(BigDecimal.ZERO) > 0) {
            String nomeEncargos = "ENCARGOS " +descricaoFormatada;
            Descricao descricaoEncargos = descricaoRepository.findByNomeIgnoreCase(nomeEncargos)
                    .orElseGet(() -> {
                        Descricao nova = new Descricao();
                        nova.setNome(nomeEncargos);
                        return descricaoRepository.save(nova);
                    });

            Lancamento lancamentoEncargos = new Lancamento();
            lancamentoEncargos.setDataOcorrencia(dataPagamento);
            lancamentoEncargos.setValor(valorEncargos);
            lancamentoEncargos.setTipo(TipoLancamento.SAIDA);
            lancamentoEncargos.setDescricao(descricaoEncargos);
            lancamentoEncargos.setEmpresa(empresa);
            lancamentoEncargos.setCompetencia(competencia);
            lancamentoEncargos.setCompetenciaReferida(competenciaReferida);
            lancamentoEncargos.setParcela(parcela);
            lancamentoEncargos.setObservacao(num + "/" + total);
            lancamentoRepository.save(lancamentoEncargos);

            parcela.getLancamentos().add(lancamentoEncargos);
        }

        parcelaRepository.save(parcela);

        Parcelamento parcelamento = parcela.getParcelamento();
        boolean quitado = parcelamento.getParcelas().stream().allMatch(Parcela::isPaga);
        parcelamento.setQuitado(quitado);
        parcelamentoRepository.save(parcelamento);

    }


    private Competencia parseOrCreateCompetencia(String competenciaStr, Empresa empresa) {
        if (competenciaStr == null || !competenciaStr.matches("\\d{2}/\\d{4}")) {
            throw new IllegalArgumentException("Formato de competência inválido: " + competenciaStr);
        }

        String[] partes = competenciaStr.split("/");
        int mes = Integer.parseInt(partes[0]);
        int ano = Integer.parseInt(partes[1]);

        return competenciaRepository.findByMesAndAnoAndEmpresa(mes, ano, empresa)
                .orElseGet(() -> {
                    Competencia nova = new Competencia();
                    nova.setMes(mes);
                    nova.setAno(ano);
                    nova.setEmpresa(empresa);
                    return competenciaRepository.save(nova);
                });
    }
}
