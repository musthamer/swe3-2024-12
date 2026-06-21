CREATE TABLE IF NOT EXISTS vaccination_center (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  address VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS timeslot (
  id INT AUTO_INCREMENT PRIMARY KEY,
  center_id INT NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  capacity INT NOT NULL,
  FOREIGN KEY (center_id) REFERENCES vaccination_center(id)
);

CREATE TABLE IF NOT EXISTS vaccine (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  manufacturer VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS vaccination_center_vaccine (
  center_id INT NOT NULL,
  vaccine_id INT NOT NULL,
  available_doses INT NOT NULL DEFAULT 0,
  PRIMARY KEY (center_id, vaccine_id),
  FOREIGN KEY (center_id) REFERENCES vaccination_center(id),
  FOREIGN KEY (vaccine_id) REFERENCES vaccine(id)
);

CREATE TABLE IF NOT EXISTS person (
  id INT AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  date_of_birth DATE NOT NULL,
  email VARCHAR(100) NULL,
  account_id INT NULL
);

CREATE TABLE IF NOT EXISTS account (
  id INT AUTO_INCREMENT PRIMARY KEY,
  person_id INT NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  is_admin BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (person_id) REFERENCES person(id)
);

CREATE TABLE IF NOT EXISTS account_activation (
  id INT AUTO_INCREMENT PRIMARY KEY,
  account_id INT NOT NULL,
  activation_code VARCHAR(100) NOT NULL,
  expiry_datetime DATETIME NOT NULL,
  FOREIGN KEY (account_id) REFERENCES account(id)
);

CREATE TABLE IF NOT EXISTS booking (
  id INT AUTO_INCREMENT PRIMARY KEY,
  person_id INT NOT NULL,
  account_id INT NOT NULL,
  timeslot_id INT NOT NULL,
  vaccine_id INT NOT NULL,
  booking_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status ENUM('CONFIRMED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
  FOREIGN KEY (person_id) REFERENCES person(id),
  FOREIGN KEY (account_id) REFERENCES account(id),
  FOREIGN KEY (timeslot_id) REFERENCES timeslot(id),
  FOREIGN KEY (vaccine_id) REFERENCES vaccine(id)
);

-- Migration: COMPLETED als zulässigen Buchungsstatus sicherstellen
ALTER TABLE booking
  MODIFY COLUMN status ENUM('CONFIRMED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED';

INSERT INTO vaccine (name, manufacturer) VALUES
('Comirnaty', 'BioNTech/Pfizer'),
('Spikevax', 'Moderna'),
('Vaxzevria', 'AstraZeneca'),
('Jcovden', 'Johnson & Johnson');

INSERT INTO person (first_name, last_name, date_of_birth, email) VALUES
('Admin', 'System', '1980-01-01', 'admin@example.com');

INSERT INTO account (person_id, email, password_hash, is_admin) VALUES
(1, 'admin@example.com', 'c3VyZXNodW50dWxhQGdtYWlsLmNvbQ==:PBKDF2WithHmacSHA1:65536:128:oUkD5Q==', TRUE);

-- Bestehenden Admin-Eintrag entfernen, falls vorhanden
DELETE FROM account WHERE email = 'admin@example.com';
DELETE FROM person WHERE email = 'admin@example.com';

-- Admin-Benutzer einfügen (passwort: admin123)
INSERT INTO account (email, password_hash, is_admin) 
VALUES ('admin@impfservice.de', 'saKwZYtBB5Ps1myXz2WTdg==:I8LWCNR/kZmJUIM8AQE72g==', TRUE);

-- Impfzentren hinzufügen (falls noch nicht vorhanden)
INSERT INTO vaccination_center (name, address) VALUES
('Impfzentrum Berlin Mitte', 'Invalidenstr. 120, 10115 Berlin'),
('Impfzentrum München', 'Heimeranstr. 31, 80339 München'),
('Impfzentrum Hamburg', 'Karolinenstr. 3, 20357 Hamburg');

-- Impfstoffbestände für die Zentren aktualisieren/einfügen
INSERT INTO vaccination_center_vaccine (center_id, vaccine_id, available_doses) VALUES
(1, 1, 100), -- Berlin: BioNTech/Pfizer
(1, 2, 50),  -- Berlin: Moderna
(1, 3, 30),  -- Berlin: AstraZeneca
(2, 1, 150), -- München: BioNTech/Pfizer
(2, 4, 80),  -- München: Johnson & Johnson
(3, 1, 120), -- Hamburg: BioNTech/Pfizer
(3, 2, 70),  -- Hamburg: Moderna
(3, 4, 40)   -- Hamburg: Johnson & Johnson
ON DUPLICATE KEY UPDATE 
    available_doses = VALUES(available_doses);

-- Trigger hinzufügen, um negative Bestände zu verhindern
DELIMITER //
CREATE TRIGGER check_vaccine_stock BEFORE UPDATE ON vaccination_center_vaccine
FOR EACH ROW
BEGIN
    IF NEW.available_doses < 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Impfstoffbestand kann nicht negativ werden';
    END IF;
END;//
DELIMITER ;

-- Prüfen, ob die is_admin-Spalte bereits existiert
SET @exists = (SELECT COUNT(*) FROM information_schema.columns 
               WHERE table_schema = DATABASE() 
               AND table_name = 'account' 
               AND column_name = 'is_admin');

-- Wenn die Spalte noch nicht existiert, fügen wir sie hinzu
SET @query = IF(@exists = 0, 
                'ALTER TABLE account ADD COLUMN is_admin BOOLEAN DEFAULT FALSE',
                'SELECT "is_admin column already exists" AS message');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Prüfen, ob die role-Spalte existiert
SET @role_exists = (SELECT COUNT(*) FROM information_schema.columns 
                    WHERE table_schema = DATABASE() 
                    AND table_name = 'account' 
                    AND column_name = 'role');

-- Wenn die role-Spalte existiert, migrieren wir die Daten und entfernen sie
SET @migrate_query = IF(@role_exists = 1,
                        'UPDATE account SET is_admin = TRUE WHERE role = "ADMIN"',
                        'SELECT "No role column to migrate" AS message');
PREPARE stmt FROM @migrate_query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Optional: Entfernen der role-Spalte, wenn sie existiert
-- Auskommentiert, da dies ein destruktiver Vorgang ist und möglicherweise
-- erst nach erfolgreicher Migration durchgeführt werden sollte
-- SET @drop_query = IF(@role_exists = 1,
--                     'ALTER TABLE account DROP COLUMN role',
--                     'SELECT "No role column to drop" AS message');
-- PREPARE stmt FROM @drop_query;
-- EXECUTE stmt;
-- DEALLOCATE PREPARE stmt;

-- Entfernen der employee- und manager-Tabellen, falls sie existieren
-- und nicht mehr benötigt werden
-- Auskommentiert, da dies destruktive Vorgänge sind und möglicherweise
-- erst nach erfolgreicher Migration durchgeführt werden sollten
-- DROP TABLE IF EXISTS employee;
-- DROP TABLE IF EXISTS manager;

-- Erstellen eines initialen Administrators (falls noch keiner existiert)
-- Dieser SQL-Befehl sollte nach der Registrierung eines Admin-Benutzers ausgeführt werden
-- UPDATE account SET is_admin = TRUE WHERE email = 'admin@impfservice.de' LIMIT 1;

-- Migration: Drittpersonen-Buchung (account_id an person, E-Mail optional)
SET @person_account_id_exists = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
    AND table_name = 'person'
    AND column_name = 'account_id');
SET @add_person_account_id = IF(@person_account_id_exists = 0,
    'ALTER TABLE person ADD COLUMN account_id INT NULL',
    'SELECT "account_id column already exists" AS message');
PREPARE stmt FROM @add_person_account_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @person_email_nullable = (
    SELECT IS_NULLABLE FROM information_schema.columns
    WHERE table_schema = DATABASE()
    AND table_name = 'person'
    AND column_name = 'email'
    LIMIT 1
);
SET @make_email_nullable = IF(@person_email_nullable = 'NO',
    'ALTER TABLE person MODIFY COLUMN email VARCHAR(100) NULL',
    'SELECT "email already nullable" AS message');
PREPARE stmt FROM @make_email_nullable;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Entferne UNIQUE-Constraint auf person.email, falls vorhanden
SET @person_email_unique = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
    AND table_name = 'person'
    AND column_name = 'email'
    AND non_unique = 0
    AND index_name != 'PRIMARY'
);
SET @drop_email_unique = IF(@person_email_unique > 0,
    'ALTER TABLE person DROP INDEX email',
    'SELECT "no unique email index" AS message');
PREPARE stmt FROM @drop_email_unique;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;