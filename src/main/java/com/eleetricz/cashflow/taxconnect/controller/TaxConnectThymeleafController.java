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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            RedirectAttributes redirectAttributes) {

        try {
            importacaoService.importar(form);

            redirectAttributes.addFlashAttribute(
                    "mensagem",
                    "Importação realizada com sucesso!"
            );

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Erro ao importar: " + e.getMessage()
            );
        }

        return "redirect:/integracoes/taxconnect";
    }
}