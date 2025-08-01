package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.Usuario;

import java.util.List;

public interface EmpresaService {
    Empresa salvar(Empresa empresa);
    List<Empresa> listarPorUsuario(Usuario usuario);
    List<Empresa> listarTodas();
    Empresa buscarPorId(Long id);
    void zerarLancamentos(Long empresaId);
}
