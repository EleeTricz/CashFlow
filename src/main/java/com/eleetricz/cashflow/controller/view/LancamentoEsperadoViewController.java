package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.LancamentoEsperado;
import com.eleetricz.cashflow.service.DescricaoService;
import com.eleetricz.cashflow.service.EmpresaService;
import com.eleetricz.cashflow.service.LancamentoEsperadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/lancamentos-esperadostf")
@RequiredArgsConstructor
public class LancamentoEsperadoViewController {

    private final LancamentoEsperadoService lancamentoEsperadoService;
    private final EmpresaService empresaService;
    private final DescricaoService descricaoService;

    @GetMapping("/empresa/{empresaId}")
    public String listar(@PathVariable Long empresaId, Model model) {
        List<LancamentoEsperado> esperados = lancamentoEsperadoService.listarPorEmpresa(empresaId);
        model.addAttribute("esperados", esperados);
        model.addAttribute("empresaId", empresaId);
        return "lancamentos-esperados/lista";
    }

    @GetMapping("/novo/{empresaId}")
    public String novo(@PathVariable Long empresaId, Model model) {
        LancamentoEsperado esperado = new LancamentoEsperado();
        Empresa empresa = empresaService.buscarPorId(empresaId);
        esperado.setEmpresa(empresa);
        model.addAttribute("esperado", esperado);
        model.addAttribute("descricoes", descricaoService.listarTodas());
        model.addAttribute("frequencias", com.eleetricz.cashflow.entity.Frequencia.values());
        return "lancamentos-esperados/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute LancamentoEsperado esperado) {
        lancamentoEsperadoService.salvar(esperado);
        return "redirect:/lancamentos-esperadostf/empresa/" + esperado.getEmpresa().getId();
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        LancamentoEsperado esperado = lancamentoEsperadoService.buscarPorId(id);
        Long empresaId = esperado.getEmpresa().getId();
        lancamentoEsperadoService.excluir(id);
        return "redirect:/lancamentos-esperados/empresatf/" + empresaId;
    }
}
