package com.eleetricz.cashflow.pdfReader;

import com.eleetricz.cashflow.dto.DarfData;
import com.eleetricz.cashflow.entity.ItensDarf;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfDarfReaderTest {

    PdfDarfReader reader = new PdfDarfReader();

    @Test
    void testExtrairDadosDeDarfPdf() throws Exception {
        URL resource = getClass().getClassLoader().getResource("DARF_TEST.pdf");
        assertNotNull(resource, "Arquivo PDF de teste não encontrado!");

        File arquivo = new File(resource.toURI());
        List<DarfData> dadosExtraidos = reader.extrairTodosDados(arquivo);

        assertFalse(dadosExtraidos.isEmpty(), "Nenhum dado foi extraído do PDF");

        DarfData primeiro = dadosExtraidos.get(0);

        System.out.println("Número Documento: " + primeiro.getNumeroDocumento());
        System.out.println("Período Apuração: " + primeiro.getPeriodoApuracao());
        System.out.println("Data Vencimento: " + primeiro.getDataVencimento());
        System.out.println("Data Arrecadação: " + primeiro.getDataArrecadacao());
        System.out.println("Total Principal: " + primeiro.getTotalPrincipal());
        System.out.println("Total Juros: " + primeiro.getTotalJuros());
        System.out.println("Total Multa: " + primeiro.getTotalMulta());
        System.out.println("Total Total: " + primeiro.getTotalTotal());

        for (ItensDarf item : primeiro.getItens()) {
            System.out.println(">> Item: " + item.getCodigo() + " | " + item.getDescricao() +
                    " | " + item.getPrincipal() + " | " + item.getMulta() + " | " + item.getJuros() + " | " + item.getTotal());
        }


    }
}
