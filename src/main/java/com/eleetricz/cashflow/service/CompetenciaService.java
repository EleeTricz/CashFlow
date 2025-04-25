package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.Competencia;
import com.eleetricz.cashflow.entity.Empresa;

import java.util.List;

public interface CompetenciaService {
    Competencia salvar(Competencia competencia);
    List<Competencia> listarPorEmpresa(Empresa empresa);
    Competencia buscarOuCriar(int mes, int ano, Empresa empresa);
    Competencia buscarPorId(Long id);
}
