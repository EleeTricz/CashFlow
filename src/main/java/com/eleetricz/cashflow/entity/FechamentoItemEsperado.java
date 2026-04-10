package com.eleetricz.cashflow.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fechamento_item_esperado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FechamentoItemEsperado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Empresa empresa;

    @ManyToOne
    private Descricao descricao;

    @Enumerated(EnumType.STRING)
    private FrequenciaFechamento frequencia;

    private Boolean obrigatorio = true;
}
