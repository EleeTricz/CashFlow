package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.dto.FgtsData;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.pdfReader.PdfFgtsReader;
import com.eleetricz.cashflow.service.EmpresaService;
import com.eleetricz.cashflow.service.LancamentoFgtsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
        model.addAttribute("empresas", empresaService.listarTodas());
        return "fgts/upload";
    }

    @PostMapping("/fgts/upload-pdf")
    public String processarUploadPdf(
            @RequestParam("empresaId") Long empresaId,
            @RequestParam("files") MultipartFile[] files,
            Model model
    ) {
        int inseridos = 0;
        List<File> tempFiles = new ArrayList<>();

        try {
            List<FgtsData> dadosExtraidos = new ArrayList<>();

            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                File tempFile = File.createTempFile("fgts-", ".pdf");
                file.transferTo(tempFile);
                tempFiles.add(tempFile);

                dadosExtraidos.addAll(
                        pdfFgtsReader.extrairTodosDados(tempFile)
                );
            }

            inseridos = lancamentoFgtsService
                    .importarDadosPdfFgts(empresaId, dadosExtraidos);

            lancamentoFgtsService.importarTodos();

            model.addAttribute(
                    "mensagem",
                    "PDF importado com sucesso! Registros: " + inseridos
            );

        } catch (Exception e) {
            model.addAttribute(
                    "erro",
                    "Erro ao importar PDF: " + e.getMessage()
            );
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

    @PostMapping("/fgts/upload-txt")
    public String processarUploadTxt(
            @RequestParam("empresaId") Long empresaId,
            @RequestParam("files") MultipartFile[] files,
            Model model
    ) {
        try {
            int total = 0;

            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                total += lancamentoFgtsService
                        .importarFgtsExtratoTxt(file, empresaId);
            }

            model.addAttribute(
                    "mensagem",
                    "Extrato TXT importado com sucesso! Registros: " + total
            );

        } catch (Exception e) {
            model.addAttribute(
                    "erro",
                    "Erro ao importar TXT: " + e.getMessage()
            );
        }

        model.addAttribute("empresas", empresaService.listarTodas());
        return "fgts/upload";
    }
}