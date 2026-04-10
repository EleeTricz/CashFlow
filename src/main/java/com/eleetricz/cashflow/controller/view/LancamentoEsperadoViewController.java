package com.eleetricz.cashflow.controller.view;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/lancamentos-esperadostf")
@RequiredArgsConstructor
public class LancamentoEsperadoViewController {

    @GetMapping("/empresa/{empresaId}")
    public String listar(@PathVariable Long empresaId, Model model) {
        return "redirect:/fechamento/perfil/" + empresaId;
    }

    @GetMapping("/novo/{empresaId}")
    public String novo(@PathVariable Long empresaId, Model model) {
        return "redirect:/fechamento/perfil/" + empresaId;
    }

    @PostMapping("/salvar")
    public String salvar(@RequestParam(required = false) Long empresaId) {
        if (empresaId == null) return "redirect:/dashboard/fechamento";
        return "redirect:/fechamento/perfil/" + empresaId;
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        return "redirect:/dashboard/fechamento";
    }
}
