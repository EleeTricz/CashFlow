package com.eleetricz.cashflow.entity.documento;

import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.enums.TipoDocumentoFiscal;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "documento_fiscal")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_documento", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class DocumentoFiscal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "competencia", nullable = false, length = 7)
    private String competencia;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(name = "valor_principal", precision = 15, scale = 2)
    private BigDecimal valorPrincipal;

    @Column(name = "valor_multa", precision = 15, scale = 2)
    private BigDecimal valorMulta;

    @Column(name = "valor_juros", precision = 15, scale = 2)
    private BigDecimal valorJuros;

    @Column(name = "valor_total", precision = 15, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento;

    @Column(name = "identificador_guia", length = 50)
    private String identificadorGuia;

    @Column(name = "periodo_apuracao", length = 20)
    private String periodoApuracao;

    @Column(name = "competencia_referida", length = 7)
    private String competenciaReferida;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "tipo_documento", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TipoDocumentoFiscal tipo;
}
