package com.eleetricz.cashflow.relatorio;

import com.eleetricz.cashflow.entity.Competencia;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.Lancamento;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ExportadorLancamentosExcel {
    private final Workbook workbook;
    private final CellStyle headerStyle;
    private final CellStyle titleStyle;
    private final CellStyle currencyStyle;
    private final CellStyle dateStyle;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ExportadorLancamentosExcel(Workbook workbook) {
        this.workbook = workbook;
        this.headerStyle = criarEstiloCabecalho();
        this.titleStyle = criarEstiloTitulo();
        this.currencyStyle = criarEstiloMonetario();
        this.dateStyle = criarEstiloData();
    }

    public void criarAba(Empresa empresa, Competencia competencia, List<Lancamento> lancamentos, Map<String, BigDecimal> saldoAcumuladoPorCompetencia) {
        String nomeAba = String.format("%02d-%d", competencia.getMes(), competencia.getAno());
        String chaveCompetencia = String.format("%04d-%02d", competencia.getAno(), competencia.getMes());
        BigDecimal saldo = saldoAcumuladoPorCompetencia.getOrDefault(chaveCompetencia, BigDecimal.ZERO);

        Sheet sheet = workbook.createSheet(nomeAba);
        int colunasTotais = 9;

        criarTituloEmpresa(sheet, empresa.getNome(), colunasTotais);
        criarCabecalho(sheet, 1);
        preencherLancamentos(sheet, lancamentos, saldo, 2);
        ajustarLayout(sheet, colunasTotais);
    }

    private void criarTituloEmpresa(Sheet sheet, String nomeEmpresa, int colunasTotais) {
        Row tituloRow = sheet.createRow(0);
        Cell tituloCell = tituloRow.createCell(0);
        tituloCell.setCellValue("Empresa: " + nomeEmpresa);
        tituloCell.setCellStyle(titleStyle);

        // Mescla as colunas para centralizar o título
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, colunasTotais - 1));
    }

    private void criarCabecalho(Sheet sheet, int rowIndex) {
        String[] colunas = {"Descrição", "Competência Referida", "Débito", "Crédito", "Histórico", "Tipo", "Valor", "Data Ocorrência", "Saldo"};
        Row header = sheet.createRow(rowIndex);
        for (int i = 0; i < colunas.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(colunas[i]);
            cell.setCellStyle(headerStyle);
        }
        sheet.createFreezePane(0, rowIndex + 1);
    }

    private void preencherLancamentos(Sheet sheet, List<Lancamento> lancamentos, BigDecimal saldo, int startRow) {
        int rowNum = startRow;
        for (Lancamento lancamento : lancamentos) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(lancamento.getDescricao().getNome());
            row.createCell(1).setCellValue(String.format("%02d/%d", lancamento.getCompetenciaReferida().getMes(), lancamento.getCompetenciaReferida().getAno()));
            row.createCell(2).setCellValue(lancamento.getDescricao().getCodigoDebito());
            row.createCell(3).setCellValue(lancamento.getDescricao().getCodigoCredito());
            row.createCell(4).setCellValue(lancamento.getDescricao().getCodigoHistorico());
            row.createCell(5).setCellValue(lancamento.getTipo().toString());

            Cell valorCell = row.createCell(6);
            valorCell.setCellValue(lancamento.getValor().setScale(2, RoundingMode.HALF_UP).doubleValue());
            valorCell.setCellStyle(currencyStyle);

            Cell dataCell = row.createCell(7);
            dataCell.setCellValue(lancamento.getDataOcorrencia());
            dataCell.setCellStyle(dateStyle);

            Cell saldoCell = row.createCell(8);
            saldoCell.setCellValue(saldo.setScale(2, RoundingMode.HALF_UP).doubleValue());
            saldoCell.setCellStyle(currencyStyle);
        }
    }

    private void ajustarLayout(Sheet sheet, int colunasTotais) {
        for (int i = 0; i < colunasTotais; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private CellStyle criarEstiloCabecalho() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle criarEstiloTitulo() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle criarEstiloMonetario() {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("R$ #,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle criarEstiloData() {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("dd/MM/yyyy"));
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}
