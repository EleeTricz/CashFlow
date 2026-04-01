package com.eleetricz.cashflow.importacao.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.eleetricz.cashflow.entity.Competencia;
import com.eleetricz.cashflow.entity.Descricao;
import com.eleetricz.cashflow.entity.Empresa;
import com.eleetricz.cashflow.entity.Lancamento;
import com.eleetricz.cashflow.entity.TipoLancamento;
import com.eleetricz.cashflow.entity.Usuario;
import com.eleetricz.cashflow.importacao.dto.ImportacaoDaeDTO;
import com.eleetricz.cashflow.repository.CompetenciaRepository;
import com.eleetricz.cashflow.repository.DescricaoRepository;
import com.eleetricz.cashflow.repository.EmpresaRepository;
import com.eleetricz.cashflow.repository.LancamentoRepository;
import com.eleetricz.cashflow.repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImportacaoDaeService {
    private final EmpresaRepository empresaRepository;
    private final CompetenciaRepository competenciaRepository;
    private final DescricaoRepository descricaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LancamentoRepository lancamentoRepository;

    public List<ImportacaoDaeDTO> importar(MultipartFile file) throws IOException {

        List<ImportacaoDaeDTO> lista = new ArrayList<>();

        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);

            if (row == null) continue;

            String natureza = getCellValue(row.getCell(1));

            if (!"00058-2".equals(natureza)) {
                continue;
            }

            LocalDate dataPagamento =
                    row.getCell(5)
                       .getLocalDateTimeCellValue()
                       .toLocalDate();

            BigDecimal valor =
                    BigDecimal.valueOf(row.getCell(7).getNumericCellValue());

            ImportacaoDaeDTO dto = new ImportacaoDaeDTO();
            dto.setNaturezaReceita(natureza);
            dto.setDataPagamento(dataPagamento);
            dto.setValor(valor);

            lista.add(dto);
        }

        workbook.close();

        return lista;
    }

    @Transactional
    public void salvar(List<ImportacaoDaeDTO> registros, Long empresaId) {
        if (empresaId == null) {
            throw new IllegalArgumentException("Empresa é obrigatória para importar DAE.");
        }

        List<ImportacaoDaeDTO> dados = registros == null ? Collections.emptyList() : registros;
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada: " + empresaId));
        Descricao descricao = descricaoRepository.findByNome("DAE 10")
                .orElseThrow(() -> new IllegalStateException("Descrição 'DAE 10' não encontrada."));
        Usuario usuario = usuarioRepository.findByNome("admin")
                .orElseThrow(() -> new IllegalStateException("Usuário 'admin' não encontrado."));

        for (ImportacaoDaeDTO registro : dados) {
            if (registro.getDataPagamento() == null || registro.getValor() == null) {
                continue;
            }

            Competencia competencia = obterOuCriarCompetencia(registro.getDataPagamento(), empresa);

            boolean existe = lancamentoRepository
                    .existsByEmpresaAndCompetenciaAndCompetenciaReferidaAndDescricaoAndValorAndDataOcorrenciaAndTipo(
                            empresa,
                            competencia,
                            competencia,
                            descricao,
                            registro.getValor(),
                            registro.getDataPagamento(),
                            TipoLancamento.SAIDA
                    );

            if (existe) {
                continue;
            }

            Lancamento lancamento = new Lancamento();
            lancamento.setEmpresa(empresa);
            lancamento.setCompetencia(competencia);
            lancamento.setCompetenciaReferida(competencia);
            lancamento.setDescricao(descricao);
            lancamento.setUsuario(usuario);
            lancamento.setTipo(TipoLancamento.SAIDA);
            lancamento.setDataOcorrencia(registro.getDataPagamento());
            lancamento.setValor(registro.getValor());
            lancamento.setObservacao("Importação DAE - natureza " + registro.getNaturezaReceita());

            lancamentoRepository.save(lancamento);
        }
    }

    private Competencia obterOuCriarCompetencia(LocalDate data, Empresa empresa) {
        int mes = data.getMonthValue();
        int ano = data.getYear();
        Competencia competencia = competenciaRepository.findByMesAndAnoAndEmpresa(mes, ano, empresa)
                .orElseGet(() -> {
                    Competencia nova = new Competencia();
                    nova.setMes(mes);
                    nova.setAno(ano);
                    nova.setEmpresa(empresa);
                    return competenciaRepository.save(nova);
                });
        return Objects.requireNonNull(competencia);
    }

    private String getCellValue(Cell cell) {
        return cell == null ? "" : cell.toString().trim();
    }
}
