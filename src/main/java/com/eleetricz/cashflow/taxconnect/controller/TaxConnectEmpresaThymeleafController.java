package com.eleetricz.cashflow.taxconnect.controller;

import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.service.EmpresaService;
import com.eleetricz.cashflow.taxconnect.dto.ImportacaoTaxConnectFormDTO;
import com.eleetricz.cashflow.taxconnect.service.ImportacaoTaxConnectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/integracoes/taxconnect/empresa")
@RequiredArgsConstructor
public class TaxConnectEmpresaThymeleafController {

    private final EmpresaService empresaService;
    private final ImportacaoTaxConnectService importacaoService;

    @GetMapping("/{empresaId}")
    public String telaImportacao(@PathVariable Long empresaId, Model model) {
        Empresa empresa = empresaService.buscarPorId(empresaId);
        if (empresa == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        ImportacaoTaxConnectFormDTO form = new ImportacaoTaxConnectFormDTO();
        form.setEmpresaId(empresaId);

        model.addAttribute("empresa", empresa);
        model.addAttribute("form", form);

        return "integracoes/taxconnect-importacao-empresa";
    }

    @PostMapping("/{empresaId}/importar")
    public String importar(
            @PathVariable Long empresaId,
            @ModelAttribute("form") ImportacaoTaxConnectFormDTO form,
            RedirectAttributes redirectAttributes) {

        form.setEmpresaId(empresaId);

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

        return "redirect:/integracoes/taxconnect/empresa/" + empresaId;
    }
}
