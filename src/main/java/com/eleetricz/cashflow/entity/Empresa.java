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
public class Empresa {
    @Id @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;

    @ManyToOne
    private Usuario usuario;
}
