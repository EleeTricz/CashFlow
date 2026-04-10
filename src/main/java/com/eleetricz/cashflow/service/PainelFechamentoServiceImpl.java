package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.dto.PainelFechamentoDTO;
import com.eleetricz.cashflow.dto.PerfilFechamentoFormDTO;
import com.eleetricz.cashflow.entity.Descricao;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.FechamentoItemEsperado;
import com.eleetricz.cashflow.repository.DescricaoRepository;
import com.eleetricz.cashflow.repository.EmpresaRepository;
import com.eleetricz.cashflow.repository.FechamentoItemEsperadoRepository;
import com.eleetricz.cashflow.service.fechamento.FechamentoCentralService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PainelFechamentoServiceImpl implements PainelFechamentoService{

    private final FechamentoItemEsperadoRepository itemRepository;
    private final EmpresaRepository empresaRepository;
    private final DescricaoRepository descricaoRepository;
    private final FechamentoCentralService fechamentoCentralService;

    public List<PainelFechamentoDTO> gerarPainel(
            Long empresaId,
            Long competenciaId
    ) {
        // Delegado para o núcleo central de fechamento (mantém comportamento atual da tela).
        return fechamentoCentralService.gerarPainelPerfilEsperado(empresaId, competenciaId);
    }

    public Integer calcularPercentual(
            List<PainelFechamentoDTO> painel
    ) {
        long total = painel.size();

        long completos = painel.stream()
                .filter(p -> "CONCLUÍDO".equals(p.getStatus())
                        || "IGNORADO".equals(p.getStatus())
                        || "DISPENSADO".equals(p.getStatus()))
                .count();

        return total == 0 ? 0 :
                (int) ((completos * 100) / total);
    }

    @Transactional
    @SuppressWarnings("null")
    public void salvarPerfil(PerfilFechamentoFormDTO dto) {

        Long empresaId = Objects.requireNonNull(
                dto.getEmpresaId(),
                "Empresa não informada no formulário."
        );

        Empresa empresa = empresaRepository
                .findById(empresaId)
                .orElseThrow(() ->
                        new RuntimeException("Empresa não encontrada.")
                );

        itemRepository.deleteByEmpresaId(empresa.getId());

        List<Long> descricoesSelecionadas = Objects.requireNonNullElse(dto.getDescricoesSelecionadas(), List.<Long>of())
                .stream()
                .filter(Objects::nonNull)
                .toList();
        List<Descricao> descricoes = descricaoRepository.findAllById(descricoesSelecionadas);

        List<FechamentoItemEsperado> itens =
                descricoes.stream()
                        .map(desc -> FechamentoItemEsperado.builder()
                                .empresa(empresa)
                                .descricao(desc)
                                .frequencia(dto.getFrequencia())
                                .obrigatorio(true)
                                .build())
                        .toList();

        itemRepository.saveAll(itens);
    }
}
