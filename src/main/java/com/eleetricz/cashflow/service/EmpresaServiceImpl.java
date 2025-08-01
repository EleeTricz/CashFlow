package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.Usuario;
import com.eleetricz.cashflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaServiceImpl implements EmpresaService {
    private final EmpresaRepository empresaRepository;
    private final LancamentoDarfRepository lancDarfRepo;
    private final ItensDarfRepository itensDarfRepo;
    private final LancamentoDasRepository lancDasRepo;
    private final LancamentoDaeRepository lancDaeRepo;
    private final LancamentoRepository lancRepo;

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

    @Transactional
    @Override
    public void zerarLancamentos(Long empresaId) {
        // Deletar ItensDarf antes de LancamentoDarf (por FK)
        var darfs = lancDarfRepo.findByEmpresaId(empresaId);
        for (var darf : darfs) {
            itensDarfRepo.deleteByLancamentoDarf_Id(darf.getId());
        }
        lancDarfRepo.deleteAll(darfs);
        lancDasRepo.deleteByEmpresaId_Id(empresaId);
        lancDaeRepo.deleteByEmpresa_Id(empresaId);
        lancRepo.deleteByEmpresaId(empresaId);
    }
}
