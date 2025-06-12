package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.dto.ReceitaCompraResumoDTO;
import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.relatorio.ExportadorLancamentosExcel;
import com.eleetricz.cashflow.repository.CompetenciaRepository;
import com.eleetricz.cashflow.repository.DescricaoRepository;
import com.eleetricz.cashflow.repository.LancamentoRepository;
import com.eleetricz.cashflow.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/lancamentostf")
public class LancamentoViewController {

    @Autowired private LancamentoService lancamentoService;
    @Autowired private EmpresaService empresaService;
    @Autowired private CompetenciaService competenciaService;
    @Autowired private DescricaoService descricaoService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private LancamentoRepository lancamentoRepository;
    @Autowired private DescricaoRepository descricaoRepository;
    @Autowired private CompetenciaRepository competenciaRepository;
    @Autowired private AuditoriaService auditoriaService;



    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Lancamento lancamento) {
        lancamentoService.salvar(lancamento);
        return "redirect:/lancamentostf/novo/" + lancamento.getEmpresa().getId() + "/" + lancamento.getCompetencia().getId();
    }


    @GetMapping("/empresa/{empresaId}")
    public String listarPorEmpresa(
            @PathVariable Long empresaId,
            @RequestParam(value = "sort", required = false) String sort,
            Model model) {

        Empresa empresa = empresaService.buscarPorId(empresaId);
        List<Lancamento> lancamentos;

        if ("dataAsc".equals(sort)) {
            lancamentos = lancamentoService.buscarPorEmpresaOrdenadoPorData(empresa.getId(), true);
        } else if ("dataDesc".equals(sort)) {
            lancamentos = lancamentoService.buscarPorEmpresaOrdenadoPorData(empresa.getId(), false);
        } else {
            lancamentos = lancamentoService.listarPorEmpresa(empresa);
        }

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

        String compStr = competencia.getMes() + "-" + competencia.getAno();

        List<LancamentoEsperado> pendencias = auditoriaService.verificarPendencias(empresaId, compStr);

        model.addAttribute("empresa", empresa);
        model.addAttribute("competencia", competencia);
        model.addAttribute("lancamentos", lancamentos);
        model.addAttribute("saldo", saldoPorMes.get(competencia.getMes() + "/" + competencia.getAno()));
        model.addAttribute("pendencias", pendencias);
        return "lancamentos/listar-por-competencia";
    }

    @GetMapping("/novo/{empresaId}/{competenciaId}")
    public String novoLancamento(@PathVariable Long empresaId, @PathVariable Long competenciaId, Model model) {
        Empresa empresa = empresaService.buscarPorId(empresaId);
        List<Competencia> competencias = competenciaService.listarPorEmpresa(empresa);

        Competencia competencia = competenciaService.buscarPorId(competenciaId);

        List<Descricao> descricoes = descricaoService.listarTodas();  // Buscar todas as descrições


        List<Usuario> usuarios = usuarioService.listarTodos(); // Buscar todos os usuarios


        // monta o objeto de domínio
        Lancamento lancamento = new Lancamento();
        lancamento.setEmpresa(empresa);
        lancamento.setCompetencia(competencia);

        model.addAttribute("empresa", empresa);
        model.addAttribute("lancamento", lancamento);
        model.addAttribute("competencias", competencias);
        model.addAttribute("competencia", competencia); // Pegar competencia
        model.addAttribute("usuarios", usuarios); // Listar todos os usuarios
        model.addAttribute("descricoes", descricoes);  // Lista de descrições

        return "lancamentos/formulario";
    }

    @GetMapping("/empresa/{empresaId}/exportar-excel")
    public void exportarExcel(@PathVariable Long empresaId, HttpServletResponse response) throws IOException {
        // Buscar empresa
        Empresa empresa = empresaService.buscarPorId(empresaId);

        // Buscar lançamentos e agrupar por competência
        List<Lancamento> todosLancamentos = lancamentoService.listarPorEmpresa(empresa);
        Map<String, BigDecimal> saldoAcumuladoPorCompetencia = lancamentoService.calcularSaldoAcumuladoPorCompetencia(empresa);

        Map<Competencia, List<Lancamento>> lancamentosPorCompetencia = todosLancamentos.stream()
                .collect(Collectors.groupingBy(Lancamento::getCompetencia));

        // Ordenar competências por data
        List<Map.Entry<Competencia, List<Lancamento>>> competenciasOrdenadas = lancamentosPorCompetencia.entrySet().stream()
                .sorted(Comparator.comparing(entry -> LocalDate.of(entry.getKey().getAno(), entry.getKey().getMes(), 1)))
                .toList();

        // Definir cabeçalho da resposta HTTP
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        // Evita problemas com espaços ou caracteres inválidos no nome do arquivo
        String nomeArquivo = "lancamentos_" + empresa.getNome().replaceAll("[^a-zA-Z0-9_-]", "_") + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + nomeArquivo);

        // Criar planilha Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            ExportadorLancamentosExcel exportador = new ExportadorLancamentosExcel(workbook);
            for (Map.Entry<Competencia, List<Lancamento>> entry : competenciasOrdenadas) {
                exportador.criarAba(empresa, entry.getKey(), entry.getValue(), saldoAcumuladoPorCompetencia);
            }
            workbook.write(response.getOutputStream());
        }
    }

    @GetMapping("/excluir/{id}/{empresaId}/{competenciaId}")
    public String excluir(
            @PathVariable Long id,
            @PathVariable Long empresaId,
            @PathVariable Long competenciaId,
            Model model) {
        try {
            lancamentoService.excluirLancamento(id);
            // redireciona incluindo empresaId e competenciaId
            return "redirect:/lancamentostf/competencia/"
                    + empresaId + "/" + competenciaId;
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao excluir lançamento: " + e.getMessage());
            // recarrega a lista em caso de erro
            Empresa empresa = empresaService.buscarPorId(empresaId);
            Competencia competencia = competenciaService.buscarPorId(competenciaId);
            List<Lancamento> lancamentos = lancamentoService
                    .listarPorCompetencia(empresa, competencia);
            model.addAttribute("lancamentos", lancamentos);
            model.addAttribute("empresaId", empresaId);
            model.addAttribute("competenciaId", competenciaId);
            return "lancamentos/listar-por-competencia";
        }
    }

    @GetMapping("/resumo/empresa/{empresaId}/ano/{ano}")
    public String getResumoReceitaCompra(@PathVariable Long empresaId,
                                         @PathVariable int ano,
                                         Model model) {
        List<ReceitaCompraResumoDTO> resumo = lancamentoService.getResumoReceitaCompraAnual(empresaId, ano);

        BigDecimal totalReceitas = resumo.stream()
                .map(ReceitaCompraResumoDTO::getReceitas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCompras = resumo.stream()
                .map(ReceitaCompraResumoDTO::getCompras)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("resumo", resumo);
        model.addAttribute("ano", ano);
        model.addAttribute("empresa", empresaService.buscarPorId(empresaId));
        model.addAttribute("totalReceitas", totalReceitas);
        model.addAttribute("totalCompras", totalCompras);

        return "compras-receitas/resumo";
    }



}
