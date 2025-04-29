package com.eleetricz.cashflow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "lancamento_das")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LancamentoDas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Empresa empresaId;

    private String competencia;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    private String principal;
    private String multa;
    private String juros;
    private String total;
    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "encargos_das")
    private String encargosDas;

    @Column(name = "data_arrecadacao")
    private LocalDate dataArrecadacao;
}
