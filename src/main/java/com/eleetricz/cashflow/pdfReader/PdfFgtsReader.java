package com.eleetricz.cashflow.pdfReader;

import com.eleetricz.cashflow.dto.FgtsData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PdfFgtsReader {

    private static final Pattern PADRAO_COMPETENCIA = Pattern.compile("\\b(\\d{2}/\\d{4})\\b");
    private static final Pattern PADRAO_IDENTIFICADOR = Pattern.compile("Identificador\\s*(\\d[\\d\\-]+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern PADRAO_DATA_PAGAMENTO = Pattern.compile(
            "Guia paga em:\\s*.*?(\\d{2}/\\d{2}/\\d{4})",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern PADRAO_TOTAL_GUIA = Pattern.compile(
            "(?:Total\\s+da\\s+Guia|Valor\\s+a\\s+recolher)\\s*:?(?:\\s|\\R)*([\\d.]+,\\d{2})",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PADRAO_TOTAL_FGTS = Pattern.compile(
            "Total\\s+FGTS:\\s*([\\d.]+,\\d{2})\\s*([\\d.]+,\\d{2})\\s*([\\d.]+,\\d{2})\\s+([\\d.]+,\\d{2})\\s+([\\d.]+,\\d{2})",
            Pattern.CASE_INSENSITIVE);

    public List<FgtsData> extrairTodosDados(File arquivoPdf) throws IOException {
        List<FgtsData> resultados = new ArrayList<>();

        try (PDDocument document = PDDocument.load(arquivoPdf)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPaginas = document.getNumberOfPages();

            for (int i = 1; i <= totalPaginas; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String texto = stripper.getText(document);
                System.out.println("=== TEXTO PDF ===");
                System.out.println(texto);
                System.out.println("=================");
                Optional<FgtsData> dados = extrairDadosDeTexto(texto);
                dados.ifPresent(resultados::add);
            }
        }

        return resultados;
    }

    private Optional<FgtsData> extrairDadosDeTexto(String texto) {
        if (!texto.toUpperCase().contains("GUIA DO FGTS DIGITAL")) {
            throw new IllegalArgumentException("Este arquivo não parece ser uma guia de FGTS.");
        }

        FgtsData data = new FgtsData();

        Matcher competenciaMatcher = PADRAO_COMPETENCIA.matcher(extrairLinhaTag(texto));
        if (competenciaMatcher.find()) {
            data.setCompetencia(competenciaMatcher.group(1));
        } else {
            Matcher fallbackCompetencia = PADRAO_COMPETENCIA.matcher(texto);
            if (fallbackCompetencia.find()) {
                data.setCompetencia(fallbackCompetencia.group(1));
            }
        }

        Matcher idMatcher = PADRAO_IDENTIFICADOR.matcher(texto);
        if (idMatcher.find()) {
            data.setIdentificadorGuia(idMatcher.group(1).trim());
        }

        Matcher dataPagamentoMatcher = PADRAO_DATA_PAGAMENTO.matcher(texto);
        if (dataPagamentoMatcher.find()) {
            data.setDataPagamento(dataPagamentoMatcher.group(1));
        }

        Matcher totaisMatcher = PADRAO_TOTAL_FGTS.matcher(texto.replace("\n", " "));
        if (totaisMatcher.find()) {
            // Ordem no PDF: Total | Encargos FGTS | FGTS Mensal | FGTS Rescisório | Indenização
            // Observação: em alguns PDFs os 3 primeiros valores vêm "colados" (sem espaços).
            data.setJuros(normalizar(totaisMatcher.group(2))); // encargos
            data.setMulta("0.00");
            data.setValorPrincipal(normalizar(totaisMatcher.group(3))); // FGTS mensal
        }

        if (isBlank(data.getValorPrincipal())) {
            Matcher totalGuiaMatcher = PADRAO_TOTAL_GUIA.matcher(texto);
            if (totalGuiaMatcher.find()) {
                data.setValorPrincipal(normalizar(totalGuiaMatcher.group(1)));
            }
        }

        if (isBlank(data.getCompetencia()) || isBlank(data.getDataPagamento()) || isBlank(data.getValorPrincipal())) {
            return Optional.empty();
        }

        if (isBlank(data.getJuros())) {
            data.setJuros("0.00");
        }
        if (isBlank(data.getMulta())) {
            data.setMulta("0.00");
        }

        return Optional.of(data);
    }

    private String extrairLinhaTag(String texto) {
        String[] linhas = texto.split("\\r?\\n");
        for (int i = 0; i < linhas.length; i++) {
            if (linhas[i].trim().equalsIgnoreCase("Tag") && i + 1 < linhas.length) {
                return linhas[i + 1];
            }
        }
        return texto;
    }

    private String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return "0.00";
        }
        return valor.replace(".", "").replace(",", ".");
    }

    private boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }
}
