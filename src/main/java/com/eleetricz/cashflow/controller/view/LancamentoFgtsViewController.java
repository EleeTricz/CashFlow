package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.dto.FgtsData;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.pdfReader.PdfFgtsReader;
import com.eleetricz.cashflow.service.EmpresaService;
import com.eleetricz.cashflow.service.LancamentoFgtsService;
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
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/importacaotf")
@RequiredArgsConstructor
public class LancamentoFgtsViewController {

    private final EmpresaService empresaService;
    private final LancamentoFgtsService lancamentoFgtsService;
    private final PdfFgtsReader pdfFgtsReader;

    @GetMapping("/fgts")
    public String mostrarFormularioUpload(Model model) {
        List<Empresa> empresas = empresaService.listarTodas();
        model.addAttribute("empresas", empresas);
        return "fgts/upload";
    }

    @PostMapping("/fgts/upload")
    public String processarUpload(
            @RequestParam("empresaId") Long empresaId,
            @RequestParam("files") MultipartFile[] files,
            Model model
    ) {
        int inseridos = 0;
        List<File> tempFiles = new ArrayList<>();

        try {
            if (files == null || files.length == 0) {
                throw new IllegalArgumentException("Selecione ao menos um PDF de FGTS.");
            }

            List<FgtsData> dadosExtraidos = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                File tempFile = File.createTempFile("fgts-", ".pdf");
                file.transferTo(tempFile);
                tempFiles.add(tempFile);

                dadosExtraidos.addAll(pdfFgtsReader.extrairTodosDados(tempFile));
            }

            inseridos = lancamentoFgtsService.importarDadosPdfFgts(empresaId, dadosExtraidos);
            lancamentoFgtsService.importarTodos();

            model.addAttribute("mensagem", "Upload concluído! Registros inseridos: " + inseridos);
        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao processar o upload: " + e.getMessage());
        } finally {
            for (File tempFile : tempFiles) {
                try {
                    Files.deleteIfExists(tempFile.toPath());
                } catch (Exception ignored) {
                }
            }
        }

        model.addAttribute("empresas", empresaService.listarTodas());
        return "fgts/upload";
    }
}
