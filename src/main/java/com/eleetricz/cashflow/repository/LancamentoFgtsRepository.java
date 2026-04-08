package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.LancamentoFgts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface LancamentoFgtsRepository extends JpaRepository<LancamentoFgts, Long> {
    boolean existsByEmpresaAndCompetenciaAndIdentificadorGuia(Empresa empresa, String competencia, String identificadorGuia);

    boolean existsByEmpresaAndCompetenciaAndDataPagamentoAndValorPrincipalAndJurosAndMulta(
            Empresa empresa,
            String competencia,
            LocalDate dataPagamento,
            String valorPrincipal,
            String juros,
            String multa
    );
    void deleteByEmpresa_Id(Long empresaId);
}
