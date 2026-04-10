package com.eleetricz.cashflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "fechamento_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusIntegracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empres-id", nullable = false)
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "competenci-id", nullable = false)
    private Competencia competencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTarefa fiscalStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTarefa fgtsStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTarefa inssStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTarefa simplesStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTarefa folhaStatus;

    private LocalDate ultimaAtualizacao;

    @PrePersist
    @PreUpdate
    public void setInitialStatusAndDate() {
        if (fiscalStatus == null) this.fiscalStatus = StatusTarefa.PENDENTE;
        if (fgtsStatus == null) this.fgtsStatus = StatusTarefa.PENDENTE;
        if (inssStatus == null) this.inssStatus = StatusTarefa.PENDENTE;
        if (simplesStatus == null) this.simplesStatus = StatusTarefa.PENDENTE;
        if (folhaStatus == null) this.folhaStatus = StatusTarefa.PENDENTE;
        this.ultimaAtualizacao = LocalDate.now();
    }
}

