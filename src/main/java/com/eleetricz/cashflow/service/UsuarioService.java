package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.Usuario;

import java.util.List;

public interface UsuarioService {
    List<Usuario> listarTodos();
    Usuario buscarPorId(Long id);
}
