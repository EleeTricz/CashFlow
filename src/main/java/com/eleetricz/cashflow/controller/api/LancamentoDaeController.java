package com.eleetricz.cashflow.controller.api;

import com.eleetricz.cashflow.service.DocumentoFiscalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lancamento-dae")
@RequiredArgsConstructor
public class LancamentoDaeController {
    private final DocumentoFiscalService documentoFiscalService;

    @GetMapping("/importar")
    public String importarDae(){
        documentoFiscalService.importarTodos();
        return "Importação DAE concluída!";
    }
}
