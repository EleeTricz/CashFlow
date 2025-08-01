package com.eleetricz.cashflow.exportacao.service;

import com.eleetricz.cashflow.entity.Lancamento;
import com.eleetricz.cashflow.exportacao.strategy.*;
import com.eleetricz.cashflow.repository.LancamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

@Service
@RequiredArgsConstructor
public class ExportacaoServiceImpl implements ExportacaoService {

    private final LancamentoRepository lancamentoRepository;

    private final ExportacaoStrategy estrategiaPadrao = new ExportacaoPadraoStrategy();

    private final Map<String, ExportacaoStrategy> estrategiasEspecificas = Map.ofEntries(
            entry("DAS", new ExportacaoDasStrategy()),
            entry("ENCARGOS DAS", new ExportacaoEncargosDasStrategy()),
            entry("INSS 13º", new ExportacaoINSSStrategy()),
            entry("DAS PARCELAMENTO", new ExportacaoDasParcelamentoStrategy()),
            entry("ENCARGOS DAS PARCELAMENTO", new ExportacaoEncargosDasParcelamentoStrategy()),
            entry("DAE 10 PARCELAMENTO", new ExportacaoDae10ParcelamentoStrategy()),
            entry("PREFEITURA", new ExportacaoPrefeituraStrategy()),
            entry("MULTA CLT", new ExportacaoMultaCltStrategy()),
            entry("DARF CSLL", new ExportacaoDarfCSLLStrategy()),
            entry("DARF IRPJ", new ExportacaoDarfIRPJStrategy()),
            entry("FERIAS", new ExportacaoFeriasStrategy()),
            entry("RESCISAO", new ExportacaoRescisaoStrategy())
            // Adicione outras aqui conforme necessário
    );

    private final Set<String> descricoesIgnoradas = Set.of(
            "COMPRAS A VISTA",
            "RECEITAS DE VENDAS",
            "CAIXA INICIAL",
            "SERVIÇOS"
            // Outras descrições que não devem gerar exportação
    );

    @Override
    public File exportarLancamentosParaTxt(Long empresaId, int mesInicial, int anoInicial, int mesFinal, int anoFinal) throws IOException {
        List<Lancamento> lancamentos = lancamentoRepository.findByEmpresaAndCompetenciaIntervaloOrdenado(
                empresaId, mesInicial, anoInicial, mesFinal, anoFinal);

        File arquivo = File.createTempFile("exportacao_lancamentos_", ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo))) {
            for (Lancamento lancamento : lancamentos) {

                String nomeDescricao = lancamento.getDescricao().getNome();

                // Ignorar lançamentos com descrições específicas
                if (descricoesIgnoradas.contains(nomeDescricao.toUpperCase())) {
                    continue;
                }

                // Seleciona estratégia específica ou usa padrão
                ExportacaoStrategy strategy = estrategiasEspecificas.getOrDefault(
                        nomeDescricao.toUpperCase(), estrategiaPadrao
                );

                strategy.escreverLancamento(writer, lancamento);
            }
        }

        return arquivo;
    }
}

