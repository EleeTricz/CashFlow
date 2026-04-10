package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.dto.PainelFechamentoDTO;
import com.eleetricz.cashflow.dto.PerfilFechamentoFormDTO;
import com.eleetricz.cashflow.entity.Descricao;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.FechamentoItemEsperado;
import com.eleetricz.cashflow.repository.DescricaoRepository;
import com.eleetricz.cashflow.repository.EmpresaRepository;
import com.eleetricz.cashflow.repository.FechamentoItemEsperadoRepository;
import com.eleetricz.cashflow.repository.LancamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PainelFechamentoServiceImpl implements PainelFechamentoService{

    private final LancamentoRepository lancamentoRepository;
    private final FechamentoItemEsperadoRepository itemRepository;
    private final EmpresaRepository empresaRepository;
    private final DescricaoRepository descricaoRepository;

    public List<PainelFechamentoDTO> gerarPainel(
            Long empresaId,
            Long competenciaId
    ) {

        Set<Long> descricoesLancadas =
                lancamentoRepository.buscarDescricoesLancadas(
                        empresaId,
                        competenciaId
                );

        List<FechamentoItemEsperado> esperados =
                itemRepository.findByEmpresaId(empresaId);

        return esperados.stream()
                .map(item -> new PainelFechamentoDTO(
                        item.getDescricao().getNome(),
                        descricoesLancadas.contains(
                                item.getDescricao().getId()
                        ) ? "CONCLUÍDO" : "PENDENTE"
                ))
                .toList();
    }

    public Integer calcularPercentual(
            List<PainelFechamentoDTO> painel
    ) {
        long total = painel.size();

        long concluidos = painel.stream()
                .filter(p -> p.getStatus().equals("CONCLUÍDO"))
                .count();

        return total == 0 ? 0 :
                (int) ((concluidos * 100) / total);
    }

    @Transactional
    public void salvarPerfil(PerfilFechamentoFormDTO dto) {

        if (dto.getEmpresaId() == null) {
            throw new IllegalArgumentException(
                    "Empresa não informada no formulário."
            );
        }

        Empresa empresa = empresaRepository
                .findById(dto.getEmpresaId())
                .orElseThrow(() ->
                        new RuntimeException("Empresa não encontrada.")
                );

        itemRepository.deleteByEmpresaId(empresa.getId());

        List<Descricao> descricoes =
                descricaoRepository.findAllById(
                        dto.getDescricoesSelecionadas()
                );

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
