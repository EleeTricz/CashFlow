package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.FechamentoItemEsperado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FechamentoItemEsperadoRepository
        extends JpaRepository<FechamentoItemEsperado, Long> {

    List<FechamentoItemEsperado> findByEmpresaId(Long empresaId);

    @Transactional
    void deleteByEmpresaId(Long empresaId);
}