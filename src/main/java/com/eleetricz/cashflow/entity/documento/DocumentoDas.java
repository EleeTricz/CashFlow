package com.eleetricz.cashflow.entity.documento;

import com.eleetricz.cashflow.entity.enums.TipoDocumentoFiscal;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("DAS")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class DocumentoDas extends DocumentoFiscal {

    @Override
    public TipoDocumentoFiscal getTipo() {
        return TipoDocumentoFiscal.DAS;
    }
}
