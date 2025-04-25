package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.Competencia;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.Lancamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
    List<Lancamento> findByEmpresa(Empresa empresa);
    List<Lancamento> findByCompetencia(Empresa empresa);
    List<Lancamento> findByCompetenciaAndEmpresa(Competencia competencia, Empresa empresa);
}
