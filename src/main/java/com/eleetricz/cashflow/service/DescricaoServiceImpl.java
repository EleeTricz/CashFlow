package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.Descricao;
import com.eleetricz.cashflow.repository.DescricaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DescricaoServiceImpl implements DescricaoService{
    private final DescricaoRepository descricaoRepository;

    @Override
    public Descricao salvar(Descricao descricao) {
        return descricaoRepository.save(descricao);
    }

    @Override
    public List<Descricao> listarTodas() {
        return descricaoRepository.findAll();
    }
}
