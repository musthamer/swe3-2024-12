-- Einfügen eines Admin-Benutzers in die Datenbank
INSERT INTO person (first_name, last_name, date_of_birth, email) 
VALUES ('Admin', 'User', '1990-01-01', 'admin@impfservice.de');

-- Admin-ID abrufen
SET @admin_person_id = LAST_INSERT_ID();

-- Passwort ist 'admin123' (Beispiel-Hash), Admin-Rechte über is_admin
INSERT INTO account (person_id, email, password_hash, is_admin) 
VALUES (@admin_person_id, 'admin@impfservice.de', 
        'RDdJ8s9nL1xVwZYsQH7MFA==:D7pUbRtlpJ0zrw5Ipw5wdg==', TRUE);