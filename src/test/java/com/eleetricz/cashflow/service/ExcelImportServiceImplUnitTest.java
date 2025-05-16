package com.eleetricz.cashflow.service;

import com.eleetricz.cashflow.entity.*;
import com.eleetricz.cashflow.repository.CompetenciaRepository;
import com.eleetricz.cashflow.repository.DescricaoRepository;
import com.eleetricz.cashflow.repository.EmpresaRepository;
import com.eleetricz.cashflow.repository.LancamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentCaptor;

class ExcelImportServiceImplUnitTest {

    @InjectMocks
    private ExcelImportServiceImpl excelImportService;

    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private CompetenciaRepository competenciaRepository;
    @Mock
    private DescricaoRepository descricaoRepository;
    @Mock
    private LancamentoRepository lancamentoRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveImportarTresLancamentosDeDuasAbas() throws Exception {
        // Arrange
        Long empresaId = 1L;
        Empresa empresa = new Empresa();
        empresa.setId(empresaId);
        empresa.setNome("Empresa Mock");

        // Descrições
        Descricao descricaoProlabore = new Descricao();
        descricaoProlabore.setId(1L);
        descricaoProlabore.setNome("PROLABORE");

        Descricao descricaoGps = new Descricao();
        descricaoGps.setId(2L);
        descricaoGps.setNome("GPS");

        // Competências
        Competencia competencia032025 = new Competencia(1L, 3, 2025, empresa);
        Competencia competencia022025 = new Competencia(2L, 2, 2025, empresa);
        Competencia competencia042025 = new Competencia(3L, 4, 2025, empresa);
        Competencia competencia032025Referida = new Competencia(4L, 3, 2025, empresa);

        // Mocks
        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
        when(descricaoRepository.findByNomeIgnoreCase(eq("PROLABORE")))
                .thenReturn(Optional.of(descricaoProlabore));
        when(descricaoRepository.findByNomeIgnoreCase(eq("GPS")))
                .thenReturn(Optional.of(descricaoGps));

        when(competenciaRepository.findByMesAndAnoAndEmpresa(eq(3), eq(2025), eq(empresa)))
                .thenReturn(Optional.of(competencia032025));
        when(competenciaRepository.findByMesAndAnoAndEmpresa(eq(2), eq(2025), eq(empresa)))
                .thenReturn(Optional.of(competencia022025));
        when(competenciaRepository.findByMesAndAnoAndEmpresa(eq(4), eq(2025), eq(empresa)))
                .thenReturn(Optional.of(competencia042025));

        // Arquivo com 2 lançamentos em uma aba e 1 em outra
        FileInputStream fis = new FileInputStream("src/test/resources/lancamentos_test.xlsx");
        MockMultipartFile file = new MockMultipartFile("file", "lancamentos_test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", fis);

        // Act
        excelImportService.importarPlanilhaDoExcel(file, empresaId);

        // Assert
        ArgumentCaptor<Lancamento> captor = ArgumentCaptor.forClass(Lancamento.class);
        verify(lancamentoRepository, atLeast(3)).save(captor.capture());

        var lancamentos = captor.getAllValues();
        assertEquals(3, lancamentos.size(), "Devem ter sido salvos três lançamentos");

        Lancamento l1 = lancamentos.get(0);
        Lancamento l2 = lancamentos.get(1);
        Lancamento l3 = lancamentos.get(2);

        // Verifica primeiro lançamento (PROLABORE - MARÇO)
        assertEquals("PROLABORE", l1.getDescricao().getNome());
        assertEquals(new BigDecimal("1256.68"), l1.getValor());
        assertEquals(LocalDate.of(2025, 3, 1), l1.getDataOcorrencia());
        assertEquals(3, l1.getCompetencia().getMes());
        assertEquals(2025, l1.getCompetencia().getAno());
        assertEquals(2, l1.getCompetenciaReferida().getMes());

        // Verifica segundo lançamento (GPS - MARÇO)
        assertEquals("GPS", l2.getDescricao().getNome());
        assertEquals(new BigDecimal("540.0"), l2.getValor());
        assertEquals(LocalDate.of(2025, 3, 1), l2.getDataOcorrencia());
        assertEquals(3, l2.getCompetencia().getMes());
        assertEquals(2025, l2.getCompetencia().getAno());
        assertEquals(2, l2.getCompetenciaReferida().getMes());

        // Verifica terceiro lançamento (GPS - ABRIL)
        assertEquals("GPS", l3.getDescricao().getNome());
        assertEquals(new BigDecimal("320.0"), l3.getValor());
        assertEquals(LocalDate.of(2025, 4, 1), l3.getDataOcorrencia());
        assertEquals(4, l3.getCompetencia().getMes());
        assertEquals(2025, l3.getCompetencia().getAno());
        assertEquals(3, l3.getCompetenciaReferida().getMes());
    }
}
