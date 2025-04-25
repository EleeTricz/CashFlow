package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.service.CompetenciaService;
import com.eleetricz.cashflow.service.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/competenciastf")
public class CompetenciaViewController {
    @Autowired private CompetenciaService competenciaService;
    @Autowired private EmpresaService empresaService;

    @PostMapping("/nova")
    public String criar(@RequestParam Integer mes, @RequestParam Integer ano, @RequestParam Long empresaId) {
        Empresa empresa = empresaService.buscarPorId(empresaId);
        competenciaService.buscarOuCriar(mes, ano, empresa);
        return "redirect:/empresastf/" + empresaId;
    }
}
