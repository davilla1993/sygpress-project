/*
-- Insertion des utilisateurs par défaut
-- Mot de passe Admin: Admin@123
-- Mot de passe User: User@123

INSERT INTO users (
    id,
    public_id,
    username,
    email,
    password,
    first_name,
    last_name,
    phone,
    role,
    enabled,
    account_non_expired,
    account_non_locked,
    credentials_non_expired,
    must_change_password,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    deleted_by
) VALUES (
    1,
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'admin',
    'admin@sygpress.com',
    '$2a$10$YX8ibsWuFIshWVUQKoqIoeWZQ.JKAay.IvJY4U0EBcvE85v928HSy',
    'Administrateur',
    'Système',
    '+228 90 00 00 01',
    'ADMIN',
    true,
    true,
    true,
    true,
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    'SYSTEM',
    false,
    NULL,
    NULL
);

INSERT INTO users (
    id,
    public_id,
    username,
    email,
    password,
    first_name,
    last_name,
    phone,
    role,
    enabled,
    account_non_expired,
    account_non_locked,
    credentials_non_expired,
    must_change_password,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    deleted_by
) VALUES (
    2,
    'b2c3d4e5-f6a7-8901-bcde-f23456789012',
    'user',
    'user@sygpress.com',
    '$2a$10$nB1WsZ7mMlfkqX0fAshJW.ibR1Hplc9Exp0u7de2H3YLngzYGssYy',
    'Utilisateur',
    'Standard',
    '+228 90 00 00 02',
    'USER',
    true,
    true,
    true,
    true,
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    'SYSTEM',
    false,
    NULL,
    NULL
);

-- Mise à jour de la séquence pour éviter les conflits d'ID
ALTER SEQUENCE users_seq RESTART WITH 3;
*/
