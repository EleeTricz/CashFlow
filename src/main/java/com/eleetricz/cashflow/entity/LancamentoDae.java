package com.eleetricz.cashflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor @AllArgsConstructor @Builder
public class LancamentoDae {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "competencia_referida")
    private String competenciaReferida;

    @Column(name = "competencia")
    private String competencia;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(name = "data_arrecadacao")
    private LocalDate dataArrecadacao;

    private String valor;

    @Column(name = "numero_documento_origem")
    private String numeroDocumentoOrigem;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

}
