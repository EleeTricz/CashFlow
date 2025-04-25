package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.Descricao;
import com.eleetricz.cashflow.repository.DescricaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DescricaoServiceImpl implements DescricaoService{
    @Autowired private DescricaoRepository descricaoRepository;

    @Override
    public Descricao salvar(Descricao descricao) {
        return descricaoRepository.save(descricao);
    }

    @Override
    public List<Descricao> listarTodas() {
        return descricaoRepository.findAll();
    }
}
