package com.eleetricz.cashflow.controller.api;

import com.eleetricz.cashflow.service.LancamentoDasService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lancamento-das")
@RequiredArgsConstructor
public class LancamentoDasController {

    private final LancamentoDasService dasService;

    @GetMapping("/importar")
    public String importarDas() {
        dasService.importarTodos();
        return "Importação de DAS concluída!";
    }
}
