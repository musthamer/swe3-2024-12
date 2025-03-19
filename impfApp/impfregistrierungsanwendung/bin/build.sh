#!/bin/bash
set -e
SCRIPT_DIR="$(dirname "$0")"
echo "Starte den kompletten Build-Prozess..."
echo "1. Bibliotheken herunterladen..."
"$SCRIPT_DIR/download_libs.sh"
echo "2. Datenbank erstellen und befüllen..."
"$SCRIPT_DIR/create_database.sh"
echo "3. Quellcode kompilieren und WAR-Paket erstellen..."
"$SCRIPT_DIR/compile.sh"
echo "4. Anwendung deployen..."
"$SCRIPT_DIR/deploy.sh"
echo "Build-Prozess erfolgreich abgeschlossen."
