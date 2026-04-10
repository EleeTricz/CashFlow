package com.eleetricz.cashflow.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "empresa_perfil_fechamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaPerfilFechamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Empresa empresa;

    private Boolean ativo = true;
}