package com.eleetricz.cashflow.service;

import org.springframework.web.multipart.MultipartFile;

public interface ExcelImportService {
     void importarPlanilhaDoExcel(MultipartFile file, Long empresaId) throws Exception;
}
