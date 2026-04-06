package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.entity.FechamentoStatus;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.StatusTarefa;
import com.eleetricz.cashflow.repository.EmpresaRepository;
import com.eleetricz.cashflow.repository.FechamentoStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/fechamento-status")
@RequiredArgsConstructor
public class FechamentoStatusViewController {

    private final FechamentoStatusRepository fechamentoStatusRepository;
    private final EmpresaRepository empresaRepository;

    @GetMapping
    public String statusPanel(@RequestParam(required = false) Long empresaId, Model model) {
        List<FechamentoStatus> statusList;
        if (empresaId != null) {
            Optional<Empresa> empresa = empresaRepository.findById(empresaId);
            if (empresa.isPresent()) {
                statusList = fechamentoStatusRepository
                        .findByEmpresaOrderByCompetenciaAnoDescCompetenciaMesDesc(empresa.get());
                model.addAttribute("empresaSelecionada", empresa.get());
            } else {
                statusList = fechamentoStatusRepository.findAllByOrderByCompetenciaAnoDescCompetenciaMesDescEmpresaNomeAsc(); // Retorna todos ou manipula erro
                model.addAttribute("mensagemErro", "Empresa não encontrada.");
            }
        } else {
            statusList = fechamentoStatusRepository.findAllByOrderByCompetenciaAnoDescCompetenciaMesDescEmpresaNomeAsc();
        }

        model.addAttribute("statusList", statusList);
        model.addAttribute("empresas", empresaRepository.findAll()); // Para um dropdown de filtro
        return "fechamento-status/panel"; // Nome do seu template Thymeleaf
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
        FechamentoStatus status = fechamentoStatusRepository.findById(id)
                .orElseThrow();

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
