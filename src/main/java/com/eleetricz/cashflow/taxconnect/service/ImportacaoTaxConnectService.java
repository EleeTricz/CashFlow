package com.eleetricz.cashflow.taxconnect.service;

import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.service.EmpresaService;
import com.eleetricz.cashflow.taxconnect.client.TaxConnectClient;
import com.eleetricz.cashflow.taxconnect.dto.ConsultaPagamentosRequestDTO;
import com.eleetricz.cashflow.taxconnect.dto.ImportacaoTaxConnectFormDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImportacaoTaxConnectService {

    private final EmpresaService empresaService;
    private final TaxConnectClient taxConnectClient;
    private final ImportacaoPagamentosService importacaoPagamentosService;

    public void importar(ImportacaoTaxConnectFormDTO form) {

        Empresa empresa = empresaService.buscarPorId(form.getEmpresaId());

        ConsultaPagamentosRequestDTO.IntervaloDataArrecadacao intervalo =
                new ConsultaPagamentosRequestDTO.IntervaloDataArrecadacao();

        intervalo.setDataInicial(form.getDataInicio());
        intervalo.setDataFinal(form.getDataFim());

        ConsultaPagamentosRequestDTO request = new ConsultaPagamentosRequestDTO();
        request.setContribuinteNumero(empresa.getCnpj());
        request.setIntervaloDataArrecadacao(intervalo);

        var pagamentos = taxConnectClient.consultarPagamentos(request);

        importacaoPagamentosService.importar(empresa.getId(), pagamentos);
    }

}
