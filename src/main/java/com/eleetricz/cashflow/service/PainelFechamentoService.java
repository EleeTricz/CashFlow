package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.dto.PainelFechamentoDTO;
import com.eleetricz.cashflow.dto.PerfilFechamentoFormDTO;

import java.util.List;

public interface PainelFechamentoService {
    List<PainelFechamentoDTO> gerarPainel(Long empresaId, Long competenciaId);
    Integer calcularPercentual(List<PainelFechamentoDTO> painel);
    void salvarPerfil(PerfilFechamentoFormDTO dto);
}
