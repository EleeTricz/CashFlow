package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.entity.Descricao;
import com.eleetricz.cashflow.service.DescricaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/descricoestf")
public class DescricaoViewController {
    private final DescricaoService descricaoService;

    public DescricaoViewController(DescricaoService descricaoService) {
        this.descricaoService = descricaoService;
    }

    @GetMapping("/novo")
    public String novo(@RequestParam("empresaId") Long empresaId, @RequestParam("competenciaId") Long competenciaId,Model model) {
        model.addAttribute("descricao", new Descricao());
        model.addAttribute("competenciaId", competenciaId);
        model.addAttribute("empresaId", empresaId);
        return "descricao/formulario";
    }

    @PostMapping
    public String salvar(@ModelAttribute Descricao descricao, @RequestParam("empresaId") Long empresaId, @RequestParam("competenciaId") Long competenciaId) {
        descricaoService.salvar(descricao);
        return "redirect:/lancamentostf/novo/" + empresaId + "/" + competenciaId;
    }


}
