package com.eleetricz.cashflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Lancamento {
    @Id @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Descricao descricao;

    @ManyToOne
    private Competencia competencia;

    @ManyToOne
    @JoinColumn( name = "competencia_referida_id")
    private Competencia competenciaReferida;

    @ManyToOne
    private Empresa empresa;

    @ManyToOne
    private Usuario usuario;

    private BigDecimal valor;

    private LocalDate dataOcorrencia;

    private LocalDateTime dataLancamento = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private TipoLancamento tipo;

    @ManyToOne
    private Parcela parcela;

    private String observacao;
}
