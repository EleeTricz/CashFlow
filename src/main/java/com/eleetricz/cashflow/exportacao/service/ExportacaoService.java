package com.eleetricz.cashflow.exportacao.service;

import java.io.File;
import java.io.IOException;

public interface ExportacaoService {
    File exportarLancamentosParaTxt(Long empresaId, int mesInicial, int anoInicial, int mesFinal, int anoFinal) throws IOException;

}
