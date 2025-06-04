package com.eleetricz.cashflow.exportacao.strategy;

import com.eleetricz.cashflow.entity.Lancamento;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ExportacaoDae10ParcelamentoStrategy implements ExportacaoStrategy{
    private static final DateTimeFormatter FORMATTER_DATA = DateTimeFormatter.ofPattern("ddMMyyyy");

    private static final DecimalFormat FORMATTER_VALOR;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US); // garante ponto como separador decimal
        FORMATTER_VALOR = new DecimalFormat("0.00", symbols);
    }

    @Override
    public boolean aplicaPara(String descricaoNome) {
        return "DAS PARCELAMENTO".equalsIgnoreCase(descricaoNome);
    }

    @Override
    public void escreverLancamento(BufferedWriter writer, Lancamento lancamento) throws IOException {
        StringBuilder linha = new StringBuilder();

        linha.append(lancamento.getDataOcorrencia().format(FORMATTER_DATA)).append(",");
        linha.append(lancamento.getDescricao().getCodigoDebito() != null ? lancamento.getDescricao().getCodigoDebito() : 0).append(",");
        linha.append(lancamento.getDescricao().getCodigoCredito() != null ? lancamento.getDescricao().getCodigoCredito() : 0).append(",");
        linha.append(FORMATTER_VALOR.format(lancamento.getValor())).append(",");
        linha.append(lancamento.getDescricao().getCodigoHistorico() != null ? lancamento.getDescricao().getCodigoHistorico() : 0).append(",");

        int mes = lancamento.getCompetenciaReferida().getMes();
        int ano = lancamento.getCompetenciaReferida().getAno();
        String obs = lancamento.getObservacao();
        linha.append(String.format("\"%02d/%04d DAE 10 PARCELAMENTO %s\"", mes, ano, obs));

        writer.write(linha.toString());
        writer.newLine();
    }
}
