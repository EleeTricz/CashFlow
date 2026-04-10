package com.eleetricz.cashflow.service.fechamento;

import com.eleetricz.cashflow.dto.PainelFechamentoDTO;
import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.repository.FechamentoItemEsperadoRepository;
import com.eleetricz.cashflow.repository.LancamentoRepository;
import com.eleetricz.cashflow.repository.PendenciaFechamentoRepository;
import com.eleetricz.cashflow.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FechamentoCentralService {

    private final LancamentoRepository lancamentoRepository;
    private final FechamentoItemEsperadoRepository fechamentoItemEsperadoRepository;
    private final PendenciaFechamentoRepository pendenciaFechamentoRepository;
    private final EmpresaService empresaService;
    private final CompetenciaResolver competenciaResolver;

    /**
     * Mantém compatibilidade com a tela atual `/fechamento/{empresaId}/{competenciaId}`.
     * Regras idênticas às atuais: concluído se houve lançamento com a descrição na competência.
     */
    @Transactional(readOnly = true)
    public List<PainelFechamentoDTO> gerarPainelPerfilEsperado(Long empresaId, Long competenciaId) {
        Set<Long> descricoesLancadas = lancamentoRepository.buscarDescricoesLancadas(empresaId, competenciaId);
        List<FechamentoItemEsperado> esperados = fechamentoItemEsperadoRepository.findByEmpresaId(empresaId);
        Map<Long, StatusPendencia> overrides = carregarOverrides(empresaId, competenciaId);

        return esperados.stream()
                .map(item -> {
                    Long descricaoId = item.getDescricao().getId();
                    boolean concluido = descricoesLancadas.contains(descricaoId);
                    StatusPendencia base = concluido ? StatusPendencia.CONCLUIDO : StatusPendencia.PENDENTE;
                    StatusPendencia override = overrides.get(descricaoId);
                    StatusPendencia finalStatus = override != null ? override : base;

                    String statusTexto = switch (finalStatus) {
                        case CONCLUIDO -> "CONCLUÍDO";
                        case DISPENSADA -> "DISPENSADO";
                        case IGNORADA -> "IGNORADO";
                        default -> "PENDENTE";
                    };

                    return new PainelFechamentoDTO(
                            descricaoId,
                            item.getDescricao().getNome(),
                            statusTexto
                    );
                })
                .toList();
    }

    private Map<Long, StatusPendencia> carregarOverrides(Long empresaId, Long competenciaId) {
        List<PendenciaFechamento> registros = pendenciaFechamentoRepository.findByEmpresaIdAndCompetenciaId(empresaId, competenciaId);
        Map<Long, StatusPendencia> map = new HashMap<>();
        for (PendenciaFechamento p : registros) {
            if (p.getDescricao() != null && p.getDescricao().getId() != null && p.getStatus() != null) {
                map.put(p.getDescricao().getId(), p.getStatus());
            }
        }
        return map;
    }
}

