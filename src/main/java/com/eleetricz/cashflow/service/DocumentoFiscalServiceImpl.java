package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.dto.DarfData;
import com.eleetricz.cashflow.dto.DasData;
import com.eleetricz.cashflow.dto.FgtsData;
import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.entity.documento.*;
import com.eleetricz.cashflow.entity.enums.TipoDocumentoFiscal;
import com.eleetricz.cashflow.repository.*;
import com.eleetricz.cashflow.service.fechamento.FechamentoStatusService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DocumentoFiscalServiceImpl implements DocumentoFiscalService {

    private final DocumentoFiscalRepository documentoFiscalRepository;
    private final ItemDocumentoFiscalRepository itemDocumentoFiscalRepository;
    private final EmpresaRepository empresaRepository;
    private final CompetenciaRepository competenciaRepository;
    private final DescricaoRepository descricaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LancamentoRepository lancamentoRepository;
    private final FechamentoStatusService fechamentoStatusService;

    private static final DateTimeFormatter BR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter COMP_DATE = DateTimeFormatter.ofPattern("MM/yyyy");

    @Override
    public List<DocumentoFiscal> listarPorEmpresa(Empresa empresa) {
        return documentoFiscalRepository.findByEmpresa(empresa);
    }

    @Override
    public List<DocumentoFiscal> listarPorEmpresaETipo(Empresa empresa, TipoDocumentoFiscal tipo) {
        return documentoFiscalRepository.findByEmpresaAndTipo(empresa, tipo);
    }

    @Override
    public boolean registroJaExiste(Long empresaId, TipoDocumentoFiscal tipo, String competencia, String numeroDocumento) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalStateException("Empresa id=" + empresaId + " não existe"));
        return documentoFiscalRepository.existsByEmpresaAndTipoAndCompetenciaAndNumeroDocumento(
                empresa, tipo, competencia, numeroDocumento);
    }

    @Override
    @Transactional
    public void deletarPorEmpresaId(Long empresaId) {
        documentoFiscalRepository.deleteByEmpresaId(empresaId);
    }

    // ==================== IMPORTAÇÃO DARF ====================

    @Override
    @Transactional
    public int importarDadosPdfDarf(Long empresaId, List<DarfData> dadosExtraidos) {
        int inseridos = 0;

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalStateException("Empresa id=" + empresaId + " não existe"));

        for (DarfData data : dadosExtraidos) {
            if (registroJaExiste(empresaId, TipoDocumentoFiscal.DARF, data.getPeriodoApuracao(), data.getNumeroDocumento())) {
                continue;
            }

            DocumentoDarf documento = DocumentoDarf.builder()
                    .empresa(empresa)
                    .competencia(normalizarCompetencia(data.getPeriodoApuracao()))
                    .periodoApuracao(data.getPeriodoApuracao())
                    .numeroDocumento(data.getNumeroDocumento())
                    .dataVencimento(parseDate(data.getDataVencimento()))
                    .dataPagamento(parseDate(data.getDataArrecadacao()))
                    .valorPrincipal(parseBigDecimal(data.getTotalPrincipal()))
                    .valorMulta(parseBigDecimal(data.getTotalMulta()))
                    .valorJuros(parseBigDecimal(data.getTotalJuros()))
                    .valorTotal(parseBigDecimal(data.getTotalTotal()))
                    .build();

            DocumentoDarf saved = (DocumentoDarf) documentoFiscalRepository.save(documento);

            // Salvar itens
            if (data.getItens() != null) {
                for (var itemDto : data.getItens()) {
                    ItemDocumentoFiscal item = ItemDocumentoFiscal.builder()
                            .documentoFiscal(saved)
                            .codigo(itemDto.getCodigo())
                            .descricao(itemDto.getDescricao())
                            .principal(parseBigDecimal(itemDto.getPrincipal()))
                            .multa(parseBigDecimal(itemDto.getMulta()))
                            .juros(parseBigDecimal(itemDto.getJuros()))
                            .total(parseBigDecimal(itemDto.getTotal()))
                            .build();
                    itemDocumentoFiscalRepository.save(item);
                }
            }

            inseridos++;
        }

        return inseridos;
    }

    // ==================== IMPORTAÇÃO DAS ====================

    @Override
    @Transactional
    public int importarDadosPdfDas(Long empresaId, List<DasData> dadosExtraidos) {
        int inseridos = 0;

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalStateException("Empresa id=" + empresaId + " não existe"));

        for (DasData data : dadosExtraidos) {
            if (registroJaExiste(empresaId, TipoDocumentoFiscal.DAS, data.competencia, data.numeroDocumento)) {
                continue;
            }

            DocumentoDas documento = DocumentoDas.builder()
                    .empresa(empresa)
                    .competencia(normalizarCompetencia(data.competencia))
                    .numeroDocumento(data.numeroDocumento)
                    .dataVencimento(parseDate(data.dataVencimento))
                    .dataPagamento(parseDate(data.dataArrecadacao))
                    .valorPrincipal(parseBigDecimal(data.principal))
                    .valorMulta(parseBigDecimal(data.multa))
                    .valorJuros(parseBigDecimal(data.juros))
                    .valorTotal(parseBigDecimal(data.total))
                    .build();

            documentoFiscalRepository.save(documento);
            inseridos++;
        }

        return inseridos;
    }

    // ==================== IMPORTAÇÃO FGTS ====================

    @Override
    @Transactional
    public int importarDadosPdfFgts(Long empresaId, List<FgtsData> dadosExtraidos) {
        int inseridos = 0;

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalStateException("Empresa id=" + empresaId + " não existe"));

        for (FgtsData data : dadosExtraidos) {
            LocalDate dataPagamento = parseDate(data.getDataPagamento());

            boolean existe;
            if (data.getIdentificadorGuia() != null && !data.getIdentificadorGuia().isBlank()) {
                existe = documentoFiscalRepository.existsByEmpresaAndTipoAndCompetenciaAndIdentificadorGuia(
                        empresa, TipoDocumentoFiscal.FGTS, data.getCompetencia(), data.getIdentificadorGuia()
                );
            } else {
                // Verificação alternativa por data + valores
                var existentes = documentoFiscalRepository.findByEmpresaAndCompetencia(empresa, data.getCompetencia());
                existe = existentes.stream()
                        .filter(d -> d.getTipo() == TipoDocumentoFiscal.FGTS)
                        .anyMatch(d -> d.getDataPagamento().equals(dataPagamento) &&
                                d.getValorPrincipal().equals(parseBigDecimal(data.getValorPrincipal())));
            }

            if (existe) {
                continue;
            }

            DocumentoFgts documento = DocumentoFgts.builder()
                    .empresa(empresa)
                    .competencia(normalizarCompetencia(data.getCompetencia()))
                    .dataPagamento(dataPagamento)
                    .valorPrincipal(parseBigDecimal(data.getValorPrincipal()))
                    .valorMulta(parseBigDecimal(data.getMulta()))
                    .valorJuros(parseBigDecimal(data.getJuros()))
                    .identificadorGuia(data.getIdentificadorGuia())
                    .build();

            documentoFiscalRepository.save(documento);
            inseridos++;
        }

        return inseridos;
    }

    @Override
    @Transactional
    public int importarFgtsExtratoTxt(MultipartFile arquivo, Long empresaId) throws IOException {
        int inseridos = 0;

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalStateException("Empresa id=" + empresaId + " não existe"));

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // Parse do formato de extrato FGTS (exemplo: "COMPETENCIA: 04/2024 | VALOR: 1.234,56")
                Map<String, String> dados = parseLinhaFgts(line);
                if (dados.isEmpty()) continue;

                String competencia = dados.get("competencia");
                String valorPrincipal = dados.get("valor_principal");
                String juros = dados.getOrDefault("juros", "0");
                String multa = dados.getOrDefault("multa", "0");

                // Verificar duplicidade
                boolean existe = documentoFiscalRepository.existsByEmpresaAndTipoAndCompetenciaAndIdentificadorGuia(
                        empresa, TipoDocumentoFiscal.FGTS, competencia, null
                );

                if (!existe) {
                    DocumentoFgts documento = DocumentoFgts.builder()
                            .empresa(empresa)
                            .competencia(normalizarCompetencia(competencia))
                            .valorPrincipal(parseBigDecimal(valorPrincipal))
                            .valorJuros(parseBigDecimal(juros))
                            .valorMulta(parseBigDecimal(multa))
                            .dataPagamento(LocalDate.now()) // Data atual como fallback
                            .build();

                    documentoFiscalRepository.save(documento);
                    inseridos++;
                }
            }
        }

        return inseridos;
    }

    // ==================== IMPORTAÇÃO GERAL (LANÇAMENTOS) ====================

    @Override
    @Transactional
    public void importarTodos() {
        importarTodosDarf();
        importarTodosDas();
        importarTodosFgts();
        importarTodosDae();
    }

    private void importarTodosDarf() {
        Usuario sistema = getUsuarioByName("admin");
        Descricao descINSS = getOrCreateDescricao("INSS");
        Descricao descEncINSS = getOrCreateDescricao("ENCARGOS INSS");
        Descricao descIR = getOrCreateDescricao("IRRF");
        Descricao descEncIR = getOrCreateDescricao("ENCARGOS IRRF");
        Descricao descMultaCLT = getOrCreateDescricao("MULTA CLT");
        Descricao descEncMultaCLT = getOrCreateDescricao("ENCARGOS MULTA CLT");

        Map<Long, Empresa> empresaCache = new HashMap<>();
        Map<String, Competencia> competenciaCache = new HashMap<>();

        List<DocumentoDarf> documentos = documentoFiscalRepository.findByEmpresaAndTipo(null, TipoDocumentoFiscal.DARF)
                .stream()
                .map(d -> (DocumentoDarf) d)
                .toList();

        for (DocumentoDarf doc : documentos) {
            if (doc.getItens() == null || doc.getItens().isEmpty()) continue;

            Empresa empresa = empresaCache.computeIfAbsent(
                    doc.getEmpresa().getId(),
                    id -> empresaRepository.findById(id)
                            .orElseThrow(() -> new IllegalStateException("Empresa id=" + id + " não existe"))
            );

            String chaveCompReferida = doc.getCompetencia() + "-" + empresa.getId();
            Competencia competenciaReferida = competenciaCache.computeIfAbsent(
                    chaveCompReferida,
                    key -> criarOuObterCompetencia(doc.getCompetencia(), empresa)
            );

            String competenciaPagamentoStr = doc.getDataPagamento() != null
                    ? doc.getDataPagamento().format(COMP_DATE)
                    : doc.getCompetencia();
            Competencia competenciaPagamento = competenciaCache.computeIfAbsent(
                    competenciaPagamentoStr + "-" + empresa.getId(),
                    key -> criarOuObterCompetencia(competenciaPagamentoStr, empresa)
            );

            LocalDate dataOcorrencia = doc.getDataPagamento();

            // Processar itens
            BigDecimal totalINSS = BigDecimal.ZERO, encargosINSS = BigDecimal.ZERO;
            BigDecimal totalIR = BigDecimal.ZERO, encargosIR = BigDecimal.ZERO;
            BigDecimal totalMultaCLT = BigDecimal.ZERO, encargosMultaCLT = BigDecimal.ZERO;

            for (ItemDocumentoFiscal item : doc.getItens()) {
                String descricao = item.getDescricao();
                if (descricao == null) continue;

                BigDecimal principal = item.getPrincipal() != null ? item.getPrincipal() : BigDecimal.ZERO;
                BigDecimal multa = item.getMulta() != null ? item.getMulta() : BigDecimal.ZERO;
                BigDecimal juros = item.getJuros() != null ? item.getJuros() : BigDecimal.ZERO;
                BigDecimal encargos = multa.add(juros);

                if (descricao.contains("CONTR PREV DESCONTA SEGURADO") || descricao.contains("CP DESCONTADA SEGURADO")) {
                    totalINSS = totalINSS.add(principal);
                    encargosINSS = encargosINSS.add(encargos);
                } else if (descricao.contains("IRRF")) {
                    totalIR = totalIR.add(principal);
                    encargosIR = encargosIR.add(encargos);
                } else if (descricao.contains("MULTA DA CLT")) {
                    totalMultaCLT = totalMultaCLT.add(principal);
                    encargosMultaCLT = encargosMultaCLT.add(encargos);
                }
            }

            // Salvar lançamentos
            if (totalINSS.compareTo(BigDecimal.ZERO) > 0) {
                salvarLancamento(empresa, competenciaPagamento, competenciaReferida, descINSS, sistema, totalINSS, dataOcorrencia);
            }
            if (encargosINSS.compareTo(BigDecimal.ZERO) > 0) {
                salvarLancamento(empresa, competenciaPagamento, competenciaReferida, descEncINSS, sistema, encargosINSS, dataOcorrencia);
            }
            if (totalIR.compareTo(BigDecimal.ZERO) > 0) {
                salvarLancamento(empresa, competenciaPagamento, competenciaReferida, descIR, sistema, totalIR, dataOcorrencia);
            }
            if (encargosIR.compareTo(BigDecimal.ZERO) > 0) {
                salvarLancamento(empresa, competenciaPagamento, competenciaReferida, descEncIR, sistema, encargosIR, dataOcorrencia);
            }
            if (totalMultaCLT.compareTo(BigDecimal.ZERO) > 0) {
                salvarLancamento(empresa, competenciaPagamento, competenciaReferida, descMultaCLT, sistema, totalMultaCLT, dataOcorrencia);
            }
            if (encargosMultaCLT.compareTo(BigDecimal.ZERO) > 0) {
                salvarLancamento(empresa, competenciaPagamento, competenciaReferida, descEncMultaCLT, sistema, encargosMultaCLT, dataOcorrencia);
            }
        }
    }

    private void importarTodosDas() {
        Descricao dasDesc = getOrCreateDescricao("DAS");
        Descricao encDasDesc = getOrCreateDescricao("ENCARGOS DAS");
        Usuario usuario = getUsuarioByName("admin");

        Map<Long, Empresa> empresaCache = new HashMap<>();
        Map<String, Competencia> competenciaCache = new HashMap<>();

        List<DocumentoDas> documentos = documentoFiscalRepository.findByEmpresaAndTipo(null, TipoDocumentoFiscal.DAS)
                .stream()
                .map(d -> (DocumentoDas) d)
                .toList();

        for (DocumentoDas rec : documentos) {
            Empresa empresa = empresaCache.computeIfAbsent(
                    rec.getEmpresa().getId(),
                    id -> empresaRepository.findById(id)
                            .orElseThrow(() -> new IllegalStateException("Empresa id=" + id + " não existe"))
            );

            Competencia competencia = getOrCacheCompetencia(competenciaCache, rec.getCompetencia(), empresa);
            Competencia competenciaReferida = rec.getCompetencia().equals(rec.getCompetencia())
                    ? competencia
                    : getOrCacheCompetencia(competenciaCache, rec.getCompetencia(), empresa);

            LocalDate dataOcorrencia = rec.getDataPagamento() != null
                    ? rec.getDataPagamento()
                    : LocalDate.now();

            BigDecimal multa = rec.getValorMulta() != null ? rec.getValorMulta() : BigDecimal.ZERO;
            BigDecimal juros = rec.getValorJuros() != null ? rec.getValorJuros() : BigDecimal.ZERO;
            BigDecimal encargos = multa.add(juros);
            BigDecimal principal = rec.getValorPrincipal() != null ? rec.getValorPrincipal() : BigDecimal.ZERO;

            if (encargos.compareTo(BigDecimal.ZERO) > 0) {
                salvarLancamento(empresa, competencia, competenciaReferida, dasDesc, usuario, principal, dataOcorrencia);
                salvarLancamento(empresa, competencia, competenciaReferida, encDasDesc, usuario, encargos, dataOcorrencia);
            } else {
                BigDecimal total = rec.getValorTotal() != null ? rec.getValorTotal() : principal;
                salvarLancamento(empresa, competencia, competenciaReferida, dasDesc, usuario, total, dataOcorrencia);
            }
        }
    }

    private void importarTodosFgts() {
        Descricao descFgts = getOrCreateDescricao("FGTS");
        Usuario admin = getUsuarioByName("admin");

        Map<Long, Empresa> empresaCache = new HashMap<>();
        Map<String, Competencia> competenciaCache = new HashMap<>();
        Set<String> competenciasProcessadas = new HashSet<>();

        List<DocumentoFgts> documentos = documentoFiscalRepository.findByEmpresaAndTipo(null, TipoDocumentoFiscal.FGTS)
                .stream()
                .map(d -> (DocumentoFgts) d)
                .toList();

        for (DocumentoFgts rec : documentos) {
            Empresa empresa = empresaCache.computeIfAbsent(
                    rec.getEmpresa().getId(),
                    id -> empresaRepository.findById(id)
                            .orElseThrow(() -> new IllegalStateException("Empresa id=" + id + " não existe"))
            );

            LocalDate dataOcorrencia = rec.getDataPagamento();
            String competenciaStr = rec.getCompetencia();

            Competencia competenciaReferida = getOrCacheCompetencia(competenciaCache, competenciaStr, empresa);

            String competenciaPagamentoStr = dataOcorrencia.format(COMP_DATE);
            Competencia competenciaPagamento = getOrCacheCompetencia(competenciaCache, competenciaPagamentoStr, empresa);

            BigDecimal valor = rec.getValorTotal() != null
                    ? rec.getValorTotal()
                    : (rec.getValorPrincipal() != null ? rec.getValorPrincipal() : BigDecimal.ZERO);

            boolean exists = lancamentoRepository
                    .existsByEmpresaAndCompetenciaAndCompetenciaReferidaAndDescricaoAndValorAndDataOcorrenciaAndTipo(
                            empresa, competenciaPagamento, competenciaReferida, descFgts, valor, dataOcorrencia, TipoLancamento.SAIDA
                    );

            if (!exists) {
                lancamentoRepository.save(
                        Lancamento.builder()
                                .empresa(empresa)
                                .competencia(competenciaPagamento)
                                .competenciaReferida(competenciaReferida)
                                .descricao(descFgts)
                                .usuario(admin)
                                .valor(valor)
                                .dataOcorrencia(dataOcorrencia)
                                .tipo(TipoLancamento.SAIDA)
                                .build()
                );
            }

            competenciasProcessadas.add(competenciaStr);
        }

        // Marcar fechamento como concluído
        for (String competenciaString : competenciasProcessadas) {
            Empresa empresa = !documentos.isEmpty() ? documentos.get(0).getEmpresa() : null;
            if (empresa != null) {
                fechamentoStatusService.marcarConcluido(
                        empresa,
                        competenciaString,
                        s -> s.setFgtsStatus(StatusTarefa.CONCLUIDO)
                );
            }
        }
    }

    private void importarTodosDae() {
        Descricao daeDesc = getOrCreateDescricao("DAE 10");
        Usuario sistema = getUsuarioByName("admin");

        Map<Long, Empresa> empresaCache = new HashMap<>();
        Map<String, Competencia> competenciaCache = new HashMap<>();

        List<DocumentoDae> documentos = documentoFiscalRepository.findByEmpresaAndTipo(null, TipoDocumentoFiscal.DAE)
                .stream()
                .map(d -> (DocumentoDae) d)
                .toList();

        for (DocumentoDae rec : documentos) {
            Empresa empresa = empresaCache.computeIfAbsent(
                    rec.getEmpresa().getId(),
                    id -> empresaRepository.findById(id)
                            .orElseThrow(() -> new IllegalStateException("Empresa id=" + id + " não existe"))
            );

            Competencia competenciaReferida = getOrCacheCompetencia(competenciaCache, rec.getCompetenciaReferida(), empresa);
            Competencia competencia = getOrCacheCompetencia(competenciaCache, rec.getCompetencia(), empresa);

            LocalDate dataOcorrencia = rec.getDataPagamento() != null
                    ? rec.getDataPagamento()
                    : LocalDate.now();

            BigDecimal valor = rec.getValorTotal() != null
                    ? rec.getValorTotal()
                    : (rec.getValorPrincipal() != null ? rec.getValorPrincipal() : BigDecimal.ZERO);

            salvarLancamento(empresa, competencia, competenciaReferida, daeDesc, sistema, valor, dataOcorrencia);
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private void salvarLancamento(Empresa empresa, Competencia competencia, Competencia competenciaReferida,
                                  Descricao descricao, Usuario usuario, BigDecimal valor, LocalDate dataOcorrencia) {
        boolean exists = lancamentoRepository
                .existsByEmpresaAndCompetenciaAndCompetenciaReferidaAndDescricaoAndValorAndDataOcorrenciaAndTipo(
                        empresa, competencia, competenciaReferida, descricao, valor, dataOcorrencia, TipoLancamento.SAIDA
                );

        if (!exists) {
            lancamentoRepository.save(
                    Lancamento.builder()
                            .empresa(empresa)
                            .competencia(competencia)
                            .competenciaReferida(competenciaReferida)
                            .descricao(descricao)
                            .usuario(usuario)
                            .valor(valor)
                            .dataOcorrencia(dataOcorrencia)
                            .tipo(TipoLancamento.SAIDA)
                            .build()
            );
        }
    }

    private Competencia getOrCacheCompetencia(Map<String, Competencia> cache, String competenciaStr, Empresa empresa) {
        String key = competenciaStr + "-" + empresa.getId();
        return cache.computeIfAbsent(key, k -> criarOuObterCompetencia(competenciaStr, empresa));
    }

    private Competencia criarOuObterCompetencia(String chave, Empresa empresa) {
        String[] p = chave.split("/");
        int mes = Integer.parseInt(p[0]);
        int ano = Integer.parseInt(p[1]);
        return competenciaRepository.findByMesAndAnoAndEmpresa(mes, ano, empresa)
                .orElseGet(() -> competenciaRepository.save(
                        Competencia.builder()
                                .mes(mes)
                                .ano(ano)
                                .empresa(empresa)
                                .build()
                ));
    }

    private Descricao getOrCreateDescricao(String nome) {
        return descricaoRepository.findByNome(nome)
                .orElseGet(() -> descricaoRepository.save(Descricao.builder().nome(nome).build()));
    }

    private Usuario getUsuarioByName(String nome) {
        return usuarioRepository.findByNome(nome)
                .orElseThrow(() -> new IllegalStateException("Usuário '" + nome + "' não encontrado"));
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr, BR_DATE);
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) return BigDecimal.ZERO;
        return new BigDecimal(value.replace(".", "").replace(",", "."));
    }

    private String normalizarCompetencia(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Competência vazia");
        }

        // Se vier no formato dd/MM/yyyy → converte para MM/yyyy
        if (valor.matches("\\d{2}/\\d{2}/\\d{4}")) {
            LocalDate data = LocalDate.parse(valor, BR_DATE);
            return data.format(COMP_DATE);
        }

        // Se já estiver MM/yyyy
        if (valor.matches("\\d{2}/\\d{4}")) {
            return valor;
        }

        throw new IllegalArgumentException("Formato inválido de competência: " + valor);
    }

    private Map<String, String> parseLinhaFgts(String line) {
        Map<String, String> dados = new HashMap<>();
        // Implementação básica - ajustar conforme formato real do arquivo
        if (line.contains("Competência:") || line.contains("COMPETENCIA:")) {
            String[] parts = line.split("\\|");
            for (String part : parts) {
                String[] kv = part.split(":");
                if (kv.length == 2) {
                    String key = kv[0].trim().toLowerCase().replace(" ", "_");
                    String value = kv[1].trim();
                    dados.put(key, value);
                }
            }
        }
        return dados;
    }
}
