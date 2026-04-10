package com.eleetricz.cashflow.dto;

import com.eleetricz.cashflow.entity.FrequenciaFechamento;
import lombok.*;

import java.util.List;

@Getter
@Setter
public class PerfilFechamentoFormDTO {

    private Long empresaId;

    private List<Long> descricoesSelecionadas;

    private FrequenciaFechamento frequencia;
}
