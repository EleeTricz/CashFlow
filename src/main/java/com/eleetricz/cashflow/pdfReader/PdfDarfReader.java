package com.eleetricz.cashflow.pdfReader;

import com.eleetricz.cashflow.entity.ItensDarf;
import com.eleetricz.cashflow.dto.DarfData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PdfDarfReader {

    private static final Pattern PADRAO_PRINCIPAL = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})\\s+(\\d{2}/\\d{2}/\\d{4})\\s+(\\d{17})");
    private static final Pattern PADRAO_TOTAIS = Pattern.compile("Totais\\s+([\\d.,]+)\\s+([\\d.,]+)\\s+([\\d.,]+)\\s+([\\d.,]+)");
    private static final Pattern PADRAO_ARRECADACAO = Pattern.compile("Data de Arrecadação.*?(\\d{2}/\\d{2}/\\d{4})", Pattern.DOTALL);
    private static final Pattern PADRAO_ITEM = Pattern.compile("(\\d{4})\\s+([A-Z\\-\\s]+?)\\s+([\\d.,\\-]+)\\s+([\\d.,\\-]+)\\s+([\\d.,\\-]+)\\s+([\\d.,\\-]+)");

    public List<DarfData> extrairTodosDados(File arquivoPdf) throws IOException {
        List<DarfData> resultados = new ArrayList<>();

        try (PDDocument document = PDDocument.load(arquivoPdf)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPaginas = document.getNumberOfPages();

            for (int i = 1; i <= totalPaginas; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String texto = stripper.getText(document);
                Optional<DarfData> dados = extrairDadosDeTexto(texto);
                dados.ifPresent(resultados::add);
            }
        }

        return resultados;
    }

    private Optional<DarfData> extrairDadosDeTexto(String texto) {
        Pattern padraoDas = Pattern.compile("\\bDARF\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcherDas = padraoDas.matcher(texto);
        if (!matcherDas.find()) {
            throw new IllegalArgumentException("Este arquivo não parece ser uma DARF.");
        }

        DarfData data = new DarfData();

        Matcher m = PADRAO_PRINCIPAL.matcher(texto);
        if (m.find()) {
            data.setPeriodoApuracao(m.group(1));
            data.setDataVencimento(m.group(2));
            data.setNumeroDocumento(m.group(3));
        }

        Matcher mt = PADRAO_TOTAIS.matcher(texto);
        if (mt.find()) {
            data.setTotalPrincipal(normalizar(mt.group(1)));
            data.setTotalMulta(normalizar(mt.group(2)));
            data.setTotalJuros(normalizar(mt.group(3)));
            data.setTotalTotal(normalizar(mt.group(4)));
        }

        Matcher ma = PADRAO_ARRECADACAO.matcher(texto);
        if (ma.find()) {
            data.setDataArrecadacao(ma.group(1));
        }

        Matcher mi = PADRAO_ITEM.matcher(texto);
        while (mi.find()) {
            ItensDarf item = new ItensDarf();
            item.setCodigo(mi.group(1));
            item.setDescricao(mi.group(2).trim());
            item.setPrincipal(normalizar(mi.group(3)));
            item.setMulta(normalizar(mi.group(4)));
            item.setJuros(normalizar(mi.group(5)));
            item.setTotal(normalizar(mi.group(6)));
            data.getItens().add(item);
        }

        if (data.getNumeroDocumento() != null && data.getPeriodoApuracao() != null) {
            return Optional.of(data);
        }

        return Optional.empty();
    }

    private String normalizar(String valor) {
        if (valor == null || valor.equals("-")) return "0.00";
        return valor.replace(".", "").replace(",", ".");
    }
}
