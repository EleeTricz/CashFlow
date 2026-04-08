package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.dto.FgtsData;
import com.eleetricz.cashflow.entity.Empresa;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface LancamentoFgtsService {
    void importarTodos();
    int importarDadosPdfFgts(Long empresaId, List<FgtsData> dadosExtraidos);
    int importarFgtsExtratoTxt(MultipartFile arquivo, Long empresaId) throws IOException;
}
