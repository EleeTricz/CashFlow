package com.eleetricz.cashflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@Builder
@AllArgsConstructor @NoArgsConstructor
public class LancamentoEsperado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Empresa empresa;

    @ManyToOne
    private Descricao descricao;

    @Enumerated(EnumType.STRING)
    private Frequencia frequencia;

    private Integer diaEsperado;

    private Boolean obrigatorio;

    private BigDecimal valorEsperado;
}
