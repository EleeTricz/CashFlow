package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.repository.*;
import com.eleetricz.cashflow.parcelamento.ParcelaService;
import com.eleetricz.cashflow.parcelamento.ParcelamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/parcelamentostf")
@RequiredArgsConstructor
public class ParcelamentoViewController {
    private final ParcelamentoRepository parcelamentoRepository;
    private final ParcelaRepository parcelaRepository;
    private final EmpresaRepository empresaRepository;
    private final DescricaoRepository descricaoRepository;
    private final ParcelamentoService parcelamentoService;
    private final ParcelaService parcelaService;

    // LISTAR PARCELAMENTOS
    @GetMapping("/todos")
    public String listar(Model model) {
        List<Parcelamento> parcelamentos = parcelamentoRepository.findAll();
        parcelamentoService.preencherTotalPago(parcelamentos);
        model.addAttribute("parcelamentos", parcelamentos);
        return "parcelamento/lista";
    }

    // FORMULÁRIO DE NOVO PARCELAMENTO
    @GetMapping("/novo")
    public String novoParcelamentoForm(Model model) {
        model.addAttribute("parcelamento", new Parcelamento());
        model.addAttribute("empresas", empresaRepository.findAll());
        model.addAttribute("descricoes", descricaoRepository.findAll());
        return "parcelamento/form";
    }

    // SALVAR NOVO PARCELAMENTO
    @PostMapping
    public String salvarParcelamento(@ModelAttribute Parcelamento parcelamento) {
        parcelamentoService.criarParcelamento(parcelamento);
        return "redirect:/parcelamentostf/todos";
    }

    // FORMULÁRIO DE PAGAMENTO
    @GetMapping("/pagar/{idParcela}")
    public String formPagamento(@PathVariable Long idParcela, Model model) {
        Parcela parcela = parcelaService.buscarPorId(idParcela);
        model.addAttribute("parcela", parcela);
        return "parcelamento/pagamento";
    }

    // PROCESSAR PAGAMENTO
    @PostMapping("/pagar")
    public String processarPagamento(
            @RequestParam Long parcelaId,
            @RequestParam BigDecimal valorBase,
            @RequestParam BigDecimal valorEncargos,
            @RequestParam String dataPagamento,
            @RequestParam String competencia,
            @RequestParam String competenciaReferida
    ) {
        parcelaService.registrarPagamentoComLancamento(
                parcelaId,
                valorBase,
                valorEncargos,
                LocalDate.parse(dataPagamento),
                competencia,
                competenciaReferida
        );
        return "redirect:/parcelamentostf/todos";
    }

    @GetMapping("/{id}")
    public String detalhar(@PathVariable Long id, Model model) {
        Parcelamento parcelamento = parcelamentoRepository.findById(id).orElseThrow();
        parcelamentoService.preencherTotalPago(List.of(parcelamento));
        model.addAttribute("parcelamento", parcelamento);
        return "parcelamento/detalhe";
    }

    @PostMapping("/excluir/{id}")
    public String excluirParcelamento(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        parcelamentoService.excluirParcelamento(id);
        redirectAttributes.addFlashAttribute("mensagem", "Parcelamento excluído com sucesso!");
        return "redirect:/parcelamentostf";
    }

    @GetMapping("/empresa/{empresaId}")
    public String listarPorEmpresa(@PathVariable Long empresaId, Model model) {
        Empresa empresa = empresaRepository.findById(empresaId).orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada"));
        List<Parcelamento> parcelamentos = parcelamentoRepository.findByEmpresaId(empresaId);
        parcelamentoService.preencherTotalPago(parcelamentos);
        model.addAttribute("parcelamentos", parcelamentos);
        model.addAttribute("empresa", empresa);
        return "parcelamento/lista-por-empresa";
    }



}
