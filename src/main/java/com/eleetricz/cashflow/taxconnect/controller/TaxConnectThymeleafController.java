package com.eleetricz.cashflow.taxconnect.controller;

import com.eleetricz.cashflow.service.EmpresaService;
import com.eleetricz.cashflow.taxconnect.dto.ImportacaoTaxConnectFormDTO;
import com.eleetricz.cashflow.taxconnect.service.ImportacaoTaxConnectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/integracoes/taxconnect")
@RequiredArgsConstructor
public class TaxConnectThymeleafController {

    private final EmpresaService empresaService;
    private final ImportacaoTaxConnectService importacaoService;

    @GetMapping
    public String telaImportacao(Model model) {

        model.addAttribute("empresas", empresaService.listarTodas());
        model.addAttribute("form", new ImportacaoTaxConnectFormDTO());

        return "integracoes/taxconnect-importacao";
    }

    @PostMapping("/importar")
    public String importar(
            @ModelAttribute("form") ImportacaoTaxConnectFormDTO form,
            Model model) {

        importacaoService.importar(form);

        model.addAttribute("sucesso", "Importação realizada com sucesso!");

        return "redirect:/integracoes/taxconnect";
    }
}