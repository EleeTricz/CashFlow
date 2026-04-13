-- Criação da tabela unificada de documentos fiscais
CREATE TABLE documento_fiscal (
    id BIGSERIAL PRIMARY KEY,
    tipo_documento VARCHAR(10) NOT NULL,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id),
    competencia VARCHAR(7) NOT NULL,
    data_pagamento DATE,
    data_vencimento DATE,
    valor_principal NUMERIC(15,2),
    valor_multa NUMERIC(15,2),
    valor_juros NUMERIC(15,2),
    valor_total NUMERIC(15,2),
    numero_documento VARCHAR(50),
    identificador_guia VARCHAR(50),
    periodo_apuracao VARCHAR(20),
    competencia_referida VARCHAR(7),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para performance
CREATE INDEX idx_doc_fiscal_empresa ON documento_fiscal(empresa_id);
CREATE INDEX idx_doc_fiscal_competencia ON documento_fiscal(competencia);
CREATE INDEX idx_doc_fiscal_tipo ON documento_fiscal(tipo_documento);
CREATE INDEX idx_doc_fiscal_empresa_comp ON documento_fiscal(empresa_id, competencia);
CREATE INDEX idx_doc_fiscal_numero ON documento_fiscal(numero_documento);

-- Tabela de itens de documentos fiscais
CREATE TABLE item_documento_fiscal (
    id BIGSERIAL PRIMARY KEY,
    documento_fiscal_id BIGINT NOT NULL REFERENCES documento_fiscal(id) ON DELETE CASCADE,
    codigo VARCHAR(20),
    descricao VARCHAR(255),
    principal NUMERIC(15,2),
    multa NUMERIC(15,2),
    juros NUMERIC(15,2),
    total NUMERIC(15,2)
);

CREATE INDEX idx_item_doc_fiscal ON item_documento_fiscal(documento_fiscal_id);
