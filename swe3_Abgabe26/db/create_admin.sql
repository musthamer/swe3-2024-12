-- Bestehende Admin-Accounts entfernen
DELETE FROM account WHERE email = 'admin@impfservice.de';

-- Admin-Account mit sicherem Hash erstellen
INSERT INTO account (email, password_hash, is_admin) 
VALUES (
    'admin@impfservice.de',
    'PBKDF2WithHmacSHA1:65536:128:4NQPxKHkmvDEkN9d4vD/wQ==:hxr1kkQ9B9yWCYDw9I9qSg==',  -- Hash für 'admin123'
    TRUE
); 