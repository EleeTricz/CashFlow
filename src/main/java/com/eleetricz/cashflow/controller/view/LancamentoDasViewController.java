package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.dto.DasData;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.pdfReader.PdfDasReader;
import com.eleetricz.cashflow.service.EmpresaService;
import com.eleetricz.cashflow.service.DocumentoFiscalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/importacaotf")
@RequiredArgsConstructor
public class LancamentoDasViewController {
    private final EmpresaService empresaService;
    private final DocumentoFiscalService documentoFiscalService;
    private final PdfDasReader pdfDasReader;

    private static final DateTimeFormatter BR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @GetMapping("/das")
    public String mostrarFormularioUpload(Model model) {
        List<Empresa> empresas = empresaService.listarTodas();
        model.addAttribute("empresas", empresas);
        return "das/upload";
    }

    @PostMapping("/das/upload")
    public String processarUpload(
            @RequestParam("empresaId") Long empresaId,
            @RequestParam("file") MultipartFile file,
            Model model
    ) {
        try {
            File tempFile = File.createTempFile("das-", ".pdf");
            file.transferTo(tempFile);

            int inseridos = 0;
            List<DasData> dadosExtraidos = pdfDasReader.extrairTodosDados(tempFile);

            inseridos = documentoFiscalService.importarDadosPdfDas(empresaId, dadosExtraidos);


            Files.deleteIfExists(tempFile.toPath());

            // ✅ Chamada automática da importação
            documentoFiscalService.importarTodos();


            model.addAttribute("mensagem", "Upload concluído! Registros inseridos: " + inseridos);
        }catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
        }catch (Exception e) {
            model.addAttribute("erro", "Erro ao processar o upload: " + e.getMessage());
        }

        model.addAttribute("empresas", empresaService.listarTodas());
        return "das/upload";
    }
}
