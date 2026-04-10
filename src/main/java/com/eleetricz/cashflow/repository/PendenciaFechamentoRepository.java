package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.PendenciaFechamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PendenciaFechamentoRepository
        extends JpaRepository<PendenciaFechamento, Long> {

    List<PendenciaFechamento> findByEmpresaIdAndCompetenciaId(
            Long empresaId,
            Long competenciaId
    );

    Optional<PendenciaFechamento> findByEmpresaIdAndCompetenciaIdAndDescricaoId(
            Long empresaId,
            Long competenciaId,
            Long descricaoId
    );
}