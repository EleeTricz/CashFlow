package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.LancamentoEsperado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LancamentoEsperadoRepository extends JpaRepository<LancamentoEsperado, Long> {
    List<LancamentoEsperado> findByEmpresaId(Long empresaId);
}
