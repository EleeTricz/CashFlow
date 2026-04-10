package com.eleetricz.cashflow.service.fechamento;

import com.eleetricz.cashflow.dto.PainelFechamentoDTO;
import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.repository.FechamentoItemEsperadoRepository;
import com.eleetricz.cashflow.repository.LancamentoRepository;
import com.eleetricz.cashflow.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FechamentoCentralService {

    private final LancamentoRepository lancamentoRepository;
    private final FechamentoItemEsperadoRepository fechamentoItemEsperadoRepository;
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

        return esperados.stream()
                .map(item -> new PainelFechamentoDTO(
                        item.getDescricao().getNome(),
                        descricoesLancadas.contains(item.getDescricao().getId()) ? "CONCLUÍDO" : "PENDENTE"
                ))
                .toList();
    }
}

