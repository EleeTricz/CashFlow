package com.eleetricz.cashflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Parcela {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int numeroParcela;

    private LocalDate vencimento;

    private BigDecimal valorPrincipal;

    private BigDecimal valorPago;

    private BigDecimal valorEncargos;

    private LocalDate dataPagamento;

    private boolean paga;

    @ManyToOne
    private Parcelamento parcelamento;

    @OneToMany
    @JoinColumn(name = "parcela_id") // chave estrangeira em Lancamento
    private List<Lancamento> lancamentos = new ArrayList<>();
}
