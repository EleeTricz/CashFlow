package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.Competencia;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.StatusIntegracao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StatusIntegracaoRepository extends JpaRepository<StatusIntegracao, Long> {

    Optional<StatusIntegracao> findByEmpresaAndCompetencia(Empresa empresa, Competencia competencia);

    @Query("SELECT s FROM StatusIntegracao s WHERE " +
            "(:empresaId IS NULL OR s.empresa.id = :empresaId) AND " +
            "(:ano IS NULL OR s.competencia.ano = :ano) " +
            "ORDER BY s.competencia.ano DESC, s.competencia.mes DESC, s.empresa.nome ASC")
    Page<StatusIntegracao> findByFiltros(@Param("empresaId") Long empresaId,
                                         @Param("ano") Integer ano,
                                         Pageable pageable);

    void deleteByEmpresa_Id(Long empresaId);
}

