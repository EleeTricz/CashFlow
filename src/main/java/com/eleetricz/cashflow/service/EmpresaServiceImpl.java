package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.Usuario;
import com.eleetricz.cashflow.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpresaServiceImpl implements EmpresaService {
    @Autowired private EmpresaRepository empresaRepository;

    @Override
    public Empresa salvar(Empresa empresa) {
        return empresaRepository.save(empresa);
    }

    @Override
    public List<Empresa> listarPorUsuario(Usuario usuario) {
        return empresaRepository.findByUsuario(usuario);
    }

    @Override
    public List<Empresa> listarTodas() {
        return empresaRepository.findAll();
    }

    @Override
    public Empresa buscarPorId(Long id) {
        return empresaRepository.findById(id).orElse(null);
    }
}
