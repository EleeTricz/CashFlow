package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.entity.StatusIntegracao;
import com.eleetricz.cashflow.entity.StatusTarefa;
import com.eleetricz.cashflow.repository.EmpresaRepository;
import com.eleetricz.cashflow.repository.StatusIntegracaoRepository;
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
@RequestMapping({"/status-integracao", "/fechamento-status"})
@RequiredArgsConstructor
public class StatusIntegracaoViewController {

    private final StatusIntegracaoRepository statusIntegracaoRepository;
    private final EmpresaRepository empresaRepository;

    @GetMapping
    public String statusPanel(
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) Integer ano,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        Page<StatusIntegracao> pagina = statusIntegracaoRepository.findByFiltros(empresaId, ano, pageable);

        int anoAtual = LocalDate.now().getYear();
        List<Integer> ultimosAnos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ultimosAnos.add(anoAtual - i);
        }

        model.addAttribute("statusList", pagina.getContent());
        model.addAttribute("pagina", pagina);
        model.addAttribute("empresas", empresaRepository.findAll());
        model.addAttribute("ultimosAnos", ultimosAnos);

        model.addAttribute("empresaSelecionadaId", empresaId);
        model.addAttribute("anoSelecionado", ano);

        // Mantemos o template antigo por compatibilidade, apenas com labels renomeados.
        return "fechamento-status/panel";
    }

    @PostMapping("/atualizar")
    @SuppressWarnings("null")
    public String atualizarStatus(
            @RequestParam Long id,
            @RequestParam StatusTarefa fiscalStatus,
            @RequestParam StatusTarefa fgtsStatus,
            @RequestParam StatusTarefa inssStatus,
            @RequestParam StatusTarefa simplesStatus,
            @RequestParam StatusTarefa folhaStatus
    ) {
        StatusIntegracao status = statusIntegracaoRepository.findById(id).orElseThrow();
        status.setFiscalStatus(fiscalStatus);
        status.setFgtsStatus(fgtsStatus);
        status.setInssStatus(inssStatus);
        status.setSimplesStatus(simplesStatus);
        status.setFolhaStatus(folhaStatus);
        statusIntegracaoRepository.save(status);

        return "redirect:/status-integracao";
    }

    @GetMapping("/excluir/{id}")
    @SuppressWarnings("null")
    public String excluir(@PathVariable Long id) {
        statusIntegracaoRepository.deleteById(id);
        return "redirect:/status-integracao";
    }
}

