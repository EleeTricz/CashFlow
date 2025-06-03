package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.Descricao;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.Parcelamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParcelamentoRepository extends JpaRepository<Parcelamento, Long> {
    Optional<Parcelamento> findByDescricaoBaseAndEmpresaAndTotalParcelas(
            Descricao descricaoBase,
            Empresa empresa,
            Integer totalParcelas
    );
    List<Parcelamento> findByEmpresaId(Long empresaId);
}
