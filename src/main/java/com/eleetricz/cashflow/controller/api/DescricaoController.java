package com.eleetricz.cashflow.controller.api;

import com.eleetricz.cashflow.entity.Descricao;
import com.eleetricz.cashflow.service.DescricaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/descricoes")
public class DescricaoController {
    @Autowired private DescricaoService descricaoService;

    @GetMapping
    public List<Descricao> listar() {
        return descricaoService.listarTodas();
    }

    @PostMapping
    public Descricao salvar(@RequestBody Descricao descricao){
        return descricaoService.salvar(descricao);
    }
}
