INSERT INTO vaccination_center (name, address) VALUES
('Impfzentrum Nemerb-Mitte', 'Bürgermeister-Timhs-Str. 35, 28195 Nemerb'),
('Impfzentrum Nemerb-Nord', 'Heidebecker Str. 228, 28755 Nemerb'),
('Impfzentrum Nemerb-Süd', 'Neuenland-Allee 30, 28199 Nemerb');

UPDATE vaccine SET name = 'Comirnaty', manufacturer = 'BioNTech/Pfizer' WHERE id = 1;
UPDATE vaccine SET name = 'Spikevax', manufacturer = 'Moderna' WHERE id = 2;
UPDATE vaccine SET name = 'Vaxzevria', manufacturer = 'AstraZeneca' WHERE id = 3;
UPDATE vaccine SET name = 'Janssen', manufacturer = 'Johnson & Johnson' WHERE id = 4;

INSERT INTO vaccination_center_vaccine (center_id, vaccine_id, available_doses) VALUES
(1, 1, 100), -- Comirnaty (BioNTech/Pfizer) in Nemerb-Mitte
(1, 2, 75),  -- Spikevax (Moderna) in Nemerb-Mitte
(1, 3, 50),  -- Vaxzevria (AstraZeneca) in Nemerb-Mitte
(1, 4, 30),  -- Janssen (Johnson & Johnson) in Nemerb-Mitte

(2, 1, 80),  -- Comirnaty (BioNTech/Pfizer) in Nemerb-Nord
(2, 2, 60),  -- Spikevax (Moderna) in Nemerb-Nord
(2, 3, 40),  -- Vaxzevria (AstraZeneca) in Nemerb-Nord
(2, 4, 25),  -- Janssen (Johnson & Johnson) in Nemerb-Nord

(3, 1, 90),  -- Comirnaty (BioNTech/Pfizer) in Nemerb-Süd
(3, 2, 70),  -- Spikevax (Moderna) in Nemerb-Süd
(3, 3, 45),  -- Vaxzevria (AstraZeneca) in Nemerb-Süd
(3, 4, 35);  -- Janssen (Johnson & Johnson) in Nemerb-Süd

-- Bestehende Termine löschen
DELETE FROM timeslot;

-- Neue Termine einfügen
INSERT INTO timeslot (center_id, start_time, end_time, capacity) VALUES
-- Nemerb-Mitte
(1, '2024-03-25 09:00:00', '2024-03-25 09:30:00', 5),
(1, '2024-03-25 09:30:00', '2024-03-25 10:00:00', 5),
(1, '2024-03-25 10:00:00', '2024-03-25 10:30:00', 5),
(1, '2024-03-25 10:30:00', '2024-03-25 11:00:00', 5),
(1, '2024-03-25 11:00:00', '2024-03-25 11:30:00', 5),

-- Nemerb-Nord
(2, '2024-03-26 09:00:00', '2024-03-26 09:30:00', 5),
(2, '2024-03-26 09:30:00', '2024-03-26 10:00:00', 5),
(2, '2024-03-26 10:00:00', '2024-03-26 10:30:00', 5),
(2, '2024-03-26 10:30:00', '2024-03-26 11:00:00', 5),
(2, '2024-03-26 11:00:00', '2024-03-26 11:30:00', 5),

-- Nemerb-Süd
(3, '2024-03-27 09:00:00', '2024-03-27 09:30:00', 5),
(3, '2024-03-27 09:30:00', '2024-03-27 10:00:00', 5),
(3, '2024-03-27 10:00:00', '2024-03-27 10:30:00', 5),
(3, '2024-03-27 10:30:00', '2024-03-27 11:00:00', 5),
(3, '2024-03-27 11:00:00', '2024-03-27 11:30:00', 5);

-- Termine für die nächste Woche kopieren
INSERT INTO timeslot (center_id, start_time, end_time, capacity)
SELECT 
    center_id,
    DATE_ADD(start_time, INTERVAL 1 WEEK),
    DATE_ADD(end_time, INTERVAL 1 WEEK),
    capacity
FROM timeslot;

-- Bestehenden Admin-Account löschen (falls vorhanden)
DELETE FROM account WHERE email = 'admin@impfservice.de';

-- Admin-Account einfügen (Hash wird durch AdminPasswordGenerator generiert)
-- Führen Sie zuerst AdminPasswordGenerator aus und ersetzen Sie den folgenden INSERT
-- mit dem generierten SQL-Statement
INSERT INTO account (email, password_hash, is_admin) 
VALUES ('admin@impfservice.de', 'GENERATED_HASH_HERE', TRUE);