-- Migration pour adapter la base de données au FCFA (sans décimales)
-- Le FCFA n'utilise pas de décimales, tous les montants sont des entiers

-- Arrondir les valeurs décimales existantes dans invoice
UPDATE invoice
SET
    discount = ROUND(discount, 0),
    vat_amount = ROUND(vat_amount, 0),
    amount_paid = ROUND(amount_paid, 0),
    remaining_amount = ROUND(remaining_amount, 0);

-- Arrondir les valeurs décimales existantes dans pricing
UPDATE pricing
SET price = ROUND(price, 0);

-- Arrondir les valeurs décimales existantes dans additional_fees
UPDATE additional_fees
SET amount = ROUND(amount, 0);

-- Arrondir les valeurs décimales existantes dans invoice_line
UPDATE invoice_line
SET amount = ROUND(amount, 0);

-- Modifier la précision des colonnes (de decimal(10,2) à decimal(10,0))
-- Invoice
ALTER TABLE invoice ALTER COLUMN discount TYPE NUMERIC(10,0);
ALTER TABLE invoice ALTER COLUMN vat_amount TYPE NUMERIC(10,0);
ALTER TABLE invoice ALTER COLUMN amount_paid TYPE NUMERIC(10,0);
ALTER TABLE invoice ALTER COLUMN remaining_amount TYPE NUMERIC(10,0);

-- Pricing
ALTER TABLE pricing ALTER COLUMN price TYPE NUMERIC(10,0);

-- Additional Fees
ALTER TABLE additional_fees ALTER COLUMN amount TYPE NUMERIC(10,0);

-- Invoice Line
ALTER TABLE invoice_line ALTER COLUMN amount TYPE NUMERIC(10,0);
