package com.eleetricz.cashflow.parcelamento;

import com.eleetricz.cashflow.entity.Parcela;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ParcelaService {
    List<Parcela> listarTodas();
    Parcela buscarPorId(Long id);
    void registrarPagamentoComLancamento(Long idParcela, BigDecimal valorBase, BigDecimal valorEncargos,LocalDate dataPagamento, String competencia,String competenciaReferida);
}
