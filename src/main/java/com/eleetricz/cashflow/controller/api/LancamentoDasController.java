package com.eleetricz.cashflow.controller.api;

import com.eleetricz.cashflow.dto.DasData;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.LancamentoDas;
import com.eleetricz.cashflow.pdfReader.PdfDasReader;
import com.eleetricz.cashflow.service.EmpresaService;
import com.eleetricz.cashflow.service.LancamentoDasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/lancamento-das")
@RequiredArgsConstructor
public class LancamentoDasController {

    private final LancamentoDasService dasService;
    private final PdfDasReader pdfDasReader;
    private final EmpresaService empresaService;

    private static final DateTimeFormatter BR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @GetMapping("/importar")
    public ResponseEntity<String> importarDas() {
        dasService.importarTodos();
        return ResponseEntity.ok("Importação de DAS concluída!");
    }

    @PostMapping("/upload/{empresaId}")
    public ResponseEntity<String> uploadPdfDas(
            @PathVariable Long empresaId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        Empresa empresa = empresaService.buscarPorId(empresaId);

        File tempFile = File.createTempFile("das-", ".pdf");
        file.transferTo(tempFile);

        int inseridos = 0;
        try {
            List<DasData> dadosExtraidos = pdfDasReader.extrairTodosDados(tempFile);

            for (DasData data : dadosExtraidos) {
                if (dasService.registroJaExiste(empresaId, data.competencia, data.numeroDocumento)) {
                    continue;
                }

                try {
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
                } catch (DateTimeParseException | NumberFormatException e) {
                    // Log ou capturar erro silenciosamente, se quiser ignorar páginas com erro
                    System.err.println("Erro ao processar página: " + e.getMessage());
                }
            }

        } finally {
            Files.deleteIfExists(tempFile.toPath());
        }

        return ResponseEntity.ok("PDF processado. Registros inseridos: " + inseridos);
    }
}
