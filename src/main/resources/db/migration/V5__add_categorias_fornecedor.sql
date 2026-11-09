-- RF-021: categorias de produtos do fornecedor (texto livre, ex.: "Carnes, Laticínios")
ALTER TABLE fornecedor ADD COLUMN categorias VARCHAR(200) NULL;
