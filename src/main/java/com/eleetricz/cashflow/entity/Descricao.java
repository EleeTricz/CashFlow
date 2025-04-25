package com.eleetricz.cashflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Descricao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String codigoDebito;
    private String codigoCredito;
    private String codigoHistorico;

    @Enumerated(EnumType.STRING)
    private TipoLancamento tipo;


}