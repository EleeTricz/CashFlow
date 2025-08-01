package com.eleetricz.cashflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "lancamento_darf")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LancamentoDarf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Empresa empresa;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "periodo_apuracao")
    private String periodoApuracao;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(name = "data_arrecadacao")
    private LocalDate dataArrecadacao;

    @Column(name = "total_principal")
    private String totalPrincipal;

    @Column(name = "total_multa")
    private String totalMulta;

    @Column(name = "total_juros")
    private String totalJuros;

    @Column(name = "total_total")
    private String totalTotal;

    private String competencia;

    @OneToMany(mappedBy = "lancamentoDarf", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItensDarf> itensDarf;
}
