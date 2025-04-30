package com.eleetricz.cashflow.service;


import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LancamentoDaeServiceImpl implements LancamentoDaeService{
    private final LancamentoDaeRepository daeRepo;
    private final LancamentoRepository lancRepo;
    private final DescricaoRepository descRepo;
    private final CompetenciaRepository compRepo;
    private final EmpresaRepository empRepo;
    private final UsuarioRepository userRepo;

    @Transactional
    public void importarTodos(){
        Descricao daeDesc = getDescricaoByName("DAE 10");
        Usuario sistema = getUsuarioByName("admin");

        Map<Long, Empresa> empresaCache = new HashMap<>();
        Map<String, Competencia> competenciaCache = new HashMap<>();

        for (LancamentoDae rec : daeRepo.findAll()) {
            Empresa empresa = empresaCache.computeIfAbsent(
                    rec.getEmpresa().getId(),
                    id -> empRepo.findById(id).orElseThrow(()  ->
                            new IllegalStateException("Empresa id=" + id + " não existe"))
            );

            Competencia competenciaReferida = getOrCacheCompetencia(competenciaCache, rec.getCompetenciaReferida(), empresa);
            Competencia competencia = getOrCacheCompetencia(competenciaCache, rec.getCompetencia(), empresa);
            LocalDate dataOcorrencia = rec.getDataArrecadacao();
            String valorFormatado = rec.getValor()
                    .replaceAll("[^\\d,]", "")  // Remove tudo que não for dígito ou vírgula
                    .replace(",", ".");

            BigDecimal valor = new BigDecimal(valorFormatado);


            salvarSeNaoExistir(empresa, competencia, competenciaReferida, daeDesc, sistema, valor, dataOcorrencia);
        }


    }

    private Descricao getDescricaoByName(String nome){
        return descRepo.findByNome(nome)
                .orElseThrow(() -> new IllegalStateException("Descrição '" + nome + "' não encontrada"));
    }

    private Usuario getUsuarioByName(String nome) {
        return userRepo.findByNome(nome)
                .orElseThrow(() -> new IllegalStateException("Usuário '" + nome + "' não encontrado"));
    }

    private Competencia obterOuCriarCompetencia(int mes, int ano, Empresa empresa) {
        return compRepo.findByMesAndAnoAndEmpresa(mes, ano, empresa)
                .orElseGet(() -> {
                    Competencia nova = new Competencia();
                    nova.setMes(mes);
                    nova.setAno(ano);
                    nova.setEmpresa(empresa);
                    return compRepo.save(nova);
                });
    }

    private Competencia getOrCacheCompetencia(Map<String, Competencia> cache, String chave, Empresa empresa) {
        return cache.computeIfAbsent(chave + "-" + empresa.getId(), key -> {
            String[] partes = chave.split("/");
            int mes = Integer.parseInt(partes[0]);
            int ano = Integer.parseInt(partes[1]);
            return obterOuCriarCompetencia(mes, ano, empresa);
        });
    }

    private void salvarSeNaoExistir(Empresa empresa, Competencia competencia, Competencia competenciaReferida,
                                    Descricao descricao, Usuario usuario, BigDecimal valor, LocalDate dataOcorrencia) {
        boolean exists = lancRepo.existsByEmpresaAndCompetenciaAndCompetenciaReferidaAndDescricaoAndValorAndDataOcorrenciaAndTipo(
                empresa, competencia, competenciaReferida, descricao, valor, dataOcorrencia, TipoLancamento.SAIDA);

        if (!exists) {
            lancRepo.save(Lancamento.builder()
                    .empresa(empresa)
                    .competencia(competencia)
                    .competenciaReferida(competenciaReferida)
                    .descricao(descricao)
                    .usuario(usuario)
                    .valor(valor)
                    .dataOcorrencia(dataOcorrencia)
                    .tipo(TipoLancamento.SAIDA)
                    .build());
        }
    }

}
