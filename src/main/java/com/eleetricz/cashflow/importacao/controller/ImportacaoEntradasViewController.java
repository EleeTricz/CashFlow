package com.eleetricz.cashflow.importacao.controller;


import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.importacao.service.EntradasService;
import com.eleetricz.cashflow.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping("/importacaotf")
@RequiredArgsConstructor
public class ImportacaoEntradasViewController {

    private final EntradasService entradasService;
    private final EmpresaRepository empresaRepository;

    @GetMapping("/entradas")
    public String form(Model model) {
        model.addAttribute("empresas", empresaRepository.findAll());
        return "importacao/entradas/upload";
    }

    @PostMapping("/entradas")
    public String importar(@RequestParam("arquivo") MultipartFile arquivo,
                           @RequestParam("empresaId") Long empresaId,
                           @RequestParam("anoBase") int anoBase,
                           Model model) throws IOException {

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada"));

        File tempFile = File.createTempFile("importacao-entradas", ".txt");
        arquivo.transferTo(tempFile);

        entradasService.importarLancamentosDeExtrato(tempFile, empresa, anoBase);

        model.addAttribute("mensagem", "Importação realizada com sucesso.");
        model.addAttribute("empresas", empresaRepository.findAll());
        return "importacao/entradas/upload";
    }

}
