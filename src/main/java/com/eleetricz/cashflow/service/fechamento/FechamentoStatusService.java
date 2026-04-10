package com.eleetricz.cashflow.service.fechamento;

import com.eleetricz.cashflow.entity.Competencia;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.StatusIntegracao;
import com.eleetricz.cashflow.entity.StatusTarefa;
import com.eleetricz.cashflow.repository.StatusIntegracaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class FechamentoStatusService {

    private final StatusIntegracaoRepository statusIntegracaoRepository;
    private final CompetenciaResolver competenciaResolver;

    @Transactional
    @SuppressWarnings("null")
    public StatusIntegracao getOrCreate(Empresa empresa, Competencia competencia) {
        return statusIntegracaoRepository
                .findByEmpresaAndCompetencia(empresa, competencia)
                .orElseGet(() -> statusIntegracaoRepository.save(
                        StatusIntegracao.builder()
                                .empresa(empresa)
                                .competencia(competencia)
                                .build()
                ));
    }

    /**
     * Atualiza status por competência (string) de forma centralizada.
     * Usado por integrações/importações para evitar duplicação e regras espalhadas.
     */
    @Transactional
    @SuppressWarnings("null")
    public StatusIntegracao atualizarStatus(
            Empresa empresa,
            String competenciaString,
            Consumer<StatusIntegracao> updater
    ) {
        Competencia competencia = competenciaResolver.resolverOuCriar(empresa, competenciaString);
        StatusIntegracao status = getOrCreate(empresa, competencia);
        updater.accept(status);
        return statusIntegracaoRepository.save(status);
    }

    @Transactional
    public StatusIntegracao marcarConcluido(Empresa empresa, String competenciaString, Consumer<StatusIntegracao> fieldSetter) {
        return atualizarStatus(empresa, competenciaString, s -> {
            fieldSetter.accept(s);
            // garante um estado consistente se algum campo vier nulo por registros antigos
            if (s.getFiscalStatus() == null) s.setFiscalStatus(StatusTarefa.PENDENTE);
            if (s.getFgtsStatus() == null) s.setFgtsStatus(StatusTarefa.PENDENTE);
            if (s.getInssStatus() == null) s.setInssStatus(StatusTarefa.PENDENTE);
            if (s.getSimplesStatus() == null) s.setSimplesStatus(StatusTarefa.PENDENTE);
            if (s.getFolhaStatus() == null) s.setFolhaStatus(StatusTarefa.PENDENTE);
        });
    }
}

