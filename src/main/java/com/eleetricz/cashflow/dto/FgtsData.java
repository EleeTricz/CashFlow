package com.eleetricz.cashflow.dto;

import lombok.Data;

@Data
public class FgtsData {
    private String competencia;
    private String dataPagamento;
    private String valorPrincipal;
    private String juros;
    private String multa;
    private String identificadorGuia;
}
