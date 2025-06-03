package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.entity.Competencia;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.TipoLancamento;
import com.eleetricz.cashflow.entity.Usuario;
import com.eleetricz.cashflow.service.CompetenciaService;
import com.eleetricz.cashflow.service.EmpresaService;
import com.eleetricz.cashflow.service.LancamentoService;
import com.eleetricz.cashflow.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/empresastf")
public class EmpresaViewController {
    final private EmpresaService empresaService;
    final private UsuarioService usuarioService;
    final private CompetenciaService competenciaService;
    final private LancamentoService lancamentoService;

    public EmpresaViewController(EmpresaService empresaService, UsuarioService usuarioService, CompetenciaService competenciaService, LancamentoService lancamentoService) {
        this.empresaService = empresaService;
        this.usuarioService = usuarioService;
        this.competenciaService = competenciaService;
        this.lancamentoService = lancamentoService;
    }

    @GetMapping("/usuario")
    public String listarPorUsuario(@RequestParam("usuarioId") Long usuarioId, Model model) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        List<Empresa> empresas = empresaService.listarPorUsuario(usuario);
        model.addAttribute("empresas", empresas);
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "painel";
    }

    @GetMapping("/todas")
    public String listarTodas(Model model) {
        List<Empresa> empresas = empresaService.listarTodas();
        model.addAttribute("empresas", empresas);
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "painel";  // Nome da sua página Thymeleaf
    }

    @GetMapping("/nova")
    public String novaEmpresaForm(Model model) {
        model.addAttribute("empresa", new Empresa());
        model.addAttribute("usuarios", usuarioService.listarTodos()); // para o select
        return "empresa-form"; // esse é o nome do HTML com o formulário
    }

    @GetMapping("/{id}")
    public String visualizarEmpresa(@PathVariable Long id,
                                    @RequestParam(required = false) Integer ano,
                                    @RequestParam(required = false) String busca,
                                    Model model) {
        Empresa empresa = empresaService.buscarPorId(id);
        List<Competencia> competencias = competenciaService.listarPorEmpresa(empresa);

        // Filtrar por ano
        if (ano != null) {
            competencias.removeIf(c -> c.getAno() != ano);
        }

        // Busca textual no mes/ano
        if (busca != null && !busca.isBlank()) {
            String buscaLower = busca.toLowerCase();
            competencias.removeIf(c ->
                    !(c.getMes() + "/" + c.getAno()).toLowerCase().contains(buscaLower));
        }


        // Ordenar por ano e depois por mês
        competencias.sort(Comparator
                .comparing(Competencia::getAno)
                .thenComparing(Competencia::getMes));

        BigDecimal saldoTotal = lancamentoService.calcularSaldoPorEmpresa(empresa);
        Map<String, BigDecimal> saldoPorMes = lancamentoService.calcularSaldoPorCompetencia(empresa);

        Map<String, BigDecimal> saldoAcumulado = lancamentoService.calcularSaldoAcumuladoPorCompetencia(empresa);

        int anoAtual = Year.now().getValue();
        BigDecimal totalEntradasAno = lancamentoService.somarPorTipoEAno(empresa, TipoLancamento.ENTRADA, anoAtual);
        BigDecimal totalSaidasAno = lancamentoService.somarPorTipoEAno(empresa, TipoLancamento.SAIDA, anoAtual);


        model.addAttribute("empresa", empresa);
        model.addAttribute("competencias", competencias);
        model.addAttribute("saldoTotal", saldoTotal);
        model.addAttribute("saldoPorMes", saldoPorMes);
        model.addAttribute("saldoAcumulado", saldoAcumulado);
        model.addAttribute("totalEntradasAno", totalEntradasAno);
        model.addAttribute("totalSaidasAno", totalSaidasAno);
        model.addAttribute("anoAtual", anoAtual);
        model.addAttribute("anoSelecionado", ano);
        model.addAttribute("busca", busca);

        return "empresa-visualizar";
    }


    @PostMapping
    public String salvar(@ModelAttribute Empresa empresa) {
        empresaService.salvar(empresa);
        return "redirect:/empresastf/todas";
    }


    @PostMapping("/{id}/zerar-lancamentos")
    public String zerarLancamentosEmpresa(@PathVariable Long id, RedirectAttributes ra) {
        lancamentoService.deletarLancamentosPorEmpresa(id);
        ra.addFlashAttribute("mensagemSucesso", "Lançamentos da empresa foram zerados.");
        return "redirect:/empresastf/" + id ;
    }
}
