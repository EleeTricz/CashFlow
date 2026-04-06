package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.Competencia;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.FechamentoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FechamentoStatusRepository extends JpaRepository<FechamentoStatus, Long> {
    Optional<FechamentoStatus> findByEmpresaAndCompetencia(Empresa empresa, Competencia competencia);
    List<FechamentoStatus> findByEmpresaOrderByCompetenciaAnoDescCompetenciaMesDesc(Empresa empresa);
    List<FechamentoStatus> findAllByOrderByCompetenciaAnoDescCompetenciaMesDescEmpresaNomeAsc();

}