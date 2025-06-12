package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.Competencia;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.Lancamento;
import com.eleetricz.cashflow.entity.LancamentoEsperado;
import com.eleetricz.cashflow.repository.CompetenciaRepository;
import com.eleetricz.cashflow.repository.EmpresaRepository;
import com.eleetricz.cashflow.repository.LancamentoEsperadoRepository;
import com.eleetricz.cashflow.repository.LancamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService{

    private final LancamentoEsperadoRepository esperadoRepository;
    private final LancamentoRepository lancamentoRepository;
    private final CompetenciaRepository competenciaRepository;
    private final EmpresaService empresaService;

    @Override
    public List<LancamentoEsperado> verificarPendencias(Long empresaId, String competencia) {
        Empresa empresa = empresaService.buscarPorId(empresaId);
        String[] partes = competencia.split("-");
        int mes = Integer.parseInt(partes[0]);
        int ano = Integer.parseInt(partes[1]);
        Competencia competenciaObj = obterOuCriarCompetencia(mes, ano, empresa);


        List<LancamentoEsperado> esperados = esperadoRepository.findByEmpresaId(empresaId);
        List<Lancamento> realizados = lancamentoRepository.findByEmpresaIdAndCompetenciaId(empresaId, competenciaObj.getId());


        return esperados.stream()
                .filter(esperado -> realizados.stream().noneMatch(real ->
                        real.getDescricao().getId().equals(esperado.getDescricao().getId()) &&
                                real.getDescricao().getTipo().equals(esperado.getDescricao().getTipo())
                ))
                .collect(Collectors.toList());
    }

    private Competencia obterOuCriarCompetencia(int mes, int ano, Empresa empresa) {
        return competenciaRepository.findByMesAndAnoAndEmpresa(mes, ano, empresa)
                .orElseGet(() -> {
                    Competencia nova = new Competencia();
                    nova.setMes(mes);
                    nova.setAno(ano);
                    nova.setEmpresa(empresa);
                    return competenciaRepository.save(nova);
                });
    }
}
