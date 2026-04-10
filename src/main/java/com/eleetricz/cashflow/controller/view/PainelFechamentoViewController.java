package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.dto.PainelFechamentoDTO;
import com.eleetricz.cashflow.dto.PerfilFechamentoFormDTO;
import com.eleetricz.cashflow.entity.FrequenciaFechamento;
import com.eleetricz.cashflow.repository.DescricaoRepository;
import com.eleetricz.cashflow.repository.EmpresaRepository;
import com.eleetricz.cashflow.service.PainelFechamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/fechamento")
@RequiredArgsConstructor
public class PainelFechamentoViewController {

    private final PainelFechamentoService service;
    private final EmpresaRepository empresaRepository;
    private final DescricaoRepository descricaoRepository;

    @GetMapping("/{empresaId}/{competenciaId}")
    public String painel(
            @PathVariable Long empresaId,
            @PathVariable Long competenciaId,
            Model model
    ) {

        List<PainelFechamentoDTO> painel =
                service.gerarPainel(empresaId, competenciaId);

        Integer percentual =
                service.calcularPercentual(painel);

        model.addAttribute("painel", painel);
        model.addAttribute("percentual", percentual);

        return "fechamento/painel";
    }

    @GetMapping("/perfil/{empresaId}")
    public String telaConfig(
            @PathVariable Long empresaId,
            Model model
    ) {
        model.addAttribute(
                "empresa",
                empresaRepository.findById(empresaId).orElseThrow()
        );

        model.addAttribute(
                "descricoes",
                descricaoRepository.findAll()
        );

        model.addAttribute(
                "form",
                new PerfilFechamentoFormDTO()
        );

        return "fechamento/configurar-perfil";
    }

    @PostMapping("/perfil/salvar")
    public String salvar(
            @RequestParam Long empresaId,
            @RequestParam List<Long> descricoesSelecionadas,
            @RequestParam FrequenciaFechamento frequencia
    ) {

        PerfilFechamentoFormDTO dto = new PerfilFechamentoFormDTO();
        dto.setEmpresaId(empresaId);
        dto.setDescricoesSelecionadas(descricoesSelecionadas);
        dto.setFrequencia(frequencia);

        service.salvarPerfil(dto);

        return "redirect:/fechamento/perfil/" + empresaId;
    }
}
