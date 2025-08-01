package com.eleetricz.cashflow.importacao.controller;

import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.importacao.service.ImportacaoFolhaService;
import com.eleetricz.cashflow.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/importacaotf")
public class ImportacaoFolhaViewController {

    private final ImportacaoFolhaService folhaService;
    private final EmpresaService empresaService;

    @GetMapping("/folha")
    public String formImportacao(Model model) {
        List<Empresa> empresas = empresaService.listarTodas();

        model.addAttribute("empresas", empresas);
        return "importacao/folha/formulario";
    }

    @PostMapping("/folha")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam("empresaId") Long empresaId,
                         Model model) {
        List<Empresa> empresas = empresaService.listarTodas();
        model.addAttribute("empresas", empresas);

        try {
            folhaService.importarFolha(file, empresaId);
            model.addAttribute("mensagem", "Importação realizada com sucesso.");
        } catch (Exception e) {
            model.addAttribute("mensagem", "Erro: " + e.getMessage());
        }
        return "importacao/folha/formulario";
    }

}
