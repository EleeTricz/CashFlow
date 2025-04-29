package com.eleetricz.cashflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "itens_darf")
public class ItensDarf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "darf_id")
    LancamentoDarf lancamentoDarf;

    String codigo;
    String descricao;
    String principal;
    String multa;
    String juros;
    String total;

}
