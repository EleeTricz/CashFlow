package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LancamentoDarfRepository extends JpaRepository<LancamentoDarf, Long> {
    boolean existsByEmpresaAndCompetenciaAndDataArrecadacaoAndTotalTotal(
            Empresa empresa,
            Competencia competencia,
            LocalDate dataArrecadacao,
            String totalTotal
    );
}
