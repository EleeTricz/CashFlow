package com.eleetricz.cashflow.parcelamento;
import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.repository.LancamentoRepository;
import com.eleetricz.cashflow.repository.ParcelaRepository;
import com.eleetricz.cashflow.repository.ParcelamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParcelamentoServiceImpl implements ParcelamentoService {

    private final ParcelamentoRepository parcelamentoRepository;
    private final ParcelaRepository parcelaRepository;
    private final LancamentoRepository lancamentoRepository;
    @Override
    public void criarParcelamento(Parcelamento parcelamento) {
        // Buscar os objetos reais
        Empresa empresa = parcelamento.getEmpresa(); // com ID preenchido
        Descricao descricao = parcelamento.getDescricaoBase(); // com ID preenchido

        // Aqui você pode validar se os objetos existem
        if (empresa == null || empresa.getId() == null) throw new IllegalArgumentException("Empresa inválida");
        if (descricao == null || descricao.getId() == null) throw new IllegalArgumentException("Descrição inválida");

        // opcional: buscar do banco se quiser validar
        // empresa = empresaRepository.findById(empresa.getId()).orElseThrow();
        // descricao = descricaoRepository.findById(descricao.getId()).orElseThrow();

        parcelamento.setEmpresa(empresa);
        parcelamento.setDescricaoBase(descricao);

        // Gerar parcelas
        BigDecimal valorParcela = parcelamento.getValorTotal()
                .divide(BigDecimal.valueOf(parcelamento.getTotalParcelas()), 2, RoundingMode.HALF_EVEN);

        List<Parcela> parcelas = new ArrayList<>();
        LocalDate vencimento = parcelamento.getDataInicio();

        for (int i = 1; i <= parcelamento.getTotalParcelas(); i++) {
            Parcela parcela = new Parcela();
            parcela.setNumeroParcela(i);
            parcela.setVencimento(vencimento);
            parcela.setValorPrincipal(valorParcela);
            parcela.setPaga(false);
            parcela.setParcelamento(parcelamento);
            parcelas.add(parcela);
            vencimento = vencimento.plusMonths(1);
        }

        parcelamento.setParcelas(parcelas);
        parcelamento.setQuitado(false);

        parcelamentoRepository.save(parcelamento);
    }

    @Override
    public void registrarPagamento(Long idParcela, Lancamento lancamento) {

    }

    @Override
    public void preencherTotalPago(List<Parcelamento> parcelamentos) {
        for (Parcelamento p : parcelamentos) {
            BigDecimal totalPago = p.getParcelas().stream()
                    .filter(Parcela::isPaga)
                    .map(parcela -> {
                        BigDecimal principal = parcela.getValorPrincipal() != null ? parcela.getValorPrincipal() : BigDecimal.ZERO;
                        BigDecimal encargos = parcela.getValorEncargos() != null ? parcela.getValorEncargos() : BigDecimal.ZERO;
                        return principal.add(encargos);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            p.setValorTotalPago(totalPago);
        }
    }

    @Override
    @Transactional
    public void excluirParcelamento(Long parcelamentoId) {
        Parcelamento parcelamento = parcelamentoRepository.findById(parcelamentoId)
                .orElseThrow(() -> new RuntimeException("Parcelamento não encontrado"));

        // Deletar todos os lançamentos de todas as parcelas
        for (Parcela parcela : parcelamento.getParcelas()) {
            lancamentoRepository.deleteAll(parcela.getLancamentos());
        }

        // Agora o Cascade.ALL cuida das parcelas
        parcelamentoRepository.delete(parcelamento);
    }

}

