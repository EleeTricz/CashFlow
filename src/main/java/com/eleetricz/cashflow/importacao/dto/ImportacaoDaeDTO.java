package com.eleetricz.cashflow.importacao.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class ImportacaoDaeDTO {
    private String naturezaReceita;
    private LocalDate dataPagamento;
    private BigDecimal valor;
}
