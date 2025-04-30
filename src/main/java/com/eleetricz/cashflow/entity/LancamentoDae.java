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
    Long id;

    @Column(name = "competencia_referida")
    String competenciaReferida;

    @Column(name = "competencia")
    String competencia;

    @Column(name = "data_vencimento")
    LocalDate dataVencimento;

    @Column(name = "data_arrecadacao")
    LocalDate dataArrecadacao;

    String valor;

    @Column(name = "numero_documento_origem")
    String numeroDocumentoOrigem;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    Empresa empresa;

}
