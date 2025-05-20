package com.eleetricz.cashflow.dto;

public class DasData {
    public String competencia;
    public String dataVencimento;
    public String numeroDocumento;
    public String total;
    public String principal;
    public String multa;
    public String juros;
    public String dataArrecadacao;

    @Override
    public String toString() {
        return "DasData{" +
                "competencia='" + competencia + '\'' +
                ", dataVencimento='" + dataVencimento + '\'' +
                ", numeroDocumento='" + numeroDocumento + '\'' +
                ", total='" + total + '\'' +
                ", principal='" + principal + '\'' +
                ", multa='" + multa + '\'' +
                ", juros='" + juros + '\'' +
                ", dataArrecadacao='" + dataArrecadacao + '\'' +
                '}';
    }
}
