package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.importacao.service.EntradasServiceImpl;
import com.eleetricz.cashflow.repository.CompetenciaRepository;
import com.eleetricz.cashflow.repository.DescricaoRepository;
import com.eleetricz.cashflow.repository.LancamentoRepository;
import com.eleetricz.cashflow.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EntradasServiceImplTest {

    private DescricaoRepository descricaoRepository = mock(DescricaoRepository.class);
    private LancamentoRepository lancamentoRepository = mock(LancamentoRepository.class);
    private CompetenciaRepository competenciaRepository = mock(CompetenciaRepository.class);
    private UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);


    private EntradasServiceImpl entradasService;

    private Empresa empresa;
    private Usuario usuario;
    private Descricao descricaoReceita;

    @BeforeEach
    void setup() {
        entradasService = new EntradasServiceImpl(descricaoRepository, lancamentoRepository, competenciaRepository, usuarioRepository);

        empresa = new Empresa();
        empresa.setId(1L);

        usuario = new Usuario();
        usuario.setId(1L);

        descricaoReceita = new Descricao();
        descricaoReceita.setId(100L);
        descricaoReceita.setNome("Receita de Vendas");

        MockitoAnnotations.openMocks(this);

        // Mock do findByNomeIgnoreCase
        Mockito.when(descricaoRepository.findByNomeIgnoreCase("Receita de Vendas"))
                .thenReturn(java.util.Optional.of(descricaoReceita));

        // Mock do competenciaRepository.findByMesAndAnoAndEmpresa para retornar sempre um Competencia "fake"
        Mockito.when(competenciaRepository.findByMesAndAnoAndEmpresa(Mockito.anyInt(), Mockito.anyInt(), Mockito.eq(empresa)))
                .thenAnswer(invocation -> {
                    int mes = invocation.getArgument(0);
                    int ano = invocation.getArgument(1);

                    Competencia c = new Competencia();
                    c.setMes(mes);
                    c.setAno(ano);
                    c.setEmpresa(empresa);
                    return java.util.Optional.of(c);
                });

        // Mock do save (para não salvar nada, só imprimir)
        Mockito.doAnswer(invocation -> {
            List<Lancamento> lancamentos = invocation.getArgument(0);
            System.out.println("=== Lançamentos para salvar ===");
            lancamentos.forEach(l -> {
                System.out.println("Competência: " + l.getCompetencia().getMes() + "/" + l.getCompetencia().getAno()
                        + " | Valor: " + l.getValor()
                        + " | Descrição: " + l.getDescricao().getNome());
            });
            return null;
        }).when(lancamentoRepository).saveAll(anyList());
    }

    @Test
    void testeImportarExtratoImprimeLancamentos() throws Exception {
        // Carrega arquivo do resource
        URL resource = getClass().getClassLoader().getResource("extrato-exemplo.txt");
        if (resource == null) {
            throw new RuntimeException("Arquivo extrato-exemplo.txt não encontrado em src/test/resources");
        }

        File arquivo = new File(resource.toURI());

        entradasService.importarLancamentosDeExtrato(arquivo, empresa,  2023);
    }
}
