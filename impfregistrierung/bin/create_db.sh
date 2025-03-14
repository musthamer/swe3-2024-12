#!/bin/bash
set -e

DB_USER="mohalzubaidy"
DB_PASS="sRby356wj3BXDiq9Y4S2"
DB_NAME="mohalzubaidy"

echo "Starte Erstellung/Aktualisierung der MariaDB-Datenbank..."

mariadb -u $DB_USER -p$DB_PASS <<EOF
DROP DATABASE IF EXISTS $DB_NAME;
CREATE DATABASE $DB_NAME;
USE $DB_NAME;

DROP TABLE IF EXISTS booking;
DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS user_account;
DROP TABLE IF EXISTS demo;

-- Tabelle user_account mit zusätzlichen Feldern
CREATE TABLE user_account (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(200) NOT NULL UNIQUE,
  vorname VARCHAR(100) NOT NULL,
  nachname VARCHAR(100) NOT NULL,
  telefon VARCHAR(50),
  geburtsdatum DATE,
  password_hash VARCHAR(200) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'user',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE appointment (
  appointment_id INT AUTO_INCREMENT PRIMARY KEY,
  date_slot DATE NOT NULL,
  time_slot VARCHAR(20) NOT NULL,
  vaccine VARCHAR(50) NOT NULL,
  capacity INT NOT NULL,
  remaining_capacity INT NOT NULL,
  location VARCHAR(100) NOT NULL,
  provider VARCHAR(100) NOT NULL
);

CREATE TABLE booking (
  booking_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  appointment_id INT NOT NULL,
  booking_name VARCHAR(200) DEFAULT NULL,
  booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES user_account(user_id),
  FOREIGN KEY (appointment_id) REFERENCES appointment(appointment_id)
);

CREATE TABLE demo (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100)
);

-- Beispieltermine mit Standort und Anbieter
INSERT INTO appointment (date_slot, time_slot, vaccine, capacity, remaining_capacity, location, provider) VALUES
('2025-04-01', '09:00-09:15', 'Biontech', 3, 3, 'Zentrum A', 'Provider A'),
('2025-04-01', '09:15-09:30', 'Biontech', 3, 3, 'Zentrum A', 'Provider A'),
('2025-04-01', '09:00-09:15', 'Moderna', 2, 2, 'Zentrum B', 'Provider B');

EOF

echo "Datenbank erfolgreich eingerichtet."
