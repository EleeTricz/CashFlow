package com.eleetricz.cashflow.importacao.service;

import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.Usuario;

import java.io.File;
import java.io.IOException;

public interface EntradasService {
    public void importarLancamentosDeExtrato(File file, Empresa empresa, int anoBase)throws IOException;
}
