package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.dto.FgtsData;
import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LancamentoFgtsServiceImpl implements LancamentoFgtsService {

    private final LancamentoFgtsRepository fgtsRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DescricaoRepository descricaoRepository;
    private final CompetenciaRepository competenciaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final FechamentoStatusRepository fechamentoStatusRepository;

    private static final DateTimeFormatter BR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Transactional
    public int importarDadosPdfFgts(Long empresaId, List<FgtsData> dadosExtraidos) {
        int inseridos = 0;

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalStateException("Empresa id=" + empresaId + " não existe"));

        for (FgtsData data : dadosExtraidos) {
            LocalDate dataPagamento = LocalDate.parse(data.getDataPagamento(), BR_DATE);

            boolean existe;
            if (data.getIdentificadorGuia() != null && !data.getIdentificadorGuia().isBlank()) {
                existe = fgtsRepository.existsByEmpresaAndCompetenciaAndIdentificadorGuia(
                        empresa, data.getCompetencia(), data.getIdentificadorGuia()
                );
            } else {
                existe = fgtsRepository.existsByEmpresaAndCompetenciaAndDataPagamentoAndValorPrincipalAndJurosAndMulta(
                        empresa,
                        data.getCompetencia(),
                        dataPagamento,
                        data.getValorPrincipal(),
                        data.getJuros(),
                        data.getMulta()
                );
            }

            if (existe) {
                continue;
            }

            LancamentoFgts lanc = LancamentoFgts.builder()
                    .empresa(empresa)
                    .competencia(data.getCompetencia())
                    .dataPagamento(dataPagamento)
                    .valorPrincipal(data.getValorPrincipal())
                    .juros(data.getJuros())
                    .multa(data.getMulta())
                    .identificadorGuia(data.getIdentificadorGuia())
                    .build();

            fgtsRepository.save(lanc);
            inseridos++;
        }

        Set<String> competenciasProcessadas = new HashSet<>();

        for (FgtsData data : dadosExtraidos) {
            competenciasProcessadas.add(data.getCompetencia());
        }

        for (String competenciaString : competenciasProcessadas) {
            Competencia competencia = getOrCreateCompetenciaFromService(competenciaString, empresa);

            FechamentoStatus status = fechamentoStatusRepository
                    .findByEmpresaAndCompetencia(empresa, competencia)
                    .orElseGet(() -> FechamentoStatus.builder()
                            .empresa(empresa)
                            .competencia(competencia)
                            .build());

            status.setFgtsStatus(StatusTarefa.CONCLUIDO);
            fechamentoStatusRepository.save(status);
        }

        return inseridos;
    }

    // Método auxiliar para ser usado pelos serviços
    private Competencia getOrCreateCompetenciaFromService(String chave, Empresa empresa) {
        String[] partes = chave.split("/");
        int mes = Integer.parseInt(partes[0]);
        int ano = Integer.parseInt(partes[1]);

        return competenciaRepository.findByMesAndAnoAndEmpresa(mes, ano, empresa)
                .orElseGet(() -> competenciaRepository.save(
                        Competencia.builder()
                                .mes(mes)
                                .ano(ano)
                                .empresa(empresa)
                                .build()
                ));
    }

    @Override
    @Transactional
    public void importarTodos() {
        Usuario sistema = usuarioRepository.findByNome("admin")
                .orElseThrow(() -> new IllegalStateException("Usuário 'admin' não encontrado"));

        Descricao descFgts = getOrCreateDescricao("FGTS");
        Descricao descEncargosFgts = getOrCreateDescricao("ENCARGOS FGTS");

        Map<Long, Empresa> empresaCache = new HashMap<>();
        Map<String, Competencia> competenciaCache = new HashMap<>();

        for (LancamentoFgts rec : fgtsRepository.findAll()) {
            Empresa empresa = empresaCache.computeIfAbsent(
                    rec.getEmpresa().getId(),
                    id -> empresaRepository.findById(id)
                            .orElseThrow(() -> new IllegalStateException("Empresa id=" + id + " não existe"))
            );

            Competencia competenciaReferida = getOrCreateCompetencia(competenciaCache, rec.getCompetencia(), empresa);

            LocalDate dataOcorrencia = rec.getDataPagamento();
            String chaveCompetenciaPagamento = dataOcorrencia.getMonthValue() + "/" + dataOcorrencia.getYear();
            Competencia competenciaPagamento = getOrCreateCompetencia(competenciaCache, chaveCompetenciaPagamento, empresa);

            BigDecimal principal = parse(rec.getValorPrincipal());
            BigDecimal juros = parse(rec.getJuros());
            BigDecimal multa = parse(rec.getMulta());
            BigDecimal encargos = juros.add(multa);

            if (principal.compareTo(BigDecimal.ZERO) > 0) {
                salvarSeNaoExistir(empresa, competenciaPagamento, competenciaReferida, descFgts, sistema, principal, dataOcorrencia);
            }

            if (encargos.compareTo(BigDecimal.ZERO) > 0) {
                salvarSeNaoExistir(empresa, competenciaPagamento, competenciaReferida, descEncargosFgts, sistema, encargos, dataOcorrencia);
            }
        }
    }

    private Competencia getOrCreateCompetencia(Map<String, Competencia> cache, String chave, Empresa empresa) {
        return cache.computeIfAbsent(chave + "-" + empresa.getId(), key -> {
            String[] partes = chave.split("/");
            int mes = Integer.parseInt(partes[0]);
            int ano = Integer.parseInt(partes[1]);

            return competenciaRepository.findByMesAndAnoAndEmpresa(mes, ano, empresa)
                    .orElseGet(() -> competenciaRepository.save(
                            Competencia.builder()
                                    .mes(mes)
                                    .ano(ano)
                                    .empresa(empresa)
                                    .build()
                    ));
        });
    }

    private Descricao getOrCreateDescricao(String nome) {
        return descricaoRepository.findByNome(nome)
                .orElseGet(() -> descricaoRepository.save(Descricao.builder().nome(nome).build()));
    }

    private BigDecimal parse(String valor) {
        if (valor == null || valor.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(valor);
    }

    private void salvarSeNaoExistir(Empresa empresa,
                                    Competencia competencia,
                                    Competencia competenciaReferida,
                                    Descricao descricao,
                                    Usuario usuario,
                                    BigDecimal valor,
                                    LocalDate dataOcorrencia) {
        boolean exists = lancamentoRepository.existsByEmpresaAndCompetenciaAndCompetenciaReferidaAndDescricaoAndValorAndDataOcorrenciaAndTipo(
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
}
