package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
    List<Lancamento> findByEmpresa(Empresa empresa);
    List<Lancamento> findByCompetencia(Empresa empresa);
    List<Lancamento> findByCompetenciaAndEmpresa(Competencia competencia, Empresa empresa);
    List<Lancamento> findByEmpresaIdAndCompetenciaId(Long empresaId, Long competenciaId);
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

    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM Lancamento l " +
            "WHERE l.empresa = :empresa AND l.tipo = :tipo AND EXTRACT(YEAR FROM l.dataOcorrencia) = :ano")
    BigDecimal somarPorTipoEAno(@Param("empresa") Empresa empresa,
                                @Param("tipo") TipoLancamento tipo,
                                @Param("ano") int ano);

    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM Lancamento l " +
            "JOIN l.descricao d " +
            "WHERE d.nome = :nome " +
            "AND EXTRACT(MONTH FROM l.dataOcorrencia) = :mes " +
            "AND EXTRACT(YEAR FROM l.dataOcorrencia) = :ano " +
            "AND l.empresa.id = :empresaId")
    BigDecimal totalPorDescricaoNomeMesAnoEmpresa(@Param("nome") String descricaoNome,
                                                  @Param("mes") int mes,
                                                  @Param("ano") int ano,
                                                  @Param("empresaId") Long empresaId);

    @Query("""
    SELECT l FROM Lancamento l
    WHERE l.empresa.id = :empresaId
    AND (l.competencia.ano > :anoInicial OR (l.competencia.ano = :anoInicial AND l.competencia.mes >= :mesInicial))
    AND (l.competencia.ano < :anoFinal OR (l.competencia.ano = :anoFinal AND l.competencia.mes <= :mesFinal))
    ORDER BY l.dataOcorrencia ASC, l.id ASC
""")
    List<Lancamento> findByEmpresaAndCompetenciaIntervaloOrdenado(Long empresaId, int mesInicial, int anoInicial, int mesFinal, int anoFinal);
    void deleteByEmpresaId(Long empresaId);

}
