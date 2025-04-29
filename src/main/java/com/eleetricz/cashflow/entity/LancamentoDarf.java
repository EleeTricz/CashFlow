package com.eleetricz.cashflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "lancamento_darf")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LancamentoDarf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    Empresa empresa;

    @Column(name = "numero_documento")
    String numeroDocumento;

    @Column(name = "periodo_apuracao")
    String periodoApuracao;

    @Column(name = "data_vencimento")
    LocalDate dataVencimento;

    @Column(name = "data_arrecadacao")
    LocalDate dataArrecadacao;

    @Column(name = "total_principal")
    String totalPrincipal;

    @Column(name = "total_multa")
    String totalMulta;

    @Column(name = "total_juros")
    String totalJuros;

    @Column(name = "total_total")
    String totalTotal;

    String competencia;
}
