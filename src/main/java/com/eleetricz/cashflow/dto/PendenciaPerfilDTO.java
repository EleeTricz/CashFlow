package com.eleetricz.cashflow.dto;

import com.eleetricz.cashflow.entity.StatusPendencia;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PendenciaPerfilDTO {
    private final Long descricaoId;
    private final String descricaoNome;
    private final String tipo;
    private final StatusPendencia status;
}

