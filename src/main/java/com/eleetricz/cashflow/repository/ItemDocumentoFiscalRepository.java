package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.documento.DocumentoFiscal;
import com.eleetricz.cashflow.entity.documento.ItemDocumentoFiscal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemDocumentoFiscalRepository extends JpaRepository<ItemDocumentoFiscal, Long> {

    List<ItemDocumentoFiscal> findByDocumentoFiscal(DocumentoFiscal documentoFiscal);
}
