-- Initialisation de la séquence pour les numéros de facture
-- Cette migration garantit que la table sequence existe et est initialisée

-- Créer la table sequence si elle n'existe pas déjà
CREATE TABLE IF NOT EXISTS sequence (
    id BIGINT PRIMARY KEY,
    last_number BIGINT NOT NULL
);

-- Initialiser la séquence avec l'ID 1 et le numéro 0
-- ON CONFLICT permet d'éviter les erreurs si la ligne existe déjà
INSERT INTO sequence (id, last_number)
VALUES (1, 0)
ON CONFLICT (id) DO NOTHING;
