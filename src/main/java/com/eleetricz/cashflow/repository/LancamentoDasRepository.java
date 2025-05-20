package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.LancamentoDas;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LancamentoDasRepository extends JpaRepository<LancamentoDas, Long> {
    boolean existsByEmpresaId_IdAndCompetenciaAndNumeroDocumento(Long empresaId, String competencia, String numeroDocumento);

}
