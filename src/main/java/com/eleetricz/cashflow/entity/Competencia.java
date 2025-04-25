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
@Table(uniqueConstraints = @UniqueConstraint( columnNames = {"mes", "ano", "empresa_id"}))
public class Competencia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int mes;
    private int ano;

    @ManyToOne
    private Empresa empresa;
}
