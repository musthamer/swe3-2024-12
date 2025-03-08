#!/bin/bash
set -e

APP_NAME="my-impfregistrierung"

echo "Starte den kompletten Setup-Prozess für $APP_NAME ..."

echo "Starte Datenbank-Setup ..."
./create_db.sh

echo "Starte Bibliotheken-Download ..."
./download_libs.sh

echo "Starte Build & Deployment ..."
./build.sh

echo "Alle Schritte abgeschlossen."
