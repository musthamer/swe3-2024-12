#!/bin/bash
set -e
source "$(dirname "$0")/config.sh"
echo "Erstelle und befülle die Datenbank '$DB_NAME'..."
sudo mysql <<EOF_SQL
USE mysql;
DROP DATABASE IF EXISTS $DB_NAME;
CREATE DATABASE $DB_NAME;
USE $DB_NAME;
DROP TABLE IF EXISTS booking, appointment, user_account, vaccine_center, vaccine_inventory, demo;
CREATE TABLE user_account (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(200) NOT NULL UNIQUE,
  vorname VARCHAR(100) NOT NULL,
  nachname VARCHAR(100) NOT NULL,
  telefon VARCHAR(50),
  geburtsdatum DATE,
  password_hash VARCHAR(200) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'user',
  assigned_center VARCHAR(100) DEFAULT NULL,
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
CREATE TABLE vaccine_center (
  center_id INT AUTO_INCREMENT PRIMARY KEY,
  center_name VARCHAR(100) NOT NULL UNIQUE
);
CREATE TABLE vaccine_inventory (
  inventory_id INT AUTO_INCREMENT PRIMARY KEY,
  center_id INT NOT NULL,
  vaccine VARCHAR(50) NOT NULL,
  stock INT NOT NULL,
  FOREIGN KEY (center_id) REFERENCES vaccine_center(center_id)
);
CREATE TABLE demo (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100)
);
EOF_SQL
for i in {1..30}; do
  sudo mysql -D $DB_NAME -e "INSERT IGNORE INTO vaccine_center (center_name) VALUES ('Zentrum $i');"
done
sudo mysql -D $DB_NAME <<EOF_SQL
INSERT INTO appointment (date_slot, time_slot, vaccine, capacity, remaining_capacity, location, provider)
VALUES
('2025-04-01', '09:00-09:15', 'Biontech', 3, 3, 'Zentrum 1', 'Provider A'),
('2025-04-01', '09:15-09:30', 'Biontech', 3, 3, 'Zentrum 1', 'Provider A'),
('2025-04-01', '09:00-09:15', 'Moderna', 2, 2, 'Zentrum 2', 'Provider B');
EOF_SQL
sudo mysql -D $DB_NAME <<EOF_SQL
INSERT INTO user_account (email, vorname, nachname, password_hash, role, assigned_center)
VALUES
('user@user.de', 'User', 'Demo', '5994471abb01112afcc18159f6cc74b4f511b99806da5d2e5f6fca1b1f2fbcff', 'user', NULL),
('doc@doc.de', 'Center', 'Demo', '0f4b0e8e1e63a7c791d12ac9a60edaa48e71d8f96d0d8f4a5c7289d77b75a47f', 'center', 'Zentrum 1'),
('admin@admin.de', 'Admin', 'Demo', '8c6976e5b5410415bde908bd4dee15dfb167a2a29a98216b7b01f5208f819d15', 'admin', NULL);
FLUSH PRIVILEGES;
EOF_SQL
echo "Datenbank wurde erstellt und befüllt."
