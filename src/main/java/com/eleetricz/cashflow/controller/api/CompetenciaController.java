package com.eleetricz.cashflow.controller.api;

import com.eleetricz.cashflow.entity.Competencia;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.service.CompetenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/competencias")
public class CompetenciaController {
    @Autowired private CompetenciaService competenciaService;

    @PostMapping
    public Competencia salvar(@RequestBody Competencia competencia){
        return competenciaService.salvar(competencia);
    }

    @GetMapping("empresa/{empresaId}")
    public List<Competencia> listarPorEmpresa(@PathVariable long empresaId){
        Empresa empresa = new Empresa(); empresa.setId(empresaId);
        return competenciaService.listarPorEmpresa(empresa);
    }

}
