package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.parcelamento.ParcelaService;
import com.eleetricz.cashflow.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ExcelImportServiceImpl implements ExcelImportService {

    private final EmpresaRepository empresaRepository;
    private final CompetenciaRepository competenciaRepository;
    private final DescricaoRepository descricaoRepository;
    private final LancamentoRepository lancamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ParcelaService parcelaService;
    private final ParcelamentoRepository parcelamentoRepository;
    private final ParcelaRepository parcelaRepository;

    @Override
    @Transactional
    public void importarPlanilhaDoExcel(MultipartFile file, Long empresaId) throws Exception {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada"));

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);

                // Lê competência principal em A4
                Row rowComp = sheet.getRow(3);
                if (rowComp == null || rowComp.getCell(0) == null) continue;
                String competenciaStr = getCellValueAsString(rowComp.getCell(0)).trim();
                if (!competenciaStr.matches("\\d{2}/\\d{4}")) continue;

                // Cria ou busca competência principal
                String[] compParts = competenciaStr.split("/");
                Competencia competenciaPrincipal = getOrCreateCompetencia(compParts, empresa);

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());

                // Padrões de parcelamento
                Pattern pEncargos = Pattern.compile("ENCARGOS\\s+(.+?)\\s+PARCELAMENTO\\s+(\\d+)/(\\d+)", Pattern.CASE_INSENSITIVE);
                Pattern pBase     = Pattern.compile("(.+?)\\s+PARCELAMENTO\\s+(\\d+)/(\\d+)",              Pattern.CASE_INSENSITIVE);
                Pattern pParcela  = Pattern.compile("(.+?)\\s+PARCELAMENTO\\s+PARCELA\\s+(\\d+)",           Pattern.CASE_INSENSITIVE);

                // Acumula dados de parcelamento (base + encargos)
                Map<String, ParcelamentoDTO> parcelasMap = new HashMap<>();

                for (int rowIndex = 4; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) continue;

                    String descOrig = getCellValueAsString(row.getCell(1)).trim();
                    if (descOrig.isBlank()) continue;

                    // Padronização GPS -> INSS
                    String descNorm = normalizeDescricao(descOrig);

                    // Valores colunas H e I
                    BigDecimal valEnt = getCellValueAsBigDecimal(row.getCell(7));
                    BigDecimal valSai = getCellValueAsBigDecimal(row.getCell(8));
                    if (valEnt.compareTo(BigDecimal.ZERO) == 0 && valSai.compareTo(BigDecimal.ZERO) == 0) continue;

                    // Competência referida (coluna C)
                    String compRefStr = getCellValueAsString(row.getCell(2)).trim();
                    Competencia competenciaRef = parseOrDefaultCompetencia(compRefStr, empresa, competenciaPrincipal);

                    // Data ocorrência (coluna D)
                    LocalDate dataOc = parseOrDefaultData(row, competenciaPrincipal, dateFormatter, rowIndex);

                    // Identifica parcelamento
                    Matcher m;
                    if ((m = pEncargos.matcher(descNorm)).matches() || (m = pBase.matcher(descNorm)).matches() || (m = pParcela.matcher(descNorm)).matches()) {
                        boolean isEncargos = pEncargos.matcher(descNorm).matches();
                        // ajusta matcher para capturar grupos corretos
                        if (!m.group(1).equalsIgnoreCase(descNorm.split(" ")[0]) && isEncargos) {
                            // pEncargos
                        }
                        String baseName = m.group(1).trim();
                        int num = Integer.parseInt(m.group(2));
                        int total = m.groupCount() >= 3 ? Integer.parseInt(m.group(3)) : 1;

                        String key = baseName + "|" + num + "|" + total;
                        ParcelamentoDTO dto = parcelasMap.computeIfAbsent(key, k -> new ParcelamentoDTO(
                                baseName, num, total,
                                competenciaStr, compRefStr, dataOc));
                        // acumula valores
                        if (isEncargos) dto.valorEncargos = dto.valorEncargos.add(valSai.compareTo(BigDecimal.ZERO) != 0 ? valSai : valEnt);
                        else dto.valorBase = dto.valorBase.add(valEnt.compareTo(BigDecimal.ZERO) != 0 ? valEnt : valSai);

                    } else {
                        // não é parcelamento -> fluxo normal
                        processarLancamentoSimples(row, empresa, competenciaPrincipal, descNorm, valEnt, valSai, competenciaPrincipal, competenciaRef, dataOc);
                    }
                }

                // Após ler todas as linhas, executa pagamentos de parcela de uma só vez
                for (ParcelamentoDTO dto : parcelasMap.values()) {
                    Long parcelaId = findParcelaId(dto, empresa);
                    Parcela parcela = parcelaRepository.findById(parcelaId)
                            .orElseThrow(() -> new RuntimeException("Parcela não encontrada: " + parcelaId));

                    // se já foi paga, ignora
                    if (parcela.isPaga()) {
                        continue;
                    }

                    // só chama o serviço se não estiver paga
                    parcelaService.registrarPagamentoComLancamento(
                            parcelaId,
                            dto.valorBase,
                            dto.valorEncargos,
                            dto.dataPagamento,
                            dto.competenciaStr,
                            dto.competenciaReferidaStr
                    );
                }
            }
        }
    }

    // DTO interno para acumular valores de cada parcela
    private static class ParcelamentoDTO {
        final String baseName;
        final int numero;
        final int total;
        final String competenciaStr;
        final String competenciaReferidaStr;
        final LocalDate dataPagamento;
        BigDecimal valorBase = BigDecimal.ZERO;
        BigDecimal valorEncargos = BigDecimal.ZERO;

        ParcelamentoDTO(String baseName, int numero, int total,
                        String compStr, String compRef, LocalDate data) {
            this.baseName = baseName;
            this.numero = numero;
            this.total = total;
            this.competenciaStr = compStr;
            this.competenciaReferidaStr = compRef;
            this.dataPagamento = data;
        }
    }

    private void processarLancamentoSimples(Row row, Empresa empresa,
                                            Competencia comp, String descNorm,
                                            BigDecimal valEnt, BigDecimal valSai,
                                            Competencia compPrincipal, Competencia compRef,
                                            LocalDate dataOc) {
        Descricao descricao = descricaoRepository.findByNomeIgnoreCase(descNorm)
                .orElseThrow(() -> new IllegalArgumentException("Descrição não encontrada: " + descNorm));
        TipoLancamento tipo = valEnt.compareTo(BigDecimal.ZERO) != 0
                ? TipoLancamento.ENTRADA : TipoLancamento.SAIDA;
        Usuario usuario = usuarioRepository.getReferenceById(1L);
        salvarSeNaoExistir(empresa, comp, compRef, descricao, usuario,
                tipo == TipoLancamento.ENTRADA ? valEnt : valSai,
                dataOc, tipo);
    }

    private Long findParcelaId(ParcelamentoDTO dto, Empresa empresa) {
        Descricao baseDesc = descricaoRepository.findByNomeIgnoreCase(dto.baseName)
                .orElseThrow(() -> new IllegalArgumentException("Base não encontrada: " + dto.baseName));
        Parcelamento p = parcelamentoRepository.findByDescricaoBaseAndEmpresaAndTotalParcelas(
                        baseDesc, empresa, dto.total)
                .orElseThrow(() -> new RuntimeException("Parcelamento não encontrado: " + dto.baseName));
        Parcela parcela = parcelaRepository.findByParcelamentoAndNumeroParcela(p, dto.numero)
                .orElseThrow(() -> new RuntimeException("Parcela não encontrada: " + dto.numero));
        return parcela.getId();
    }

    private Competencia getOrCreateCompetencia(String[] parts, Empresa empresa) {
        int mes = Integer.parseInt(parts[0]);
        int ano = Integer.parseInt(parts[1]);
        return competenciaRepository.findByMesAndAnoAndEmpresa(mes, ano, empresa)
                .orElseGet(() -> competenceFactory(mes, ano, empresa));
    }

    private Competencia parseOrDefaultCompetencia(String compStr, Empresa empresa,
                                                  Competencia defaultComp) {
        if (compStr == null || !compStr.matches("\\d{2}/\\d{4}")) return defaultComp;
        String[] parts = compStr.split("/");
        return getOrCreateCompetencia(parts, empresa);
    }

    private Competencia competenceFactory(int mes, int ano, Empresa empresa) {
        Competencia c = new Competencia();
        c.setMes(mes);
        c.setAno(ano);
        c.setEmpresa(empresa);
        return competenciaRepository.save(c);
    }

    private LocalDate parseOrDefaultData(Row row, Competencia comp,
                                         DateTimeFormatter fmt, int rowIndex) {
        try {
            String dataStr = getCellValueAsString(row.getCell(3)).trim();
            if (!dataStr.isBlank()) return LocalDate.parse(dataStr, fmt);
        } catch (Exception ignored) {}
        int lastDay = LocalDate.of(comp.getAno(), comp.getMes(), 1).lengthOfMonth();
        return LocalDate.of(comp.getAno(), comp.getMes(), lastDay);
    }

    private String normalizeDescricao(String descOrig) {
        return switch (descOrig.toUpperCase()) {
            case "GPS" -> "INSS";
            case "ENCARGOS GPS" -> "ENCARGOS INSS";
            case "GPS 13°", "GPS 13º"    -> "INSS 13º";
            case "ENCARGOS GPS 13°",
                 "ENCARGOS GPS 13º"     -> "ENCARGOS INSS 13º";
            default -> descOrig;
        };
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    LocalDate d = cell.getLocalDateTimeCellValue().toLocalDate();
                    yield d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } else yield String.valueOf((int) cell.getNumericCellValue());
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
                case STRING -> new BigDecimal(cell.getStringCellValue().replace(",", "."));
                default -> BigDecimal.ZERO;
            };
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private void salvarSeNaoExistir(Empresa empresa,
                                    Competencia comp,
                                    Competencia compRef,
                                    Descricao desc,
                                    Usuario usuario,
                                    BigDecimal valor,
                                    LocalDate data,
                                    TipoLancamento tipo) {
        boolean exists = lancamentoRepository.existsByEmpresaAndCompetenciaAndCompetenciaReferidaAndDescricaoAndValorAndDataOcorrenciaAndTipo(
                empresa, comp, compRef, desc, valor, data, tipo);
        if (!exists) {
            lancamentoRepository.save(Lancamento.builder()
                    .empresa(empresa)
                    .competencia(comp)
                    .competenciaReferida(compRef)
                    .descricao(desc)
                    .usuario(usuario)
                    .valor(valor)
                    .dataOcorrencia(data)
                    .tipo(tipo)
                    .build());
        }
    }
}
