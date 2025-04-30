package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
    List<Lancamento> findByEmpresa(Empresa empresa);
    List<Lancamento> findByCompetencia(Empresa empresa);
    List<Lancamento> findByCompetenciaAndEmpresa(Competencia competencia, Empresa empresa);
    boolean existsByEmpresaAndCompetenciaAndCompetenciaReferidaAndDescricaoAndValorAndDataOcorrenciaAndTipo(
            Empresa empresa,
            Competencia competencia,
            Competencia competenciaReferida,
            Descricao descricao,
            BigDecimal valor,
            LocalDate dataOcorrencia,
            TipoLancamento tipo
    );

    boolean existsByEmpresaAndCompetenciaAndDescricaoAndValorAndDataOcorrenciaAndTipo(
            Empresa empresa,
            Competencia competencia,
            Descricao descricao,
            BigDecimal valor,
            LocalDate dataOcorrencia,
            TipoLancamento tipo);

    boolean existsByEmpresaAndCompetenciaAndCompetenciaReferidaAndDescricao(
            Empresa empresa,
            Competencia competencia,
            Competencia competenciaReferida,
            Descricao descricao
    );

    List<Lancamento> findByEmpresaId(Long empresaId, Sort sort);
}
