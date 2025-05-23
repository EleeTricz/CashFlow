package com.eleetricz.cashflow.exportacao.strategy;

import com.eleetricz.cashflow.entity.Lancamento;

import java.io.BufferedWriter;
import java.io.IOException;

public interface ExportacaoStrategy {
    boolean aplicaPara(String descricaoNome);
    void escreverLancamento(BufferedWriter writer, Lancamento lancamento) throws IOException;
}
