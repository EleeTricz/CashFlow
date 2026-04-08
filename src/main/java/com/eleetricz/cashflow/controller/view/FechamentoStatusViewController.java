package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.entity.FechamentoStatus;
import com.eleetricz.cashflow.entity.StatusTarefa;
import com.eleetricz.cashflow.repository.EmpresaRepository;
import com.eleetricz.cashflow.repository.FechamentoStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/fechamento-status")
@RequiredArgsConstructor
public class FechamentoStatusViewController {

    private final FechamentoStatusRepository fechamentoStatusRepository;
    private final EmpresaRepository empresaRepository;

    @GetMapping
    public String statusPanel(
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) Integer ano,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        // Busca paginada com filtros
        Page<FechamentoStatus> pagina = fechamentoStatusRepository.findByFiltros(empresaId, ano, pageable);

        // Gera lista dos últimos 5 anos (ex: 2026 até 2022)
        int anoAtual = LocalDate.now().getYear();
        List<Integer> ultimosAnos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ultimosAnos.add(anoAtual - i);
        }

        model.addAttribute("statusList", pagina.getContent());
        model.addAttribute("pagina", pagina);
        model.addAttribute("empresas", empresaRepository.findAll());
        model.addAttribute("ultimosAnos", ultimosAnos);

        // Mantém os filtros no Model para o HTML saber o que está selecionado
        model.addAttribute("empresaSelecionadaId", empresaId);
        model.addAttribute("anoSelecionado", ano);

        return "fechamento-status/panel";
    }

    @PostMapping("/atualizar")
    public String atualizarStatus(
            @RequestParam Long id,
            @RequestParam StatusTarefa fiscalStatus,
            @RequestParam StatusTarefa fgtsStatus,
            @RequestParam StatusTarefa inssStatus,
            @RequestParam StatusTarefa simplesStatus,
            @RequestParam StatusTarefa folhaStatus
    ) {
        FechamentoStatus status = fechamentoStatusRepository.findById(id).orElseThrow();
        status.setFiscalStatus(fiscalStatus);
        status.setFgtsStatus(fgtsStatus);
        status.setInssStatus(inssStatus);
        status.setSimplesStatus(simplesStatus);
        status.setFolhaStatus(folhaStatus);
        fechamentoStatusRepository.save(status);

        return "redirect:/fechamento-status";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        fechamentoStatusRepository.deleteById(id);
        return "redirect:/fechamento-status";
    }
}