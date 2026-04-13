package com.eleetricz.cashflow.entity.documento;

import com.eleetricz.cashflow.entity.enums.TipoDocumentoFiscal;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("FGTS")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class DocumentoFgts extends DocumentoFiscal {

    @Override
    public TipoDocumentoFiscal getTipo() {
        return TipoDocumentoFiscal.FGTS;
    }
}
