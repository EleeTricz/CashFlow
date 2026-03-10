package com.eleetricz.cashflow.taxconnect.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ConsultaPagamentosRequestDTO {

    private String contribuinteNumero;

    private IntervaloDataArrecadacao intervaloDataArrecadacao;

    private Integer pagina = 0;
    private Integer tamanho = 100;

    @Data
    public static class IntervaloDataArrecadacao {

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate dataInicial;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate dataFinal;
    }
}
