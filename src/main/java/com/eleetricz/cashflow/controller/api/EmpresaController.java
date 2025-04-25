package com.eleetricz.cashflow.controller.api;

import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.Usuario;
import com.eleetricz.cashflow.service.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/empresas")
public class EmpresaController {
    @Autowired private EmpresaService empresaService;

    @GetMapping("/usuario/{usuarioId}")
    public List<Empresa> listarPorUsuario(@PathVariable Long usuarioId) {
        Usuario usuario = new Usuario(); usuario.setId(usuarioId);
        return empresaService.listarPorUsuario(usuario);
    }

    @GetMapping("/todas")
    public List<Empresa> listarTodas() {
        return empresaService.listarTodas();
    }

    @PostMapping
    public Empresa salvar(@RequestBody Empresa empresa) {
        return empresaService.salvar(empresa);
    }
}
