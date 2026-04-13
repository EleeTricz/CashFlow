-- Migração de DARF
INSERT INTO documento_fiscal (
    tipo_documento, empresa_id, competencia, data_pagamento, data_vencimento,
    valor_principal, valor_multa, valor_juros, valor_total,
    numero_documento, periodo_apuracao
)
SELECT 
    'DARF',
    empresa_id,
    competencia,
    data_arrecadacao,
    data_vencimento,
    REPLACE(total_principal, ',', '.')::NUMERIC,
    REPLACE(total_multa, ',', '.')::NUMERIC,
    REPLACE(total_juros, ',', '.')::NUMERIC,
    REPLACE(total_total, ',', '.')::NUMERIC,
    numero_documento,
    periodo_apuracao
FROM lancamento_darf;

-- Migração de DAS
INSERT INTO documento_fiscal (
    tipo_documento, empresa_id, competencia, data_pagamento, data_vencimento,
    valor_principal, valor_multa, valor_juros, valor_total,
    numero_documento
)
SELECT 
    'DAS',
    empresa_id_id,
    competencia,
    data_arrecadacao,
    data_vencimento,
    REPLACE(principal, ',', '.')::NUMERIC,
    REPLACE(multa, ',', '.')::NUMERIC,
    REPLACE(juros, ',', '.')::NUMERIC,
    REPLACE(total, ',', '.')::NUMERIC,
    numero_documento
FROM lancamento_das;

-- Migração de FGTS
INSERT INTO documento_fiscal (
    tipo_documento, empresa_id, competencia, data_pagamento,
    valor_principal, valor_multa, valor_juros,
    identificador_guia
)
SELECT 
    'FGTS',
    empresa_id,
    competencia,
    data_pagamento,
    REPLACE(valor_principal, ',', '.')::NUMERIC,
    REPLACE(multa, ',', '.')::NUMERIC,
    REPLACE(juros, ',', '.')::NUMERIC,
    identificador_guia
FROM lancamento_fgts;

-- Migração de DAE
INSERT INTO documento_fiscal (
    tipo_documento, empresa_id, competencia, competencia_referida,
    data_pagamento, data_vencimento,
    valor_principal,
    numero_documento
)
SELECT 
    'DAE',
    empresa_id,
    competencia,
    competencia_referida,
    data_arrecadacao,
    data_vencimento,
    REPLACE(REPLACE(valor, '.', ''), ',', '.')::NUMERIC,
    numero_documento_origem
FROM lancamento_dae;

-- Migração de itens DARF
INSERT INTO item_documento_fiscal (
    documento_fiscal_id, codigo, descricao, principal, multa, juros, total
)
SELECT 
    df.id,
    i.codigo,
    i.descricao,
    REPLACE(i.principal, ',', '.')::NUMERIC,
    REPLACE(i.multa, ',', '.')::NUMERIC,
    REPLACE(i.juros, ',', '.')::NUMERIC,
    REPLACE(i.total, ',', '.')::NUMERIC
FROM itens_darf i
JOIN lancamento_darf ld ON i.darf_id = ld.id
JOIN documento_fiscal df ON df.empresa_id = ld.empresa_id 
    AND df.numero_documento = ld.numero_documento 
    AND df.tipo_documento = 'DARF';
