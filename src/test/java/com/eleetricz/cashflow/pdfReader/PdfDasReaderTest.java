package com.eleetricz.cashflow.pdfReader;

import com.eleetricz.cashflow.dto.DasData;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PdfDasReaderTest {

    @Test
    public void deveExtrairTodosDadosDeUmPdfComVariasPaginas() throws Exception {
        File arquivoPdf = new File("src/test/resources/DAS_TEST.pdf");
        PdfDasReader reader = new PdfDasReader();

        List<DasData> resultados = reader.extrairTodosDados(arquivoPdf);

        assertFalse(resultados.isEmpty(), "A lista de dados1 extraídos não deve estar vazia");

        DasData dados1 = resultados.get(0);
        assertEquals("12/2019", dados1.competencia);
        assertEquals("20/01/2020", dados1.dataVencimento);
        assertEquals("07202000461290356", dados1.numeroDocumento);
        assertEquals("1094.46", dados1.total);
        assertEquals("0.00", dados1.juros);
        assertEquals("0.00", dados1.multa);
        assertEquals("1094.46", dados1.principal);
        assertEquals("20/01/2020", dados1.dataArrecadacao);

        DasData dados2 = resultados.get(1);
        assertEquals("01/2020", dados2.competencia);
        assertEquals("20/02/2020", dados2.dataVencimento);
        assertEquals("07202003874335401", dados2.numeroDocumento);
        assertEquals("503.64", dados2.total);
        assertEquals("0.00", dados2.juros);
        assertEquals("0.00", dados2.multa);
        assertEquals("503.64", dados2.principal);
        assertEquals("20/02/2020", dados2.dataArrecadacao);
    }
}
