package com.eleetricz.cashflow.taxconnect.client;

import com.eleetricz.cashflow.taxconnect.dto.ConsultaPagamentosRequestDTO;
import com.eleetricz.cashflow.taxconnect.dto.PagamentoNormalizadoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaxConnectClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${taxconnect.url}")
    private String taxConnectUrl;

    public List<PagamentoNormalizadoDTO> consultarPagamentos(
            ConsultaPagamentosRequestDTO request) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ConsultaPagamentosRequestDTO> entity =
                new HttpEntity<>(request, headers);

        ResponseEntity<PagamentoNormalizadoDTO[]> response =
                restTemplate.postForEntity(
                        taxConnectUrl + "/api/pagamentos/consultar",
                        entity,
                        PagamentoNormalizadoDTO[].class
                );

        return Arrays.asList(response.getBody());
    }
}