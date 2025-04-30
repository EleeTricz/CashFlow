package com.eleetricz.cashflow.controller.view;

import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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


    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Lancamento lancamento) {
        lancamentoService.salvar(lancamento);
        return "redirect:/lancamentostf/empresa/" + lancamento.getEmpresa().getId();
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

        model.addAttribute("empresa", empresa);
        model.addAttribute("competencia", competencia);
        model.addAttribute("lancamentos", lancamentos);
        model.addAttribute("saldo", saldoPorMes.get(competencia.getMes() + "/" + competencia.getAno()));

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
        Empresa empresa = empresaService.buscarPorId(empresaId);
        List<Lancamento> lancamentos = lancamentoService.listarPorEmpresa(empresa);

        // Agrupar lançamentos por competência (objeto) para ordenar cronologicamente
        Map<Competencia, List<Lancamento>> agrupado = lancamentos.stream()
                .collect(Collectors.groupingBy(Lancamento::getCompetencia));

        // Ordenar competências da mais antiga para a mais recente
        List<Map.Entry<Competencia, List<Lancamento>>> competenciasOrdenadas = agrupado.entrySet().stream()
                .sorted(Comparator.comparing(e -> LocalDate.of(e.getKey().getAno(), e.getKey().getMes(), 1)))
                .collect(Collectors.toList());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=lancamentos_" + empresa.getNome() + ".xlsx");

        Workbook workbook = new XSSFWorkbook();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Map.Entry<Competencia, List<Lancamento>> entry : competenciasOrdenadas) {
            Competencia competencia = entry.getKey();
            String nomeAba = String.format("%02d-%d", competencia.getMes(), competencia.getAno()); // Aba sem "/"
            Sheet sheet = workbook.createSheet(nomeAba);

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Descrição");
            header.createCell(1).setCellValue("Competência Referida");
            header.createCell(2).setCellValue("Débito");
            header.createCell(3).setCellValue("Crédito");
            header.createCell(4).setCellValue("Histórico");
            header.createCell(5).setCellValue("Tipo");
            header.createCell(6).setCellValue("Valor");
            header.createCell(7).setCellValue("Data Ocorrência");

            int rowNum = 1;
            for (Lancamento lancamento : entry.getValue()) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(lancamento.getDescricao().getNome());

                String competenciaReferida = String.format("%02d/%d",
                        lancamento.getCompetenciaReferida().getMes(),
                        lancamento.getCompetenciaReferida().getAno());
                row.createCell(1).setCellValue(competenciaReferida);

                row.createCell(2).setCellValue(lancamento.getDescricao().getCodigoDebito());
                row.createCell(3).setCellValue(lancamento.getDescricao().getCodigoCredito());
                row.createCell(4).setCellValue(lancamento.getDescricao().getCodigoHistorico());
                row.createCell(5).setCellValue(lancamento.getTipo().toString());
                row.createCell(6).setCellValue(lancamento.getValor().setScale(2, RoundingMode.HALF_UP).doubleValue());

                String dataFormatada = lancamento.getDataOcorrencia().format(formatter);
                row.createCell(7).setCellValue(dataFormatada);
            }
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }





}
