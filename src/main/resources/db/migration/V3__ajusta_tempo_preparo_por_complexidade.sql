-- Tempo de preparo individual por prato, de acordo com a complexidade do preparo.
-- (V2 deixou vários pratos da mesma categoria com o mesmo tempo.)
-- Entradas
UPDATE prato SET tempo_preparo_min = 20 WHERE nome = 'Casquinhas de Siri';
UPDATE prato SET tempo_preparo_min = 25 WHERE nome = 'Caldo de Sururu';
UPDATE prato SET tempo_preparo_min = 18 WHERE nome = 'Mini Acarajés';
UPDATE prato SET tempo_preparo_min = 25 WHERE nome = 'Mini Abarás';
UPDATE prato SET tempo_preparo_min = 12 WHERE nome = 'Mini Pastel de Camarão';
UPDATE prato SET tempo_preparo_min = 12 WHERE nome = 'Porção de Pitinga';
-- Pratos principais
UPDATE prato SET tempo_preparo_min = 40 WHERE nome = 'Moqueca de Peixe';
UPDATE prato SET tempo_preparo_min = 35 WHERE nome = 'Bobó de Camarão';
UPDATE prato SET tempo_preparo_min = 30 WHERE nome = 'Carne do Sol com Purê';
UPDATE prato SET tempo_preparo_min = 25 WHERE nome = 'Baião de Dois';
UPDATE prato SET tempo_preparo_min = 45 WHERE nome = 'Arroz de Polvo';
UPDATE prato SET tempo_preparo_min = 25 WHERE nome = 'Camarão à Baiana';
-- Sobremesas
UPDATE prato SET tempo_preparo_min = 8  WHERE nome = 'Torta de Cocada';
UPDATE prato SET tempo_preparo_min = 5  WHERE nome = 'Pudim de Tapioca';
UPDATE prato SET tempo_preparo_min = 12 WHERE nome = 'Bolinho de Estudante';
UPDATE prato SET tempo_preparo_min = 5  WHERE nome = 'Bala Baiana na Travessa';
UPDATE prato SET tempo_preparo_min = 8  WHERE nome = 'Queijadinha';
UPDATE prato SET tempo_preparo_min = 5  WHERE nome = 'Doce de Leite Talhado';
UPDATE prato SET tempo_preparo_min = 5  WHERE nome = 'Pudim de Tapioca com Leite Condensado';
-- Bebidas
UPDATE prato SET tempo_preparo_min = 2  WHERE nome IN ('Água', 'Água com Gás', 'Refrigerante', 'Cerveja 600ml');
UPDATE prato SET tempo_preparo_min = 6  WHERE nome = 'Sucos Naturais';
-- Drinks
UPDATE prato SET tempo_preparo_min = 5  WHERE nome = 'Caipirinhas e Caipiroskas';
UPDATE prato SET tempo_preparo_min = 3  WHERE nome = 'Cravinho';
UPDATE prato SET tempo_preparo_min = 6  WHERE nome = 'Coice de Mula';
UPDATE prato SET tempo_preparo_min = 8  WHERE nome = 'Drinks Tropicais';
UPDATE prato SET tempo_preparo_min = 4  WHERE nome = 'Gin Tônica';
