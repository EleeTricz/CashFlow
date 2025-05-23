package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.exportacao.service.ExportacaoService;
import com.eleetricz.cashflow.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/exportacaotf")
public class ExportacaoViewController {

    private final ExportacaoService exportacaoService;
    private final EmpresaService empresaService;

    @GetMapping
    public String home(){
        return "exportar/home";
    }

    @GetMapping("/txt")
    public String exibirFormularioExportacao(Model model) {
        model.addAttribute("empresas", empresaService.listarTodas());
        return "exportar/form-exportacao";
    }

    @GetMapping("/exportar")
    public ResponseEntity<Resource> exportarLancamentos(
            @RequestParam Long empresaId,
            @RequestParam int mesInicial,
            @RequestParam int anoInicial,
            @RequestParam int mesFinal,
            @RequestParam int anoFinal) throws IOException {

        File txt = exportacaoService.exportarLancamentosParaTxt(empresaId, mesInicial, anoInicial, mesFinal, anoFinal);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(txt));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=exportacao.txt")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(txt.length())
                .body(resource);
    }
}
