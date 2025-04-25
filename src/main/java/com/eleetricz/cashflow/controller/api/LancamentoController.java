package com.eleetricz.cashflow.controller.api;

import com.eleetricz.cashflow.entity.Competencia;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.Lancamento;
import com.eleetricz.cashflow.service.LancamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/lancamentos")
public class LancamentoController {
    @Autowired private LancamentoService lancamentoService;

    @PostMapping
    public Lancamento salvar(@RequestBody Lancamento lancamento){
        return lancamentoService.salvar(lancamento);
    }

    @GetMapping("empresa/{empresaId}")
    public List<Lancamento> listarPorEmpresa(@PathVariable Long empresaId) {
        Empresa empresa = new Empresa(); empresa.setId(empresaId);
        return lancamentoService.listarPorEmpresa(empresa);
    }

    @GetMapping("/competencia/{empresaId}/{competenciaId}")
    public List<Lancamento> listarPorCompetencia(@PathVariable Long empresaId, @PathVariable Long competenciaId){
        Empresa empresa = new Empresa(); empresa.setId(empresaId);
        Competencia competencia = new Competencia(); competencia.setId(competenciaId);
        return lancamentoService.listarPorCompetencia(empresa, competencia);
    }

    @GetMapping("saldo/{empresaId}")
    public BigDecimal saldoEmpresa(@PathVariable Long empresaId){
        Empresa empresa = new Empresa(); empresa.setId(empresaId);
        return lancamentoService.calcularSaldoPorEmpresa(empresa);
    }


}
