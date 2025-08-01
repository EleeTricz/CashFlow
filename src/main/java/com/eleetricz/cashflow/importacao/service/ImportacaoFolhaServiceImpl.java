package com.eleetricz.cashflow.importacao.service;


import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ImportacaoFolhaServiceImpl implements ImportacaoFolhaService {
    private final DescricaoRepository descricaoRepository;
    private final CompetenciaRepository competenciaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DateTimeFormatter dataFmt = DateTimeFormatter.ofPattern("ddMMyyyy");
    private final Pattern compPattern = Pattern.compile("(\\d{2}/\\d{4})");

    @Override
    public void importarFolha(MultipartFile file, Long empresaId) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            Usuario sistema = getUsuarioByName("admin");
            Empresa empresa = empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada: " + empresaId));
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(",");
                LocalDate data = LocalDate.parse(cols[1].trim(), dataFmt);
                Long contaCredito = Long.valueOf(cols[3].trim());
                BigDecimal valor = new BigDecimal(cols[4].trim());
                String complemento = cols[6].replaceAll("\"", "").trim();

                String nomeDescricao;

                if (contaCredito == 1634L) {
                    if (complemento.toLowerCase().contains("rescisao")) {
                        nomeDescricao = "RESCISAO";
                    } else if (complemento.toLowerCase().contains("liquido do 13o salario adiantamento")) {
                        nomeDescricao = "ADIANTAMENTO 13º";
                    } else if (complemento.toLowerCase().contains("liquido de 13o salario")) {
                        nomeDescricao = "SALARIO INTEGRAL 13º";
                    } else {
                        nomeDescricao = "ORDENADOS SALARIOS";
                    }
                } else {
                    Map<Long, String> contaParaDescricao = Map.of(
                            1635L, "PROLABORE",
                            313L, "FERIAS"
                    );

                    nomeDescricao = contaParaDescricao.get(contaCredito);

                    if (nomeDescricao == null) {
                        throw new IllegalArgumentException("Conta crédito não mapeada: " + contaCredito);
                    }
                }

                Descricao descricao = descricaoRepository.findByNomeIgnoreCase(nomeDescricao)
                        .orElseThrow(() -> new IllegalArgumentException("Descrição " + nomeDescricao + " não encontrada"));

                Matcher m = compPattern.matcher(complemento);
                if (!m.find()) {
                    throw new IllegalArgumentException("Competência inválida no complemento: " + complemento);
                }
                String compStr = m.group(1);
                Competencia competencia = criarOuObterCompetencia(compStr, empresa);

                salvarSeNaoExistir(empresa, competencia,competencia, descricao, sistema, valor, data);

            }
        }
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
                                    LocalDate dataOcorrencia)
    {
        boolean exists = lancamentoRepository.existsByEmpresaAndCompetenciaAndCompetenciaReferidaAndDescricaoAndValorAndDataOcorrenciaAndTipo(
                empresa, competencia, competenciaReferida,descricao, valor, dataOcorrencia, TipoLancamento.SAIDA
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
                            .tipo(TipoLancamento.SAIDA)
                            .build()
            );
        }
    }
    private Usuario getUsuarioByName(String nome) {
        return usuarioRepository.findByNome(nome)
                .orElseThrow(() -> new IllegalStateException("Usuário '" + nome + "' não encontrado"));
    }

}