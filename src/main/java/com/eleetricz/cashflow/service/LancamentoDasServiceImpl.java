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
public class LancamentoDasServiceImpl implements LancamentoDasService {
    private final LancamentoDasRepository dasRepo;
    private final LancamentoRepository lancRepo;
    private final DescricaoRepository descRepo;
    private final CompetenciaRepository compRepo;
    private final EmpresaRepository empRepo;
    private final UsuarioRepository userRepo;

    @Transactional
    public void importarTodos() {
        Descricao dasDesc = getDescricaoById(9L);
        Descricao encDasDesc = getDescricaoById(10L);
        Usuario sistema = getUsuarioByName("admin");

        Map<Long, Empresa> empresaCache = new HashMap<>();
        Map<String, Competencia> competenciaCache = new HashMap<>();

        for (LancamentoDas rec : dasRepo.findAll()) {
            Empresa empresa = empresaCache.computeIfAbsent(
                    rec.getEmpresaId().getId(),
                    id -> empRepo.findById(id).orElseThrow(() ->
                            new IllegalStateException("Empresa id=" + id + " não existe"))
            );

            Competencia competenciaReferida = getOrCacheCompetencia(competenciaCache, rec.getCompetencia(), empresa);
            LocalDate dataOcorrencia = rec.getDataArrecadacao();

            String keyComp = dataOcorrencia.getMonthValue() + "/" + dataOcorrencia.getYear();
            Competencia competencia = getOrCacheCompetencia(competenciaCache, keyComp, empresa);

            BigDecimal principal = new BigDecimal(rec.getPrincipal());
            BigDecimal multa = new BigDecimal(rec.getMulta());
            BigDecimal juros = new BigDecimal(rec.getJuros());
            BigDecimal total = new BigDecimal(rec.getTotal());
            BigDecimal encargosInformado = new BigDecimal(rec.getEncargosDas());

            // Calcula a soma real de multa + juros
            BigDecimal encargosCalculado = multa.add(juros);

            // Se houver diferença, usa o valor calculado
            if (encargosInformado.compareTo(encargosCalculado) != 0) {
                encargosInformado = encargosCalculado;
            }

            System.out.println("Encargos usados: " + encargosInformado);

            // Lógica de lançamento
            if (encargosInformado.compareTo(BigDecimal.ZERO) > 0) {
                salvarSeNaoExistir(empresa, competencia, competenciaReferida, dasDesc, sistema, principal, dataOcorrencia);
                salvarSeNaoExistir(empresa, competencia, competenciaReferida, encDasDesc, sistema, encargosInformado, dataOcorrencia);
            } else {
                salvarSeNaoExistir(empresa, competencia, competenciaReferida, dasDesc, sistema, total, dataOcorrencia);
            }
        }
    }

    private Descricao getDescricaoByName(String nome) {
        return descRepo.findByNome(nome)
                .orElseThrow(() -> new IllegalStateException("Descrição '" + nome + "' não encontrada"));
    }

    private Descricao getDescricaoById(Long id){
        return descRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Descrição: '" + id + "' não encontrada"));
    }

    private Usuario getUsuarioByName(String nome) {
        return userRepo.findByNome(nome)
                .orElseThrow(() -> new IllegalStateException("Usuário '" + nome + "' não encontrado"));
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

    @Override
    public LancamentoDas salvar(LancamentoDas lancamentoDas) {
        return dasRepo.save(lancamentoDas);
    }

    @Override
    public boolean registroJaExiste(Long empresaId, String competencia, String numeroDocumento) {
        return dasRepo.existsByEmpresaId_IdAndCompetenciaAndNumeroDocumento(empresaId, competencia, numeroDocumento);
    }

}
