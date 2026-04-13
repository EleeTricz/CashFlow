package com.eleetricz.cashflow.controller.api;

import com.eleetricz.cashflow.service.DocumentoFiscalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lancamento-darf")
@RequiredArgsConstructor
public class LancamentoDarfController {

    private final DocumentoFiscalService documentoFiscalService;

    @GetMapping("/importar")
    public String importarDarf() {
        documentoFiscalService.importarTodos();
        return "Importação de DARF concluída!";
    }
}
