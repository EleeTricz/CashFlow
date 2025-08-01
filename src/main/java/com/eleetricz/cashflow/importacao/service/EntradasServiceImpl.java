package com.eleetricz.cashflow.importacao.service;

import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.repository.CompetenciaRepository;
import com.eleetricz.cashflow.repository.DescricaoRepository;
import com.eleetricz.cashflow.repository.LancamentoRepository;
import com.eleetricz.cashflow.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EntradasServiceImpl implements EntradasService {

    private final DescricaoRepository descricaoRepository;
    private final LancamentoRepository lancamentoRepository;
    private final CompetenciaRepository competenciaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public void importarLancamentosDeExtrato(File file, Empresa empresa, int anoBase) throws IOException {

        Descricao descricaoReceita = descricaoRepository.findByNomeIgnoreCase("RECEITAS DE VENDAS")
                .orElseThrow(() -> new RuntimeException("Descrição 'Receitas de Vendas' não encontrada."));

        Usuario sistema = getUsuarioByName("admin");

        Map<String, BigDecimal> somaPorCompetencia = new TreeMap<>();
        List<String> linhas = Files.readAllLines(file.toPath(), StandardCharsets.ISO_8859_1);

        int anoAtual = anoBase;
        Integer mesAnterior = null;
        LocalDate dataTransacaoAtual = null;

        for (String linha : linhas) {
            linha = linha.trim();

            // Separador de transação
            if (linha.matches("^-{50,}$")) {
                dataTransacaoAtual = null;
                continue;
            }

            // Início de nova transação
            Matcher matcherTransacao = Pattern.compile("^(\\d+)\\s+(\\d{2})/(\\d{2})").matcher(linha);
            if (matcherTransacao.find()) {
                int dia = Integer.parseInt(matcherTransacao.group(2));
                int mes = Integer.parseInt(matcherTransacao.group(3));

                if (mesAnterior != null && mesAnterior == 12 && mes == 1) {
                    anoAtual++;
                }
                mesAnterior = mes;

                String competencia = String.format("%02d/%d", mes, anoAtual);

                BigDecimal valor = extrairValorMonetario(linha);
                if (valor != null) {
                    somaPorCompetencia.put(competencia,
                            somaPorCompetencia.getOrDefault(competencia, BigDecimal.ZERO).add(valor));
                }

                dataTransacaoAtual = LocalDate.of(anoAtual, mes, dia);
                continue;
            }

            // Linhas detalhadas da transação
            if (dataTransacaoAtual != null &&
                    !linha.startsWith("Total") &&
                    !linha.startsWith("Natureza:") &&
                    !linha.startsWith("Fornecedor:") &&
                    !linha.startsWith("Observação:")) {

                int mes = dataTransacaoAtual.getMonthValue();
                int ano = dataTransacaoAtual.getYear();
                String competencia = String.format("%02d/%d", mes, ano);

                BigDecimal valor = extrairValorMonetario(linha);
                if (valor != null) {
                    somaPorCompetencia.put(competencia,
                            somaPorCompetencia.getOrDefault(competencia, BigDecimal.ZERO).add(valor));
                }
            }
        }

        // Criação de lançamentos por competência
        List<Lancamento> lancamentos = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : somaPorCompetencia.entrySet()) {
            String competenciaStr = entry.getKey();
            BigDecimal valor = entry.getValue();

            Competencia competenciaEntity = criarOuObterCompetencia(competenciaStr, empresa);
            LocalDate dataOcorrencia = YearMonth.of(competenciaEntity.getAno(), competenciaEntity.getMes())
                    .atEndOfMonth();

            salvarSeNaoExistir(
                    empresa,
                    competenciaEntity,
                    competenciaEntity,
                    descricaoReceita,
                    sistema,
                    valor,
                    dataOcorrencia
            );

        }
    }

    private BigDecimal extrairValorMonetario(String linha) {
        Matcher matcherValor = Pattern.compile("(\\d{1,3}(?:\\.\\d{3})*,\\d{2})").matcher(linha);
        if (matcherValor.find()) {
            String valorStr = matcherValor.group(1).replace(".", "").replace(",", ".");
            try {
                return new BigDecimal(valorStr);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Competencia criarOuObterCompetencia(String chave, Empresa empresa) {
        // chave esperada: "MM/YYYY"
        String[] p = chave.split("/");
        int mes = Integer.parseInt(p[0]), ano = Integer.parseInt(p[1]);
        return competenciaRepository.findByMesAndAnoAndEmpresa(mes, ano, empresa)
                .orElseGet(() -> competenciaRepository.save(
                        Competencia.builder()
                                .mes(mes)
                                .ano(ano)
                                .empresa(empresa)
                                .build()
                ));
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
                            .competenciaReferida(competenciaReferida)
                            .competencia(competencia)
                            .descricao(descricao)
                            .usuario(usuario)
                            .valor(valor)
                            .dataOcorrencia(dataOcorrencia)
                            .tipo(TipoLancamento.ENTRADA)
                            .build()
            );
        }
    }

    private Usuario getUsuarioByName(String nome) {
        return usuarioRepository.findByNome(nome)
                .orElseThrow(() -> new IllegalStateException("Usuário '" + nome + "' não encontrado"));
    }
}
