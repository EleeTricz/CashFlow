package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.Competencia;
import com.eleetricz.cashflow.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompetenciaRepository extends JpaRepository<Competencia, Long> {
    List<Competencia> findByEmpresa(Empresa empresa);
    Optional<Competencia> findByMesAndAnoAndEmpresa(int mes, int ano, Empresa empresa);
}
