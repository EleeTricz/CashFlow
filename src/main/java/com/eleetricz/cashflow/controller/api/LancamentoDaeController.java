package com.eleetricz.cashflow.controller.api;

import com.eleetricz.cashflow.service.LancamentoDaeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lancamento-dae")
@RequiredArgsConstructor
public class LancamentoDaeController {
    private final LancamentoDaeService daeService;

    @GetMapping("/importar")
    public String importarDae(){
        daeService.importarTodos();
        return "Importação DAE concluída!";
    }
}
