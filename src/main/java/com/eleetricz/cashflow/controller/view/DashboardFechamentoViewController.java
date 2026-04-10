package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.dto.PainelFechamentoDTO;
import com.eleetricz.cashflow.dto.PendenciaPerfilDTO;
import com.eleetricz.cashflow.entity.Competencia;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.StatusIntegracao;
import com.eleetricz.cashflow.repository.EmpresaRepository;
import com.eleetricz.cashflow.repository.StatusIntegracaoRepository;
import com.eleetricz.cashflow.service.CompetenciaService;
import com.eleetricz.cashflow.service.fechamento.FechamentoCentralService;
import com.eleetricz.cashflow.service.fechamento.PendenciasPerfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/dashboard/fechamento")
@RequiredArgsConstructor
public class DashboardFechamentoViewController {

    private final EmpresaRepository empresaRepository;
    private final CompetenciaService competenciaService;
    private final StatusIntegracaoRepository statusIntegracaoRepository;
    private final FechamentoCentralService fechamentoCentralService;
    private final PendenciasPerfilService pendenciasPerfilService;

    @GetMapping
    public String dashboard(
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) Long competenciaId,
            Model model
    ) {
        model.addAttribute("empresas", empresaRepository.findAll());
        model.addAttribute("empresaSelecionadaId", empresaId);
        model.addAttribute("competenciaSelecionadaId", competenciaId);

        if (empresaId == null) {
            model.addAttribute("competencias", Collections.emptyList());
            model.addAttribute("painelPerfil", Collections.emptyList());
            model.addAttribute("percentualPerfil", 0);
            model.addAttribute("pendenciasCompetencia", Collections.emptyList());
            model.addAttribute("fechamentoStatus", null);
            model.addAttribute("competenciaLabel", "");
            model.addAttribute("competenciaKey", "");
            return "dashboard/fechamento";
        }

        Empresa empresa = empresaRepository.findById(empresaId).orElseThrow();
        List<Competencia> competencias = competenciaService.listarPorEmpresaCronologico(empresa);
        model.addAttribute("competencias", competencias);

        if (competenciaId == null) {
            model.addAttribute("painelPerfil", Collections.emptyList());
            model.addAttribute("percentualPerfil", 0);
            model.addAttribute("pendenciasCompetencia", Collections.emptyList());
            model.addAttribute("fechamentoStatus", null);
            model.addAttribute("competenciaLabel", "");
            model.addAttribute("competenciaKey", "");
            return "dashboard/fechamento";
        }

        Competencia competencia = competenciaService.buscarPorId(competenciaId);
        if (competencia == null) {
            model.addAttribute("painelPerfil", Collections.emptyList());
            model.addAttribute("percentualPerfil", 0);
            model.addAttribute("pendenciasCompetencia", Collections.emptyList());
            model.addAttribute("fechamentoStatus", null);
            model.addAttribute("competenciaLabel", "");
            model.addAttribute("competenciaKey", "");
            return "dashboard/fechamento";
        }

        String competenciaLabel = String.format("%02d/%d", competencia.getMes(), competencia.getAno());
        String competenciaKey = String.format("%02d-%d", competencia.getMes(), competencia.getAno()); // compatível com rota da auditoria

        List<PainelFechamentoDTO> painelPerfil = fechamentoCentralService.gerarPainelPerfilEsperado(empresaId, competenciaId);
        int percentualPerfil = calcularPercentual(painelPerfil);
        List<PendenciaPerfilDTO> pendenciasCompetencia = pendenciasPerfilService.listarPendencias(empresaId, competenciaId);

        Optional<StatusIntegracao> fechamentoStatus = statusIntegracaoRepository.findByEmpresaAndCompetencia(empresa, competencia);

        model.addAttribute("competenciaLabel", competenciaLabel);
        model.addAttribute("competenciaKey", competenciaKey);
        model.addAttribute("painelPerfil", painelPerfil);
        model.addAttribute("percentualPerfil", percentualPerfil);
        model.addAttribute("pendenciasCompetencia", pendenciasCompetencia);
        model.addAttribute("fechamentoStatus", fechamentoStatus.orElse(null));

        return "dashboard/fechamento";
    }

    private int calcularPercentual(List<PainelFechamentoDTO> painel) {
        if (painel == null || painel.isEmpty()) return 0;
        long concluidos = painel.stream().filter(p -> "CONCLUÍDO".equals(p.getStatus())).count();
        return (int) ((concluidos * 100) / painel.size());
    }
}

