package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.Descricao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DescricaoRepository extends JpaRepository<Descricao, Long> {
    boolean existsByNome(String nome);
}
