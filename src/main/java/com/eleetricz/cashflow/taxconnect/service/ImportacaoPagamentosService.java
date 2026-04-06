package com.eleetricz.cashflow.taxconnect.service;

import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.repository.*;
import com.eleetricz.cashflow.service.LancamentoDarfServiceImpl;
import com.eleetricz.cashflow.service.LancamentoDasServiceImpl;
import com.eleetricz.cashflow.taxconnect.dto.PagamentoNormalizadoDTO;
import com.eleetricz.cashflow.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImportacaoPagamentosService {
    private final LancamentoDarfRepository darfRepository;
    private final LancamentoDasRepository dasRepository;
    private final EmpresaRepository empresaRepository;
    private final LancamentoDasServiceImpl lancamentoDasService;
    private final LancamentoDarfServiceImpl lancamentoDarfService;
    private final FechamentoStatusRepository fechamentoStatusRepository;
    private final CompetenciaRepository competenciaRepository;

    public void importar(Long empresaId, List<PagamentoNormalizadoDTO> pagamentos) {

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

        // Agrupa DARFs pelo número do documento
        Map<String, List<PagamentoNormalizadoDTO>> darfAgrupados = new HashMap<>();

        for (PagamentoNormalizadoDTO dto : pagamentos) {

            if (dto.getTipoDocumento().equalsIgnoreCase(
                    "DOCUMENTO DE ARRECADAÇÃO DO SIMPLES NACIONAL")) {

                importarDas(dto, empresa);
            }

            if (dto.getTipoDocumento().equalsIgnoreCase(
                    "DOCUMENTO DE ARRECADAÇÃO DE RECEITAS FEDERAIS")
                    || dto.getTipoDocumento().equalsIgnoreCase("DARF IRRF")) {

                darfAgrupados
                        .computeIfAbsent(dto.getNumeroDocumento(), k -> new ArrayList<>())
                        .add(dto);
            }
        }

        darfAgrupados.forEach((numero, itens) ->
                importarDarf(numero, itens, empresa)
        );

        lancamentoDarfService.importarTodosIntegra();
        lancamentoDasService.importarTodos();
    }

    private void importarDas(PagamentoNormalizadoDTO dto, Empresa empresa) {

        if (dasRepository.existsByNumeroDocumento(dto.getNumeroDocumento())) {
            return; // evita duplicidade
        }

        LancamentoDas das = LancamentoDas.builder()
                .empresaId(empresa)
                .competencia(dto.getCompetenciaApuracao())
                .dataVencimento(LocalDate.parse(dto.getDataVencimento(), Utils.DATE_FORMATTER))
                .dataArrecadacao(LocalDate.parse(dto.getDataPagamento(), Utils.DATE_FORMATTER))
                .principal(dto.getValorPrincipal().toPlainString())
                .multa(dto.getValorMulta().toPlainString())
                .juros(dto.getValorJuros().toPlainString())
                .total(dto.getValorTotal().toPlainString())
                .numeroDocumento(dto.getNumeroDocumento())
                .encargosDas(dto.getValorMulta().add(dto.getValorJuros()).toPlainString())
                .build();

        dasRepository.save(das);

        atualizarStatusCompetencia(
                empresa,
                dto.getCompetenciaApuracao(),
                status -> status.setSimplesStatus(StatusTarefa.CONCLUIDO)
        );
    }

    private void importarDarf(
            String numeroDocumento,
            List<PagamentoNormalizadoDTO> itens,
            Empresa empresa) {

        if (darfRepository.existsByNumeroDocumento(numeroDocumento)) {
            return;
        }

        BigDecimal totalPrincipal = BigDecimal.ZERO;
        BigDecimal totalMulta = BigDecimal.ZERO;
        BigDecimal totalJuros = BigDecimal.ZERO;

        List<ItensDarf> itensDarf = new ArrayList<>();

        LancamentoDarf darf = LancamentoDarf.builder()
                .empresa(empresa)
                .numeroDocumento(numeroDocumento)
                .periodoApuracao(itens.get(0).getCompetenciaApuracao())
                .competencia(itens.get(0).getCompetenciaPagamento())
                .dataVencimento(LocalDate.parse(itens.get(0).getDataVencimento(), Utils.DATE_FORMATTER))
                .dataArrecadacao(LocalDate.parse(itens.get(0).getDataPagamento(), Utils.DATE_FORMATTER))
                .build();

        for (PagamentoNormalizadoDTO dto : itens) {

            totalPrincipal = totalPrincipal.add(dto.getValorPrincipal());
            totalMulta = totalMulta.add(dto.getValorMulta());
            totalJuros = totalJuros.add(dto.getValorJuros());

            ItensDarf item = ItensDarf.builder()
                    .lancamentoDarf(darf)
                    .codigo(dto.getCodigoReceita())
                    .descricao(dto.getDescricaoReceita())
                    .principal(dto.getValorPrincipal().toPlainString())
                    .multa(dto.getValorMulta().toPlainString())
                    .juros(dto.getValorJuros().toPlainString())
                    .total(dto.getValorTotal().toPlainString())
                    .build();

            itensDarf.add(item);
        }

        darf.setTotalPrincipal(totalPrincipal.toPlainString());
        darf.setTotalMulta(totalMulta.toPlainString());
        darf.setTotalJuros(totalJuros.toPlainString());
        darf.setTotalTotal(
                totalPrincipal.add(totalMulta).add(totalJuros).toPlainString()
        );

        darf.setItensDarf(itensDarf);

        darfRepository.save(darf);

        atualizarStatusCompetencia(
                empresa,
                itens.get(0).getCompetenciaApuracao(),
                status -> status.setInssStatus(StatusTarefa.CONCLUIDO)
        );
    }

    private void atualizarStatusCompetencia(
            Empresa empresa,
            String competenciaString,
            java.util.function.Consumer<FechamentoStatus> updater
    ) {
        if (competenciaString == null || competenciaString.isBlank()) {
            return;
        }

        String[] partes = competenciaString.split("/");
        int mes = Integer.parseInt(partes[0]);
        int ano = Integer.parseInt(partes[1]);

        Competencia competencia = competenciaRepository
                .findByMesAndAnoAndEmpresa(mes, ano, empresa)
                .orElseGet(() -> competenciaRepository.save(
                        Competencia.builder()
                                .mes(mes)
                                .ano(ano)
                                .empresa(empresa)
                                .build()
                ));

        FechamentoStatus status = fechamentoStatusRepository
                .findByEmpresaAndCompetencia(empresa, competencia)
                .orElseGet(() -> FechamentoStatus.builder()
                        .empresa(empresa)
                        .competencia(competencia)
                        .build());

        updater.accept(status);

        fechamentoStatusRepository.save(status);
    }
}


