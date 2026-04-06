package com.eleetricz.cashflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "lancamento_fgts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LancamentoFgts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Empresa empresa;

    private String competencia;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(name = "valor_principal")
    private String valorPrincipal;

    private String juros;
    private String multa;

    @Column(name = "identificador_guia")
    private String identificadorGuia;
}
