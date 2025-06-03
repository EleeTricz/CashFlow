package com.eleetricz.cashflow.parcelamento;

import com.eleetricz.cashflow.entity.Lancamento;
import com.eleetricz.cashflow.entity.Parcelamento;

import java.util.List;

public interface ParcelamentoService {
    void criarParcelamento(Parcelamento parcelamento);
    void registrarPagamento(Long idParcela, Lancamento lancamento);
    void preencherTotalPago(List<Parcelamento> parcelamentos);
    void excluirParcelamento(Long id);
}
