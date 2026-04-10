package com.eleetricz.cashflow.service.fechamento;

import com.eleetricz.cashflow.entity.Competencia;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.service.CompetenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolve competências a partir de formatos usados nas telas/integrações.
 * Centraliza parsing e garante criação consistente.
 */
@Component
@RequiredArgsConstructor
public class CompetenciaResolver {

    private final CompetenciaService competenciaService;

    public Competencia resolverOuCriar(Empresa empresa, int mes, int ano) {
        if (empresa == null) {
            throw new IllegalArgumentException("Empresa não informada.");
        }
        validarMesAno(mes, ano);
        return competenciaService.buscarOuCriar(mes, ano, empresa);
    }

    /**
     * Aceita: "MM/YYYY" e "MM-YYYY" (com ou sem zero à esquerda no mês).
     */
    public Competencia resolverOuCriar(Empresa empresa, String competencia) {
        if (competencia == null || competencia.isBlank()) {
            throw new IllegalArgumentException("Competência não informada.");
        }

        String normalized = competencia.trim();
        String[] partes = normalized.contains("/") ? normalized.split("/") : normalized.split("-");
        if (partes.length != 2) {
            throw new IllegalArgumentException("Formato de competência inválido: " + competencia);
        }

        int mes = Integer.parseInt(partes[0]);
        int ano = Integer.parseInt(partes[1]);
        return resolverOuCriar(empresa, mes, ano);
    }

    private void validarMesAno(int mes, int ano) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("Mês inválido: " + mes);
        }
        if (ano < 1900 || ano > 3000) {
            throw new IllegalArgumentException("Ano inválido: " + ano);
        }
    }
}

