package com.eleetricz.cashflow.repository;

import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.documento.DocumentoFiscal;
import com.eleetricz.cashflow.entity.enums.TipoDocumentoFiscal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoFiscalRepository extends JpaRepository<DocumentoFiscal, Long> {

    List<DocumentoFiscal> findByEmpresa(Empresa empresa);

    List<DocumentoFiscal> findByEmpresaAndTipo(Empresa empresa, TipoDocumentoFiscal tipo);

    List<DocumentoFiscal> findByEmpresaAndCompetencia(Empresa empresa, String competencia);

    Optional<DocumentoFiscal> findByEmpresaAndTipoAndCompetenciaAndNumeroDocumento(
            Empresa empresa,
            TipoDocumentoFiscal tipo,
            String competencia,
            String numeroDocumento
    );

    boolean existsByEmpresaAndTipoAndCompetenciaAndNumeroDocumento(
            Empresa empresa,
            TipoDocumentoFiscal tipo,
            String competencia,
            String numeroDocumento
    );

    boolean existsByEmpresaAndTipoAndCompetenciaAndIdentificadorGuia(
            Empresa empresa,
            TipoDocumentoFiscal tipo,
            String competencia,
            String identificadorGuia
    );

    @Query("SELECT d.tipo, COUNT(d), SUM(d.valorTotal) FROM DocumentoFiscal d " +
           "WHERE d.empresa = :empresa GROUP BY d.tipo")
    List<Object[]> resumoPorTipo(@Param("empresa") Empresa empresa);

    void deleteByEmpresaId(Long empresaId);
}
