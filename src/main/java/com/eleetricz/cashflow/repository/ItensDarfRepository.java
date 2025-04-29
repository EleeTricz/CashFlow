package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.ItensDarf;
import com.eleetricz.cashflow.entity.LancamentoDarf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItensDarfRepository extends JpaRepository<ItensDarf, Long> {
    List<ItensDarf> findByLancamentoDarf(LancamentoDarf darf);
}
