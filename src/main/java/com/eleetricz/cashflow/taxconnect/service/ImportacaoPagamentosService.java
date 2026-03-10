package com.eleetricz.cashflow.taxconnect.service;

import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.ItensDarf;
import com.eleetricz.cashflow.entity.LancamentoDarf;
import com.eleetricz.cashflow.entity.LancamentoDas;
import com.eleetricz.cashflow.repository.EmpresaRepository;
import com.eleetricz.cashflow.repository.LancamentoDarfRepository;
import com.eleetricz.cashflow.repository.LancamentoDasRepository;
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
    }
}


