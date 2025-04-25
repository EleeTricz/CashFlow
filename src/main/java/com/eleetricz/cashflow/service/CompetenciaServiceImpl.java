package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.Competencia;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.repository.CompetenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompetenciaServiceImpl implements CompetenciaService{
    @Autowired private CompetenciaRepository competenciaRepository;

    @Override
    public Competencia salvar(Competencia competencia) {
        return competenciaRepository.save(competencia);
    }

    @Override
    public List<Competencia> listarPorEmpresa(Empresa empresa) {
        return competenciaRepository.findByEmpresa(empresa);
    }

    @Override
    public Competencia buscarOuCriar(int mes, int ano, Empresa empresa) {
        return competenciaRepository.findByMesAndAnoAndEmpresa(mes, ano, empresa)
                .orElseGet(() -> salvar(new Competencia(null, mes, ano, empresa)));
    }

    @Override
    public Competencia buscarPorId(Long id) {
        return competenciaRepository.findById(id).orElse(null);
    }
}
