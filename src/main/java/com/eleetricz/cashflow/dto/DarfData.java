package com.eleetricz.cashflow.dto;

import com.eleetricz.cashflow.entity.ItensDarf;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DarfData {
    private String numeroDocumento;
    private String periodoApuracao;
    private String dataVencimento;
    private String dataArrecadacao;
    private String totalPrincipal;
    private String totalMulta;
    private String totalJuros;
    private String totalTotal;

    private List<ItensDarf> itens = new ArrayList<>();
}
