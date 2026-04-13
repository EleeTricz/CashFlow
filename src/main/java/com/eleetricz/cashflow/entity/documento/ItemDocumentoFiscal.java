package com.eleetricz.cashflow.entity.documento;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "item_documento_fiscal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDocumentoFiscal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_fiscal_id")
    private DocumentoFiscal documentoFiscal;

    private String codigo;
    private String descricao;

    @Column(precision = 15, scale = 2)
    private BigDecimal principal;

    @Column(precision = 15, scale = 2)
    private BigDecimal multa;

    @Column(precision = 15, scale = 2)
    private BigDecimal juros;

    @Column(precision = 15, scale = 2)
    private BigDecimal total;
}
