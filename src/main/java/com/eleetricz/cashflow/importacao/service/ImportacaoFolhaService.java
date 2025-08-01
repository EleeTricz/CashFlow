package com.eleetricz.cashflow.importacao.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImportacaoFolhaService {
    void importarFolha(MultipartFile file, Long empresaId) throws Exception;
}
