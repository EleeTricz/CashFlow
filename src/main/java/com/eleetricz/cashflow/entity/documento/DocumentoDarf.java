package com.eleetricz.cashflow.entity.documento;

import com.eleetricz.cashflow.entity.enums.TipoDocumentoFiscal;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@DiscriminatorValue("DARF")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class DocumentoDarf extends DocumentoFiscal {

    @OneToMany(mappedBy = "documentoFiscal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemDocumentoFiscal> itens;

    @Override
    public TipoDocumentoFiscal getTipo() {
        return TipoDocumentoFiscal.DARF;
    }
}
