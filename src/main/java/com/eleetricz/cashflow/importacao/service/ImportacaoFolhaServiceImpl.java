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
import java.util.Locale;
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
                if (line.isBlank()) {
                    continue;
                }
                // Novo layout: 1, ddMMyyyy, 0, 5, valor, codigo,"complemento" — limit 7 preserva vírgulas no texto
                String[] cols = line.split(",", 7);
                if (cols.length < 7) {
                    throw new IllegalArgumentException("Linha inválida (esperados 7 campos): " + line);
                }
                LocalDate data = LocalDate.parse(cols[1].trim(), dataFmt);
                BigDecimal valor = new BigDecimal(cols[4].trim());
                String complemento = cols[6].replace("\"", "").trim();

                String nomeDescricao = resolverNomeDescricao(complemento);

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

    /**
     * Layout atual: tipo definido pelo texto do complemento (conta contábil fixa no arquivo, ex.: 5).
     * Mantém compatibilidade com textos do layout antigo (1634/1635/313) quando o complemento for o mesmo padrão.
     */
    private String resolverNomeDescricao(String complemento) {
        String c = complemento.toLowerCase(Locale.ROOT);
        if (c.contains("rescisao") || c.contains("rescisão")) {
            return "RESCISAO";
        }
        if (c.contains("pro-labore") || c.contains("prolabore")) {
            return "PROLABORE";
        }
        if (c.contains("adiantamento") && (c.contains("13o") || c.contains("13º") || c.contains("13"))) {
            return "ADIANTAMENTO 13º";
        }
        if ((c.contains("liquido de 13o salario") || c.contains("liquido de 13º salario")
                || c.contains("líquido de 13o salario") || c.contains("líquido de 13º salario"))
                && !c.contains("adiantamento")) {
            return "SALARIO INTEGRAL 13º";
        }
        if (c.contains("ferias") || c.contains("férias")) {
            return "FERIAS";
        }
        if (c.contains("liquido da folha") || c.contains("líquido da folha")) {
            return "ORDENADOS SALARIOS";
        }
        throw new IllegalArgumentException("Complemento não mapeado para descrição conhecida: " + complemento);
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