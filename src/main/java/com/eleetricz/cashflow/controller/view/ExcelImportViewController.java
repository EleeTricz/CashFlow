package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.service.EmpresaService;
import com.eleetricz.cashflow.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/importacaotf")
@RequiredArgsConstructor
public class ExcelImportViewController {

    private final ExcelImportService excelImportService;
    private final EmpresaService empresaService;

    @GetMapping("/excel")
    public String mostrarFormularioImportacao(Model model){
        List<Empresa> empresas = empresaService.listarTodas();
        model.addAttribute("empresas", empresas);
        return "importacao/formulario";
    }

    @PostMapping("/excel")
    public String importarPlanilhaExcel(
            @RequestParam("file")MultipartFile file,
            @RequestParam("empresaId") Long empresaId,
            RedirectAttributes redirectAttributes
    ){
        try{
            excelImportService.importarPlanilhaDoExcel(file, empresaId);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Importação realizada com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
        return "redirect:/importacaotf/" + empresaId;
    }

    @GetMapping("excel/{empresaId}")
    public String mostrarFormularioImportacaoComEmpresa(@PathVariable Long empresaId, Model model){
        List<Empresa> empresas = empresaService.listarTodas();
        model.addAttribute("empresas", empresas);
        model.addAttribute("empresaSelecionada", empresaId);
        return "importacao/formulario-empresa";
    }


}
