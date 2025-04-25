package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/lancamentostf")
public class LancamentoViewController {

    @Autowired private LancamentoService lancamentoService;
    @Autowired private EmpresaService empresaService;
    @Autowired private CompetenciaService competenciaService;
    @Autowired private DescricaoService descricaoService;
    @Autowired private UsuarioService usuarioService;


    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Lancamento lancamento) {
        lancamentoService.salvar(lancamento);
        return "redirect:/lancamentos/empresa/" + lancamento.getEmpresa().getId();
    }


    @GetMapping("/empresa/{empresaId}")
    public String listarPorEmpresa(@PathVariable Long empresaId, Model model) {
        Empresa empresa = empresaService.buscarPorId(empresaId);
        List<Lancamento> lancamentos = lancamentoService.listarPorEmpresa(empresa);
        BigDecimal saldo = lancamentoService.calcularSaldoPorEmpresa(empresa);

        model.addAttribute("empresa", empresa);
        model.addAttribute("lancamentos", lancamentos);
        model.addAttribute("saldo", saldo);

        return "lancamentos/listar-por-empresa";
    }

    @GetMapping("/competencia/{empresaId}/{competenciaId}")
    public String listarPorCompetencia(@PathVariable Long empresaId,
                                       @PathVariable Long competenciaId,
                                       Model model) {

        Empresa empresa = empresaService.buscarPorId(empresaId);
        Competencia competencia = competenciaService.buscarPorId(competenciaId);

        List<Lancamento> lancamentos = lancamentoService.listarPorCompetencia(empresa, competencia);
        Map<String, BigDecimal> saldoPorMes = lancamentoService.calcularSaldoPorCompetencia(empresa);

        model.addAttribute("empresa", empresa);
        model.addAttribute("competencia", competencia);
        model.addAttribute("lancamentos", lancamentos);
        model.addAttribute("saldo", saldoPorMes.get(competencia.getMes() + "/" + competencia.getAno()));

        return "lancamentos/listar-por-competencia";
    }

    @GetMapping("/novo/{empresaId}")
    public String novoLancamento(@PathVariable Long empresaId, Model model) {
        Empresa empresa = empresaService.buscarPorId(empresaId);
        List<Competencia> competencias = competenciaService.listarPorEmpresa(empresa);

        List<Descricao> descricoes = descricaoService.listarTodas();  // Buscar todas as descrições
        model.addAttribute("descricoes", descricoes);  // Lista de descrições

        List<Usuario> usuarios = usuarioService.listarTodos(); // Buscar todos os usuarios
        model.addAttribute("usuarios", usuarios); // Listar todos os usuarios

        Lancamento lancamento = new Lancamento();
        lancamento.setEmpresa(empresa);

        model.addAttribute("empresa", empresa);
        model.addAttribute("lancamento", lancamento);
        model.addAttribute("competencias", competencias);

        return "lancamentos/formulario";
    }


}
