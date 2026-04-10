package com.eleetricz.cashflow.service.fechamento;

import com.eleetricz.cashflow.dto.PendenciaPerfilDTO;
import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.repository.FechamentoItemEsperadoRepository;
import com.eleetricz.cashflow.repository.LancamentoRepository;
import com.eleetricz.cashflow.repository.PendenciaFechamentoRepository;
import com.eleetricz.cashflow.service.CompetenciaService;
import com.eleetricz.cashflow.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PendenciasPerfilService {

    private final FechamentoItemEsperadoRepository perfilRepository;
    private final LancamentoRepository lancamentoRepository;
    private final PendenciaFechamentoRepository pendenciaRepository;
    private final EmpresaService empresaService;
    private final CompetenciaService competenciaService;

    /**
     * Pendências da competência baseadas APENAS no Perfil da Empresa.
     * Por padrão, considera pendente quando não existe lançamento com a Descrição na competência.
     *
     * Regras de dispensa/ignorar:
     * - se houver registro em pendencia_fechamento com status DISPENSADA/IGNORADA para a Descrição, ela não aparece como pendência.
     */
    @Transactional(readOnly = true)
    public List<PendenciaPerfilDTO> listarPendencias(Long empresaId, Long competenciaId) {
        empresaService.buscarPorId(empresaId);
        Competencia competencia = competenciaService.buscarPorId(competenciaId);
        if (competencia == null) {
            throw new IllegalArgumentException("Competência inválida.");
        }

        Set<Long> descricoesLancadas = lancamentoRepository.buscarDescricoesLancadas(empresaId, competencia.getId());
        List<FechamentoItemEsperado> esperados = perfilRepository.findByEmpresaId(empresaId);

        Map<Long, StatusPendencia> overrides = carregarOverrides(empresaId, competencia.getId());

        List<PendenciaPerfilDTO> pendencias = new ArrayList<>();
        for (FechamentoItemEsperado item : esperados) {
            if (item.getDescricao() == null || item.getDescricao().getId() == null) continue;

            Long descricaoId = item.getDescricao().getId();
            boolean concluido = descricoesLancadas.contains(descricaoId);
            StatusPendencia base = concluido ? StatusPendencia.CONCLUIDO : StatusPendencia.PENDENTE;
            StatusPendencia override = overrides.get(descricaoId);

            StatusPendencia finalStatus = override != null ? override : base;

            if (finalStatus == StatusPendencia.PENDENTE) {
                pendencias.add(new PendenciaPerfilDTO(
                        descricaoId,
                        item.getDescricao().getNome(),
                        item.getDescricao().getTipo() == null ? "" : item.getDescricao().getTipo().name(),
                        finalStatus
                ));
            }
        }

        return pendencias;
    }

    @Transactional(readOnly = true)
    public List<PendenciaPerfilDTO> listarPendencias(Long empresaId, String competenciaKey) {
        Empresa empresa = empresaService.buscarPorId(empresaId);
        int[] mesAno = parseMesAno(competenciaKey);
        Competencia competencia = competenciaService.buscarOuCriar(mesAno[0], mesAno[1], empresa);
        return listarPendencias(empresaId, competencia.getId());
    }

    @Transactional
    public void dispensar(Long empresaId, Long competenciaId, Long descricaoId, StatusPendencia status) {
        if (status != StatusPendencia.DISPENSADA && status != StatusPendencia.IGNORADA) {
            throw new IllegalArgumentException("Status inválido para dispensa: " + status);
        }

        Empresa empresa = empresaService.buscarPorId(empresaId);
        Competencia competencia = competenciaService.buscarPorId(competenciaId);
        if (competencia == null) {
            throw new IllegalArgumentException("Competência inválida.");
        }

        PendenciaFechamento pendencia = pendenciaRepository
                .findByEmpresaIdAndCompetenciaIdAndDescricaoId(empresaId, competencia.getId(), descricaoId)
                .orElseGet(() -> PendenciaFechamento.builder()
                        .empresa(empresa)
                        .competencia(competencia)
                        .descricao(Descricao.builder().id(descricaoId).build())
                        .build());

        pendencia.setStatus(status);
        pendenciaRepository.save(pendencia);
    }

    @Transactional
    public void dispensar(Long empresaId, String competenciaKey, Long descricaoId, StatusPendencia status) {
        Empresa empresa = empresaService.buscarPorId(empresaId);
        int[] mesAno = parseMesAno(competenciaKey);
        Competencia competencia = competenciaService.buscarOuCriar(mesAno[0], mesAno[1], empresa);
        dispensar(empresaId, competencia.getId(), descricaoId, status);
    }

    @Transactional
    public void reativar(Long empresaId, Long competenciaId, Long descricaoId) {
        empresaService.buscarPorId(empresaId);
        Competencia competencia = competenciaService.buscarPorId(competenciaId);
        if (competencia == null) {
            throw new IllegalArgumentException("Competência inválida.");
        }

        pendenciaRepository.findByEmpresaIdAndCompetenciaIdAndDescricaoId(empresaId, competencia.getId(), descricaoId)
                .ifPresent(pendenciaRepository::delete);
    }

    @Transactional
    public void reativar(Long empresaId, String competenciaKey, Long descricaoId) {
        Empresa empresa = empresaService.buscarPorId(empresaId);
        int[] mesAno = parseMesAno(competenciaKey);
        Competencia competencia = competenciaService.buscarOuCriar(mesAno[0], mesAno[1], empresa);
        reativar(empresaId, competencia.getId(), descricaoId);
    }

    private Map<Long, StatusPendencia> carregarOverrides(Long empresaId, Long competenciaId) {
        List<PendenciaFechamento> registros = pendenciaRepository.findByEmpresaIdAndCompetenciaId(empresaId, competenciaId);
        Map<Long, StatusPendencia> map = new HashMap<>();
        for (PendenciaFechamento p : registros) {
            if (p.getDescricao() != null && p.getDescricao().getId() != null && p.getStatus() != null) {
                map.put(p.getDescricao().getId(), p.getStatus());
            }
        }
        return map;
    }

    private int[] parseMesAno(String competenciaKey) {
        if (competenciaKey == null || competenciaKey.isBlank()) {
            throw new IllegalArgumentException("Competência não informada.");
        }
        String normalized = competenciaKey.trim();
        String[] parts = normalized.contains("/") ? normalized.split("/") : normalized.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Formato de competência inválido: " + competenciaKey);
        }
        int mes = Integer.parseInt(parts[0]);
        int ano = Integer.parseInt(parts[1]);
        if (mes < 1 || mes > 12) throw new IllegalArgumentException("Mês inválido: " + mes);
        if (ano < 1900 || ano > 3000) throw new IllegalArgumentException("Ano inválido: " + ano);
        return new int[]{mes, ano};
    }

}

