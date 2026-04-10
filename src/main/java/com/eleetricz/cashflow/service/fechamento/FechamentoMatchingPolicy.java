package com.eleetricz.cashflow.service.fechamento;

public enum FechamentoMatchingPolicy {
    /**
     * Mantém o comportamento atual do painel (/fechamento): concluído se houve lançamento com a descrição na competência.
     */
    BY_DESCRICAO_ID,
    /**
     * Mantém o comportamento atual da auditoria (/auditoriatf): match por descrição + tipo.
     */
    BY_DESCRICAO_ID_AND_TIPO
}

