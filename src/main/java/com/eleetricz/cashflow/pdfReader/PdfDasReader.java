package com.eleetricz.cashflow.pdfReader;

import com.eleetricz.cashflow.dto.DasData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PdfDasReader {

    private static final Pattern PADRAO_DADOS_INICIAIS = Pattern.compile(
            "(\\d{2}/\\d{4})\\s+(\\d{2}/\\d{2}/\\d{4})\\s+([0-9A-Za-z\\-.]+)"
    );

    private static final Pattern PADRAO_TOTAIS = Pattern.compile(
            "Totais\\s+([\\d.,]+)\\s+([\\d.,]+)\\s+([\\d.,]+)\\s+([\\d.,]+)"
    );

    public List<DasData> extrairTodosDados(File arquivoPdf) throws IOException {
        List<DasData> resultados = new ArrayList<>();

        try (PDDocument document = PDDocument.load(arquivoPdf)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPaginas = document.getNumberOfPages();

            for (int i = 1; i <= totalPaginas; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String texto = stripper.getText(document);

                Optional<DasData> dados = extrairDadosDeTexto(texto);
                dados.ifPresent(resultados::add);
            }
        }

        return resultados;
    }

    private Optional<DasData> extrairDadosDeTexto(String texto) {

        Pattern padraoDas = Pattern.compile("\\bDAS\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcherDas = padraoDas.matcher(texto);
        if (!matcherDas.find()) {
            throw new IllegalArgumentException("Este arquivo não parece ser um DAS.");
        }

        DasData dados = new DasData();

        Matcher m1 = PADRAO_DADOS_INICIAIS.matcher(texto);
        if (m1.find()) {
            dados.competencia = m1.group(1);
            dados.dataVencimento = m1.group(2);
            dados.numeroDocumento = m1.group(3);
        }

        Matcher m2 = PADRAO_TOTAIS.matcher(texto);
        if (m2.find()) {
            dados.total = normalizarValor(m2.group(4));
            dados.juros = normalizarValor(m2.group(3));
            dados.multa = normalizarValor(m2.group(2));
            dados.principal = normalizarValor(m2.group(1));
        }

        String[] linhas = texto.split("\\r?\\n");
        for (int i = 0; i < linhas.length - 2; i++) {
            if (linhas[i].toLowerCase().contains("banco data de arrecadação")) {
                String proximaLinha = linhas[i + 2];
                Matcher matcherData = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})").matcher(proximaLinha);
                if (matcherData.find()) {
                    dados.dataArrecadacao = matcherData.group(1);
                    break;
                }
            }
        }

        if (dados.competencia != null && dados.numeroDocumento != null && dados.total != null) {
            return Optional.of(dados);
        }

        return Optional.empty();
    }

    private String normalizarValor(String valor) {
        return valor.replace(".", "").replace(",", ".");
    }
}
