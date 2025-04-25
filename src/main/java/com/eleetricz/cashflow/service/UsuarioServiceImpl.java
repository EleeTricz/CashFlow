package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.Usuario;
import com.eleetricz.cashflow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService{
    @Autowired private UsuarioRepository usuarioRepository;

    @Override
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }
}
