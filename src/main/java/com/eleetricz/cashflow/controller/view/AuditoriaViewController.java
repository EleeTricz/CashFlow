package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.entity.LancamentoEsperado;
import com.eleetricz.cashflow.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/auditoriatf")
@RequiredArgsConstructor
public class AuditoriaViewController {

    private final AuditoriaService auditoriaService;

    @GetMapping("/empresa/{empresaId}/{competencia}")
    public String auditoria(@PathVariable Long empresaId, @PathVariable String competencia, Model model) {
        List<LancamentoEsperado> pendencias = auditoriaService.verificarPendencias(empresaId, competencia);
        model.addAttribute("pendencias", pendencias);
        model.addAttribute("empresaId", empresaId);
        model.addAttribute("competencia", competencia);
        return "auditoria/pendencias";
    }
}