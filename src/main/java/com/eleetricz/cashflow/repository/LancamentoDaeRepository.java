package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.LancamentoDae;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LancamentoDaeRepository extends JpaRepository<LancamentoDae, Long> {
    void deleteByEmpresa_Id(Long empresaId);
}
