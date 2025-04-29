package com.eleetricz.cashflow.controller.api;

import com.eleetricz.cashflow.service.LancamentoDarfService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lancamento-darf")
@RequiredArgsConstructor
public class LancamentoDarfController {

    private final LancamentoDarfService darfService;

    @GetMapping("/importar")
    public String importarDarf() {
        darfService.importarTodos();
        return "Importação de DARF concluída!";
    }
}
