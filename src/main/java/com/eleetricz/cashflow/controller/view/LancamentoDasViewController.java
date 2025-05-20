package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.dto.DasData;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.LancamentoDas;
import com.eleetricz.cashflow.pdfReader.PdfDasReader;
import com.eleetricz.cashflow.service.EmpresaService;
import com.eleetricz.cashflow.service.LancamentoDasService;
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
    private final LancamentoDasService dasService;
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
            Empresa empresa = empresaService.buscarPorId(empresaId);
            File tempFile = File.createTempFile("das-", ".pdf");
            file.transferTo(tempFile);

            int inseridos = 0;
            List<DasData> dadosExtraidos = pdfDasReader.extrairTodosDados(tempFile);

            for (DasData data : dadosExtraidos) {
                if (!dasService.registroJaExiste(empresaId, data.competencia, data.numeroDocumento)) {
                    LancamentoDas lanc = new LancamentoDas();
                    lanc.setEmpresaId(empresa);
                    lanc.setCompetencia(data.competencia);
                    lanc.setNumeroDocumento(data.numeroDocumento);
                    lanc.setDataVencimento(LocalDate.parse(data.dataVencimento, BR_DATE));
                    lanc.setDataArrecadacao(LocalDate.parse(data.dataArrecadacao, BR_DATE));
                    lanc.setPrincipal(data.principal);
                    lanc.setMulta(data.multa);
                    lanc.setJuros(data.juros);
                    lanc.setTotal(data.total);

                    BigDecimal encargos = new BigDecimal(data.juros).add(new BigDecimal(data.multa));
                    lanc.setEncargosDas(encargos.toString());

                    dasService.salvar(lanc);
                    inseridos++;
                }
            }

            Files.deleteIfExists(tempFile.toPath());

            // ✅ Chamada automática da importação
            dasService.importarTodos();


            model.addAttribute("mensagem", "Upload concluído! Registros inseridos: " + inseridos);
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao processar o upload: " + e.getMessage());
        }

        model.addAttribute("empresas", empresaService.listarTodas());
        return "das/upload";
    }
}
