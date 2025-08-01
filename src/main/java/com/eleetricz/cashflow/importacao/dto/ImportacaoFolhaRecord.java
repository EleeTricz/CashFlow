package com.eleetricz.cashflow.importacao.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ImportacaoFolhaRecord(
        LocalDate dataOcorrencia,
        Long contaCredito,
        BigDecimal valor,
        String complemento
) {
}
