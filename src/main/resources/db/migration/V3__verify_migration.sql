-- Verificação de contagem de registros migrados
SELECT 
    'DARF' as tipo,
    COUNT(*) as quantidade_nova,
    (SELECT COUNT(*) FROM lancamento_darf) as quantidade_original
FROM documento_fiscal 
WHERE tipo_documento = 'DARF'

UNION ALL

SELECT 
    'DAS',
    COUNT(*),
    (SELECT COUNT(*) FROM lancamento_das)
FROM documento_fiscal 
WHERE tipo_documento = 'DAS'

UNION ALL

SELECT 
    'FGTS',
    COUNT(*),
    (SELECT COUNT(*) FROM lancamento_fgts)
FROM documento_fiscal 
WHERE tipo_documento = 'FGTS'

UNION ALL

SELECT 
    'DAE',
    COUNT(*),
    (SELECT COUNT(*) FROM lancamento_dae)
FROM documento_fiscal 
WHERE tipo_documento = 'DAE';

-- Verificação de itens migrados
SELECT 
    'Itens DARF' as tipo,
    COUNT(*) as quantidade_nova,
    (SELECT COUNT(*) FROM itens_darf) as quantidade_original
FROM item_documento_fiscal;

-- Verificação de documentos sem itens (DARF deve ter itens)
SELECT 
    df.id,
    df.numero_documento,
    df.tipo_documento
FROM documento_fiscal df
LEFT JOIN item_documento_fiscal idf ON idf.documento_fiscal_id = df.id
WHERE df.tipo_documento = 'DARF' 
AND idf.id IS NULL;
