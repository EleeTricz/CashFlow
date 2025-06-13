package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.dto.DarfData;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.pdfReader.PdfDarfReader;
import com.eleetricz.cashflow.service.EmpresaService;
import com.eleetricz.cashflow.service.LancamentoDarfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Controller
@RequestMapping("/importacaotf")
@RequiredArgsConstructor
public class LancamentoDarfViewController {
    private final EmpresaService empresaService;
    private final LancamentoDarfService lancamentoDarfService;
    private final PdfDarfReader pdfDarfReader;

    @GetMapping("darf")
    public String mostrarFormularioUpload(Model model) {
        List<Empresa> empresas = empresaService.listarTodas();
        model.addAttribute("empresas", empresas);
        return "darf/upload";
    }

    @PostMapping("/darf/upload")
    public String processarUpload(
            @RequestParam("empresaId") Long empresaId,
            @RequestParam("file") MultipartFile file,
            Model model
    ) {
        try {
            File tempFile = File.createTempFile("tf-", ".pdf");
            file.transferTo(tempFile);

            List<DarfData> dadosExtraidos = pdfDarfReader.extrairTodosDados(tempFile);
            int inseridos = lancamentoDarfService.importarDadosPdfDarf(empresaId, dadosExtraidos);
            Files.deleteIfExists(tempFile.toPath());

            lancamentoDarfService.importarTodos();
            model.addAttribute("mensagem", "Upload concluído! Registros inseridos: " + inseridos);
        }catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
        }catch (Exception e) {
            model.addAttribute("erro", "Erro ao processar o upload: " + e.getMessage());
        }

        model.addAttribute("empresas", empresaService.listarTodas());
        return "darf/upload";
    }

}
