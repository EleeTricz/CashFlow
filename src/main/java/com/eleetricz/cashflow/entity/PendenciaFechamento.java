package com.eleetricz.cashflow.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pendencia_fechamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendenciaFechamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Empresa empresa;

    @ManyToOne
    private Competencia competencia;

    @ManyToOne
    private Descricao descricao;

    @Enumerated(EnumType.STRING)
    private StatusPendencia status;
}
