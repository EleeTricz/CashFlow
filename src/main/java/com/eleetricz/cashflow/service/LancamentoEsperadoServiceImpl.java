package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.LancamentoEsperado;
import com.eleetricz.cashflow.repository.LancamentoEsperadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LancamentoEsperadoServiceImpl implements LancamentoEsperadoService{

    private final LancamentoEsperadoRepository lancamentoEsperadoRepository;

    @Override
    public List<LancamentoEsperado> listarPorEmpresa(Long empresaId){
        return lancamentoEsperadoRepository.findByEmpresaId(empresaId);
    }

    @Override
    public LancamentoEsperado salvar(LancamentoEsperado esperado) {
        return lancamentoEsperadoRepository.save(esperado);
    }

    @Override
    public void excluir(Long id) {
        lancamentoEsperadoRepository.deleteById(id);
    }

    @Override
    public LancamentoEsperado buscarPorId(Long id) {
        return lancamentoEsperadoRepository.findById(id).orElse(null);
    }
}
