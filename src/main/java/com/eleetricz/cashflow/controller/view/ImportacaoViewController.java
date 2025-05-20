package com.eleetricz.cashflow.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ImportacaoViewController {

    @GetMapping("/importartf/todas")
    public String telaEscolhaImportacao() {
        return "importar-home";
    }
}
