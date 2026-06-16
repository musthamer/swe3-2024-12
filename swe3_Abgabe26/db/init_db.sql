CREATE DATABASE IF NOT EXISTS lskupzig_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE lskupzig_db;

-- Foreign Keys temporär deaktivieren
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS booking;
DROP TABLE IF EXISTS account_activation;
DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS person;
DROP TABLE IF EXISTS vaccination_center_vaccine;
DROP TABLE IF EXISTS timeslot;
DROP TABLE IF EXISTS vaccine;
DROP TABLE IF EXISTS vaccination_center;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 1) Schema
-- =============================================================================

CREATE TABLE vaccination_center (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  address VARCHAR(255) NOT NULL
);

CREATE TABLE vaccine (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  manufacturer VARCHAR(100) NOT NULL
);

CREATE TABLE timeslot (
  id INT AUTO_INCREMENT PRIMARY KEY,
  center_id INT NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  capacity INT NOT NULL,
  FOREIGN KEY (center_id) REFERENCES vaccination_center(id)
);

CREATE TABLE vaccination_center_vaccine (
  center_id INT NOT NULL,
  vaccine_id INT NOT NULL,
  available_doses INT NOT NULL DEFAULT 0,
  PRIMARY KEY (center_id, vaccine_id),
  FOREIGN KEY (center_id) REFERENCES vaccination_center(id),
  FOREIGN KEY (vaccine_id) REFERENCES vaccine(id)
);

CREATE TABLE person (
  id INT AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  date_of_birth DATE NOT NULL,
  email VARCHAR(100) NULL,
  account_id INT NULL
);

CREATE TABLE account (
  id INT AUTO_INCREMENT PRIMARY KEY,
  person_id INT NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  is_admin BOOLEAN NOT NULL DEFAULT FALSE,
  FOREIGN KEY (person_id) REFERENCES person(id)
);

CREATE TABLE account_activation (
  id INT AUTO_INCREMENT PRIMARY KEY,
  account_id INT NOT NULL,
  activation_code VARCHAR(100) NOT NULL,
  expiry_datetime DATETIME NOT NULL,
  FOREIGN KEY (account_id) REFERENCES account(id)
);

CREATE TABLE booking (
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

DELIMITER //
CREATE TRIGGER check_vaccine_stock
BEFORE UPDATE ON vaccination_center_vaccine
FOR EACH ROW
BEGIN
  IF NEW.available_doses < 0 THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Impfstoffbestand kann nicht negativ werden';
  END IF;
END//
DELIMITER ;

-- =============================================================================
-- 2) Stammdaten – Impfstoffe
-- =============================================================================

INSERT INTO vaccine (name, manufacturer) VALUES
('Comirnaty',  'BioNTech/Pfizer'),
('Spikevax',   'Moderna'),
('Vaxzevria',  'AstraZeneca'),
('Janssen',    'Johnson & Johnson');

-- =============================================================================
-- 3) Stammdaten – Impfzentren (Stadt Nemerb)
-- =============================================================================

INSERT INTO vaccination_center (name, address) VALUES
('Impfzentrum Nemerb-Mitte', 'Bürgermeister-Timhs-Str. 35, 28195 Nemerb'),
('Impfzentrum Nemerb-Nord',  'Heidebecker Str. 228, 28755 Nemerb'),
('Impfzentrum Nemerb-Süd',   'Neuenland-Allee 30, 28199 Nemerb');

-- =============================================================================
-- 4) Impfstoffbestände pro Zentrum
-- =============================================================================

INSERT INTO vaccination_center_vaccine (center_id, vaccine_id, available_doses) VALUES
(1, 1, 100), (1, 2, 75), (1, 3, 50), (1, 4, 30),
(2, 1, 80),  (2, 2, 60), (2, 3, 40), (2, 4, 25),
(3, 1, 90),  (3, 2, 70), (3, 3, 45), (3, 4, 35);

-- =============================================================================
-- 5) Termine (ab morgen, 2 Wochen, Mo–Fr je Zentrum)
--    Slots werden relativ zu CURDATE() erzeugt → immer buchbar
-- =============================================================================

INSERT INTO timeslot (center_id, start_time, end_time, capacity)
SELECT
  c.center_id,
  TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL d.day_offset DAY), TIME(c.slot_time)),
  TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL d.day_offset DAY), TIME(ADDTIME(c.slot_time, '00:30:00'))),
  c.capacity
FROM (
  SELECT 1 AS center_id, '09:00:00' AS slot_time, 5 AS capacity UNION ALL
  SELECT 1, '09:30:00', 5 UNION ALL
  SELECT 1, '10:00:00', 5 UNION ALL
  SELECT 1, '10:30:00', 5 UNION ALL
  SELECT 1, '11:00:00', 5 UNION ALL
  SELECT 2, '09:00:00', 5 UNION ALL
  SELECT 2, '09:30:00', 5 UNION ALL
  SELECT 2, '10:00:00', 5 UNION ALL
  SELECT 2, '10:30:00', 5 UNION ALL
  SELECT 2, '11:00:00', 5 UNION ALL
  SELECT 3, '09:00:00', 5 UNION ALL
  SELECT 3, '09:30:00', 5 UNION ALL
  SELECT 3, '10:00:00', 5 UNION ALL
  SELECT 3, '10:30:00', 5 UNION ALL
  SELECT 3, '11:00:00', 5
) AS c
JOIN (
  SELECT 1 AS day_offset UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL
  SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
) AS d
WHERE DAYOFWEEK(DATE_ADD(CURDATE(), INTERVAL d.day_offset DAY)) NOT IN (1, 7);

-- =============================================================================
-- 6) Admin-Benutzer
--    Passwort: admin123  (PBKDF2, kompatibel mit PasswordUtils)
-- =============================================================================

INSERT INTO person (first_name, last_name, date_of_birth, email)
VALUES ('Admin', 'User', '1990-01-01', 'admin@impfservice.de');

SET @admin_person_id = LAST_INSERT_ID();

INSERT INTO account (person_id, email, password_hash, is_admin)
VALUES (
  @admin_person_id,
  'admin@impfservice.de',
  'PBKDF2WithHmacSHA1:65536:128:4NQPxKHkmvDEkN9d4vD/wQ==:hxr1kkQ9B9yWCYDw9I9qSg==',
  TRUE
);

-- =============================================================================
-- 7) Kurze Prüfung
-- =============================================================================

SELECT 'vaccination_center' AS tabelle, COUNT(*) AS anzahl FROM vaccination_center
UNION ALL SELECT 'vaccine',              COUNT(*) FROM vaccine
UNION ALL SELECT 'timeslot',             COUNT(*) FROM timeslot
UNION ALL SELECT 'account (admin)',      COUNT(*) FROM account WHERE is_admin = TRUE
UNION ALL SELECT 'person',               COUNT(*) FROM person;

SELECT 'Initialisierung abgeschlossen.' AS status;