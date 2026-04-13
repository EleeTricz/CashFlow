package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.dto.DarfData;
import com.eleetricz.cashflow.dto.DasData;
import com.eleetricz.cashflow.dto.FgtsData;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.documento.DocumentoFiscal;
import com.eleetricz.cashflow.entity.enums.TipoDocumentoFiscal;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DocumentoFiscalService {

    void importarTodos();

    int importarDadosPdfDarf(Long empresaId, List<DarfData> dadosExtraidos);

    int importarDadosPdfDas(Long empresaId, List<DasData> dadosExtraidos);

    int importarDadosPdfFgts(Long empresaId, List<FgtsData> dadosExtraidos);

    int importarFgtsExtratoTxt(MultipartFile arquivo, Long empresaId) throws IOException;

    List<DocumentoFiscal> listarPorEmpresa(Empresa empresa);

    List<DocumentoFiscal> listarPorEmpresaETipo(Empresa empresa, TipoDocumentoFiscal tipo);

    boolean registroJaExiste(Long empresaId, TipoDocumentoFiscal tipo, String competencia, String numeroDocumento);

    void deletarPorEmpresaId(Long empresaId);
}
