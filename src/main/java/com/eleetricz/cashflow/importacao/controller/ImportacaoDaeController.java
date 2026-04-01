package com.eleetricz.cashflow.importacao.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.importacao.dto.ImportacaoDaeDTO;
import com.eleetricz.cashflow.importacao.service.ImportacaoDaeService;
import com.eleetricz.cashflow.service.EmpresaService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/importacao-dae")
@RequiredArgsConstructor
public class ImportacaoDaeController {

    private final ImportacaoDaeService service;
    private final EmpresaService empresaService;

    @GetMapping
    public String telaUpload(Model model) {
        List<Empresa> empresas = empresaService.listarTodas();
        model.addAttribute("empresas", empresas);
        return "importacao-dae/upload";
    }

    @PostMapping("/preview")
    public String preview(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("empresaId") Long empresaId,
            Model model,
            HttpSession session) throws Exception {

        List<ImportacaoDaeDTO> registros = service.importar(arquivo);

        session.setAttribute("previewDae", registros);
        session.setAttribute("previewDaeEmpresaId", empresaId);

        model.addAttribute("registros", registros);
        model.addAttribute("empresaId", empresaId);

        return "importacao-dae/preview";
    }

    @PostMapping("/confirmar")
    public String confirmar(HttpSession session,
                            RedirectAttributes redirectAttributes) {

        Object preview = session.getAttribute("previewDae");
        List<ImportacaoDaeDTO> registros = preview instanceof List<?> list
                ? list.stream()
                    .filter(ImportacaoDaeDTO.class::isInstance)
                    .map(ImportacaoDaeDTO.class::cast)
                    .toList()
                : List.of();
        Long empresaId = (Long) session.getAttribute("previewDaeEmpresaId");

        service.salvar(registros, empresaId);

        session.removeAttribute("previewDae");
        session.removeAttribute("previewDaeEmpresaId");

        redirectAttributes.addFlashAttribute(
                "mensagem",
                "Importação realizada com sucesso!"
        );

        return "redirect:/importacao-dae";
    }
}
