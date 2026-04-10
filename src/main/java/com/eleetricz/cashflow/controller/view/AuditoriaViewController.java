package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.dto.PendenciaPerfilDTO;
import com.eleetricz.cashflow.entity.StatusPendencia;
import com.eleetricz.cashflow.service.fechamento.PendenciasPerfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/auditoriatf")
@RequiredArgsConstructor
public class AuditoriaViewController {

    private final PendenciasPerfilService pendenciasPerfilService;

    @GetMapping("/empresa/{empresaId}/{competencia}")
    public String auditoria(@PathVariable Long empresaId, @PathVariable String competencia, Model model) {
        List<PendenciaPerfilDTO> pendencias = pendenciasPerfilService.listarPendencias(empresaId, competencia);
        model.addAttribute("pendencias", pendencias);
        model.addAttribute("empresaId", empresaId);
        model.addAttribute("competencia", competencia);
        return "auditoria/pendencias";
    }

    @PostMapping("/empresa/{empresaId}/{competencia}/dispensar")
    public String dispensar(
            @PathVariable Long empresaId,
            @PathVariable String competencia,
            @RequestParam Long descricaoId,
            @RequestParam StatusPendencia acao
    ) {
        pendenciasPerfilService.dispensar(empresaId, competencia, descricaoId, acao);
        return "redirect:/auditoriatf/empresa/" + empresaId + "/" + competencia;
    }

    @PostMapping("/empresa/{empresaId}/{competencia}/reativar")
    public String reativar(
            @PathVariable Long empresaId,
            @PathVariable String competencia,
            @RequestParam Long descricaoId
    ) {
        pendenciasPerfilService.reativar(empresaId, competencia, descricaoId);
        return "redirect:/auditoriatf/empresa/" + empresaId + "/" + competencia;
    }
}