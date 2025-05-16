package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.repository.CompetenciaRepository;
import com.eleetricz.cashflow.repository.DescricaoRepository;
import com.eleetricz.cashflow.repository.EmpresaRepository;
import com.eleetricz.cashflow.repository.LancamentoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ExcelImportServiceImpl implements ExcelImportService{

    private final EmpresaRepository empresaRepository;
    private final CompetenciaRepository competenciaRepository;
    private final DescricaoRepository descricaoRepository;
    private final LancamentoRepository lancamentoRepository;

    @Override
    public void importarPlanilhaDoExcel(MultipartFile file, Long empresaId) throws Exception {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada"));

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);

                // Lê célula A4 com a competência principal
                Row rowComp = sheet.getRow(3);
                if (rowComp == null || rowComp.getCell(0) == null) continue;

                String competenciaStr = getCellValueAsString(rowComp.getCell(0)).trim();
                if (!competenciaStr.matches("\\d{2}/\\d{4}")) continue;

                String[] compParts = competenciaStr.split("/");
                int mes = Integer.parseInt(compParts[0]);
                int ano = Integer.parseInt(compParts[1]);

                Competencia competenciaPrincipal = competenciaRepository
                        .findByMesAndAnoAndEmpresa(mes, ano, empresa)
                        .orElseGet(() -> {
                            Competencia c = new Competencia();
                            c.setMes(mes);
                            c.setAno(ano);
                            c.setEmpresa(empresa);
                            return competenciaRepository.save(c);
                        });

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());

                for (int rowIndex = 4; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) continue;

                    String descricaoNome = getCellValueAsString(row.getCell(1)).trim();
                    if (descricaoNome.isBlank()) continue;

                    // Busca Descricao ignorando case
                    Descricao descricao = descricaoRepository.findByNomeIgnoreCase(descricaoNome)
                            .orElse(null);
                    if (descricao == null) {
                        System.out.println("Descrição não encontrada: " + descricaoNome);
                        continue;
                    }

                    // Lê competencia referida (coluna C)
                    String competenciaReferidaStr = getCellValueAsString(row.getCell(2)).trim();
                    Competencia competenciaReferida = null;
                    if (!competenciaReferidaStr.isBlank() && competenciaReferidaStr.matches("\\d{2}/\\d{4}")) {
                        String[] refParts = competenciaReferidaStr.split("/");
                        int mesRef = Integer.parseInt(refParts[0]);
                        int anoRef = Integer.parseInt(refParts[1]);
                        competenciaReferida = competenciaRepository
                                .findByMesAndAnoAndEmpresa(mesRef, anoRef, empresa)
                                .orElseGet(() -> {
                                    Competencia c = new Competencia();
                                    c.setMes(mesRef);
                                    c.setAno(anoRef);
                                    c.setEmpresa(empresa);
                                    return competenciaRepository.save(c);
                                });
                    }

                    // Lê data de ocorrência (coluna D)
                    LocalDate dataOcorrencia = null;
                    try {
                        String dataStr = getCellValueAsString(row.getCell(3)).trim();
                        if (!dataStr.isBlank()) {
                            dataOcorrencia = LocalDate.parse(dataStr, dateFormatter);
                        }
                    } catch (Exception e) {
                        System.out.println("Data inválida na linha " + (rowIndex + 1));
                    }

                    // Leitura dos valores nas colunas H (entrada) e I (saída)
                    BigDecimal valorEntrada = getCellValueAsBigDecimal(row.getCell(7));
                    BigDecimal valorSaida = getCellValueAsBigDecimal(row.getCell(8));


                    BigDecimal valor = null;
                    TipoLancamento tipo = null;
                    if (valorEntrada.compareTo(BigDecimal.ZERO) != 0) {
                        valor = valorEntrada;
                        tipo = TipoLancamento.ENTRADA;
                    } else if (valorSaida.compareTo(BigDecimal.ZERO) != 0) {
                        valor = valorSaida;
                        tipo = TipoLancamento.SAIDA;
                    } else {
                        continue; // pula linha se não houver valor
                    }

                    // Criação do lançamento
                    Lancamento lancamento = new Lancamento();
                    lancamento.setEmpresa(empresa);
                    lancamento.setCompetencia(competenciaPrincipal);
                    lancamento.setCompetenciaReferida(competenciaReferida);
                    lancamento.setDescricao(descricao);
                    lancamento.setValor(valor);
                    lancamento.setTipo(tipo);
                    lancamento.setDataOcorrencia(dataOcorrencia);

                    lancamentoRepository.save(lancamento);
                }
            }
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    LocalDate localDate = cell.getLocalDateTimeCellValue().toLocalDate();
                    yield localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } else {
                    yield String.valueOf((int) cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING -> {
                    String raw = cell.getStringCellValue().replace(",", ".");
                    yield new BigDecimal(raw);
                }
                default -> BigDecimal.ZERO;
            };
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

}
