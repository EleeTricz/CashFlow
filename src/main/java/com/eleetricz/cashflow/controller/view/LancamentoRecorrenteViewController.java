package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.dto.LancamentoRecorrenteDTO;
import com.eleetricz.cashflow.service.DescricaoService;
import com.eleetricz.cashflow.service.EmpresaService;
import com.eleetricz.cashflow.service.LancamentoService;
import com.eleetricz.cashflow.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/lancamentostf/recorrentes")
public class LancamentoRecorrenteViewController {

    @Autowired private LancamentoService lancamentoService;
    @Autowired private EmpresaService empresaService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private DescricaoService descricaoService;

    @GetMapping
    public String mostrarFormulario(@RequestParam("empresaId") Long empresaId, Model model) {

        LancamentoRecorrenteDTO dto = new LancamentoRecorrenteDTO();
        dto.setEmpresaId(empresaId);

        model.addAttribute("dto", dto);

        model.addAttribute("empresas", empresaService.listarTodas());
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("descricoes", descricaoService.listarTodas());
        model.addAttribute("empresaId", empresaId); // para o botão "Cancelar"
        return "lancamentos/recorrente-form";
    }

    @PostMapping
    public String processarFormulario(@ModelAttribute("dto") LancamentoRecorrenteDTO dto, Model model) {
        lancamentoService.gerarLancamentosRecorrentes(dto);
        model.addAttribute("mensagem", "Lançamentos gerados com sucesso!");
        return "redirect:/lancamentostf/recorrentes?empresaId=" + dto.getEmpresaId();
    }

}
