package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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
            for (ItensDarf item : itens) {
                BigDecimal principal = parse(item.getPrincipal());
                BigDecimal multa     = parse(item.getMulta());
                BigDecimal juros     = parse(item.getJuros());
                BigDecimal encargos  = multa.add(juros);

                Descricao descPrinc = getOrCreateDescricao(item.getDescricao());
                Descricao descEnc   = getOrCreateDescricao("Encargos " + item.getDescricao());

                if (principal.compareTo(BigDecimal.ZERO) > 0) {
                    salvarSeNaoExistir(
                            empresa,
                            competencia,
                            competenciaReferida,
                            descPrinc,
                            sistema,
                            principal,
                            dataOcorrencia
                    );
                }

                if (encargos.compareTo(BigDecimal.ZERO) > 0) {
                    salvarSeNaoExistir(
                            empresa,
                            competencia,
                            competenciaReferida,
                            descEnc,
                            sistema,
                            encargos,
                            dataOcorrencia
                    );
                }
            }
        }
    }

    private Descricao getOrCreateDescricao(String nome) {
        // Mapeamento de descrições para padronizar os nomes
        String descricao = mapearDescricao(nome);
        return descricaoRepo.findByNome(descricao)
                .orElseGet(() -> descricaoRepo.save(Descricao.builder().nome(descricao).build()));
    }

    private String mapearDescricao(String original) {
        if (original == null) return "Outro";

        String desc = original.trim().toUpperCase();

        // Mapeamento das descrições específicas
        if (desc.contains("ENCARGOS CP DESCONTADA SEGURADO")) return "ENCARGOS INSS";
        if (desc.contains("CP DESCONTADA SEGURADO")) return "INSS";
        if (desc.contains("GPS")) return "INSS";
        if (desc.contains("DAS")) return "DAS";
        if (desc.contains("FGTS")) return "FGTS";
        if (desc.contains("IRRF") || desc.contains("IRPF")) return "IR";
        if (desc.contains("DARF") && desc.contains("CSLL")) return "CSLL";
        if (desc.contains("DARF") && desc.contains("COFINS")) return "COFINS";
        if (desc.contains("DARF") && desc.contains("PIS")) return "PIS";
        if (desc.contains("DARF") && desc.contains("IRPJ")) return "IRPJ";

        // padrão geral (manter o original se nada bater)
        return desc;
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

}
