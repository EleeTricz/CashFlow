package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.Parcela;
import com.eleetricz.cashflow.entity.Parcelamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParcelaRepository extends JpaRepository<Parcela, Long> {
    List<Parcela> findByParcelamentoIdOrderByNumeroParcelaAsc(Long id);
    Optional<Parcela> findByParcelamentoAndNumeroParcela(
            Parcelamento parcelamento,
            Integer numeroParcela
    );
}
