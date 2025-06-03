package com.eleetricz.cashflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Parcelamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @ManyToOne
    private Empresa empresa;

    @ManyToOne
    private Descricao descricaoBase;

    private LocalDate dataInicio;

    private int totalParcelas;

    private BigDecimal valorTotal;

    private boolean quitado;

    @OneToMany(mappedBy = "parcelamento", cascade = CascadeType.ALL)
    private List<Parcela> parcelas;

    private BigDecimal valorTotalPago;
}