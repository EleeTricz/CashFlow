package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.dto.DarfData;
import com.eleetricz.cashflow.dto.DasData;
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
public class LancamentoDarfServiceImpl implements LancamentoDarfService {
    private final LancamentoDarfRepository darfRepo;
    private final ItensDarfRepository itensRepo;
    private final EmpresaRepository empresaRepo;
    private final CompetenciaRepository competenciaRepo;
    private final DescricaoRepository descricaoRepo;
    private final UsuarioRepository usuarioRepo;
    private final LancamentoRepository lancamentoRepo;
    private static final DateTimeFormatter BR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter COMP_DATE = DateTimeFormatter.ofPattern("MM/yyyy");

    @Transactional
    @Override
    public void importarTodos() {
        Usuario sistema = getUsuarioByName("admin");

        // caches para poupar consultas repetidas
        Map<Long, Empresa> empresaCache = new HashMap<>();
        Map<String, Competencia> competenciaReferidaCache = new HashMap<>();
        Map<String, Competencia> competenciaPorDataCache  = new HashMap<>();

        for (LancamentoDarf darf : darfRepo.findAll()) {
            Empresa empresa = empresaCache.computeIfAbsent(
                    darf.getEmpresa().getId(),
                    id -> empresaRepo.findById(id)
                            .orElseThrow(() -> new IllegalStateException("Empresa id=" + id + " não existe"))
            );

            // Obtém ou cria a competência informada no DARF (campo novo)
            String chaveCompReferida = darf.getCompetencia() + "-" + empresa.getId();
            Competencia competenciaReferida = competenciaReferidaCache.computeIfAbsent(
                    chaveCompReferida,
                    key -> criarOuObterCompetencia(darf.getCompetencia(), empresa)
            );

            // Obtém ou cria a competência com base na data de arrecadação (para controle interno)
            LocalDate dataOcorrencia = darf.getDataArrecadacao();
            String chaveCompPorData = dataOcorrencia.getMonthValue() + "/" + dataOcorrencia.getYear() + "-" + empresa.getId();
            Competencia competencia = competenciaPorDataCache.computeIfAbsent(
                    chaveCompPorData,
                    key -> criarOuObterCompetencia(
                            dataOcorrencia.getMonthValue() + "/" + dataOcorrencia.getYear(),
                            empresa
                    )
            );

            // para cada item da DARF, geramos lançamentos
            List<ItensDarf> itens = itensRepo.findByLancamentoDarf(darf);
            BigDecimal totalINSS = BigDecimal.ZERO;
            BigDecimal encargosINSS = BigDecimal.ZERO;

            BigDecimal totalIR = BigDecimal.ZERO;
            BigDecimal encargosIR = BigDecimal.ZERO;

            BigDecimal totalMultaCLT = BigDecimal.ZERO;
            BigDecimal encargosMultaCLT = BigDecimal.ZERO;

            for (ItensDarf item : itens) {
                String descricaoItem = item.getDescricao().toUpperCase();
                BigDecimal principal = parse(item.getPrincipal());
                BigDecimal multa     = parse(item.getMulta());
                BigDecimal juros     = parse(item.getJuros());
                BigDecimal encargos  = multa.add(juros);


                if (descricaoItem.contains("13 SALÁRIO")) {
                    // Soma principal e encargos separadamente
                    BigDecimal valorPrincipal13 = principal;
                    BigDecimal valorEncargos13 = encargos;

                    if (valorPrincipal13.compareTo(BigDecimal.ZERO) > 0) {
                        Descricao desc13 = getOrCreateDescricao("INSS 13º");
                        salvarSeNaoExistir(empresa, competencia, competenciaReferida,
                                desc13, sistema, valorPrincipal13, dataOcorrencia);
                    }

                    if (valorEncargos13.compareTo(BigDecimal.ZERO) > 0) {
                        Descricao descEnc13 = getOrCreateDescricao("ENCARGOS INSS 13º");
                        salvarSeNaoExistir(empresa, competencia, competenciaReferida,
                                descEnc13, sistema, valorEncargos13, dataOcorrencia);
                    }

                    continue;
                }

                if (descricaoItem.contains("CONTR PREV DESCONTA SEGURADO") ||
                        descricaoItem.contains("CP DESCONTADA SEGURADO")) {

                    totalINSS = totalINSS.add(principal);
                    encargosINSS = encargosINSS.add(encargos);

                } else if (descricaoItem.contains("IRRF")) {
                    totalIR = totalIR.add(principal);
                    encargosIR = encargosIR.add(encargos);

                } else if (descricaoItem.contains("MULTA DA CLT") || descricaoItem.contains("MULTA DA CLT") ) {
                    totalMultaCLT = totalMultaCLT.add(principal);
                    encargosMultaCLT = encargosMultaCLT.add(encargos);
                }


            }

            // Salvar lançamentos de INSS
            if (totalINSS.compareTo(BigDecimal.ZERO) > 0) {
                Descricao descINSS = getOrCreateDescricao("INSS");
                salvarSeNaoExistir(empresa, competencia, competenciaReferida, descINSS, sistema, totalINSS, dataOcorrencia);
            }
            if (encargosINSS.compareTo(BigDecimal.ZERO) > 0) {
                Descricao descEncINSS = getOrCreateDescricao("ENCARGOS INSS");
                salvarSeNaoExistir(empresa, competencia, competenciaReferida, descEncINSS, sistema, encargosINSS, dataOcorrencia);
            }

            // Salvar lançamentos de IR
            if (totalIR.compareTo(BigDecimal.ZERO) > 0) {
                Descricao descIR = getOrCreateDescricao("IRRF");
                salvarSeNaoExistir(empresa, competencia, competenciaReferida, descIR, sistema, totalIR, dataOcorrencia);
            }
            if (encargosIR.compareTo(BigDecimal.ZERO) > 0) {
                Descricao descEncIR = getOrCreateDescricao("ENCARGOS IRRF");
                salvarSeNaoExistir(empresa, competencia, competenciaReferida, descEncIR, sistema, encargosIR, dataOcorrencia);
            }

            // Salvar lançamentos de Multa da CLT
            if (totalMultaCLT.compareTo(BigDecimal.ZERO) > 0) {
                Descricao descMultaCLT = getOrCreateDescricao("MULTA CLT");
                salvarSeNaoExistir(empresa, competencia, competenciaReferida, descMultaCLT, sistema, totalIR, dataOcorrencia);
            }
            if (encargosMultaCLT.compareTo(BigDecimal.ZERO) > 0) {
                Descricao descEncMultaCLT = getOrCreateDescricao("ENCARGOS MULTA CLT");
                salvarSeNaoExistir(empresa, competencia, competenciaReferida, descEncMultaCLT, sistema, encargosIR, dataOcorrencia);
            }

        }
    }

    private Descricao getOrCreateDescricao(String nome) {
        return descricaoRepo.findByNome(nome)
                .orElseGet(() -> descricaoRepo.save(Descricao.builder().nome(nome).build()));
    }

    private Usuario getUsuarioByName(String nome) {
        return usuarioRepo.findByNome(nome)
                .orElseThrow(() -> new IllegalStateException("Usuário '" + nome + "' não encontrado"));
    }

    private Competencia criarOuObterCompetencia(String chave, Empresa empresa) {
        // chave esperada: "MM/YYYY"
        String[] p = chave.split("/");
        int mes = Integer.parseInt(p[0]), ano = Integer.parseInt(p[1]);
        return competenciaRepo.findByMesAndAnoAndEmpresa(mes, ano, empresa)
                .orElseGet(() -> competenciaRepo.save(
                        Competencia.builder()
                                .mes(mes)
                                .ano(ano)
                                .empresa(empresa)
                                .build()
                ));
    }

    private BigDecimal parse(String s) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        return new BigDecimal(s.replace(",", "."));
    }

    private void salvarSeNaoExistir(Empresa empresa,
                                    Competencia competencia,
                                    Competencia competenciaReferida,
                                    Descricao descricao,
                                    Usuario usuario,
                                    BigDecimal valor,
                                    LocalDate dataOcorrencia)
    {
        boolean exists = lancamentoRepo.existsByEmpresaAndCompetenciaAndCompetenciaReferidaAndDescricaoAndValorAndDataOcorrenciaAndTipo(
                empresa, competencia, competenciaReferida,descricao, valor, dataOcorrencia, TipoLancamento.SAIDA
        );
        if (!exists) {
            lancamentoRepo.save(
                    Lancamento.builder()
                            .empresa(empresa)
                            .competenciaReferida(competenciaReferida)
                            .competencia(competencia)
                            .descricao(descricao)
                            .usuario(usuario)
                            .valor(valor)
                            .dataOcorrencia(dataOcorrencia)
                            .tipo(TipoLancamento.SAIDA)
                            .build()
            );
        }
    }


    public boolean registroJaExiste(Empresa empresa, String competencia, String numeroDocumento){
        return darfRepo.existsByEmpresaAndCompetenciaAndNumeroDocumento(empresa, competencia, numeroDocumento);
    }

    public LancamentoDarf salvar(LancamentoDarf lancamentoDarf) {
        return darfRepo.save(lancamentoDarf);
    }

    @Transactional
    @Override
    public int importarDadosPdfDarf(Long empresaId, List<DarfData> dadosExtraidos) {
        int inseridos = 0;

        // Busca empresa e usuário "sistema"
        Empresa empresa = empresaRepo.findById(empresaId)
                .orElseThrow(() -> new IllegalStateException("Empresa id=" + empresaId + " não existe"));

        for (DarfData data : dadosExtraidos){

            LocalDate dataApuracao = LocalDate.parse(data.getPeriodoApuracao(), BR_DATE);
            String competencia = dataApuracao.format(COMP_DATE);

            if(!registroJaExiste(empresa, competencia, data.getNumeroDocumento())){
                LancamentoDarf lanc = new LancamentoDarf();
                lanc.setEmpresa(empresa);
                lanc.setCompetencia(competencia);
                lanc.setDataArrecadacao(LocalDate.parse(data.getDataArrecadacao(), BR_DATE));
                lanc.setDataVencimento(LocalDate.parse(data.getDataVencimento(), BR_DATE));
                lanc.setTotalPrincipal(data.getTotalPrincipal());
                lanc.setTotalMulta(data.getTotalMulta());
                lanc.setTotalJuros(data.getTotalJuros());
                lanc.setTotalTotal(data.getTotalTotal());
                lanc.setNumeroDocumento(data.getNumeroDocumento());
                lanc.setPeriodoApuracao(data.getPeriodoApuracao());

                // Associar cada item ao lançamento
                List<ItensDarf> itens = data.getItens();
                for (ItensDarf item : itens) {
                    item.setLancamentoDarf(lanc); // <- ESSA LINHA É ESSENCIAL
                }
                lanc.setItensDarf(itens);


                salvar(lanc);
                inseridos++;
            }
        }

        return inseridos;
    }
}
