package com.eleetricz.cashflow.taxconnect.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PagamentoNormalizadoDTO {

    private String numeroDocumento;
    private String tipoDocumento;
    private String codigoReceita;
    private String descricaoReceita;

    private String competenciaApuracao;
    private String competenciaPagamento;

    private String dataPagamento;
    private String dataVencimento;

    private BigDecimal valorPrincipal;
    private BigDecimal valorMulta;
    private BigDecimal valorJuros;
    private BigDecimal valorTotal;

    private String categoria;
}
