package com.eleetricz.cashflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReceitaCompraResumoDTO {
    private int mes;
    private BigDecimal receitas;
    private BigDecimal compras;
}
