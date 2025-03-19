#!/bin/bash
set -e
source "$(dirname "$0")/config.sh"
echo "Kompiliere Java-Quellcode..."
javac -d "$CLASSES_DIR" -cp "$BASE_DIR/app/WEB-INF/lib/*" $(find "$SRC_DIR" -name "*.java")
echo "Kompilierung abgeschlossen."
echo "Erstelle WAR-Paket..."
mkdir -p "$BUILD_DIR/war/WEB-INF/classes"
cp -r "$CLASSES_DIR"/* "$BUILD_DIR/war/WEB-INF/classes/"
cp -r "$BASE_DIR/app/static" "$BUILD_DIR/war/"
cp -r "$BASE_DIR/pdfgen" "$BUILD_DIR/war/"
cp -r "$BASE_DIR/app/WEB-INF" "$BUILD_DIR/war/"
cp -r "$BASE_DIR/app/META-INF" "$BUILD_DIR/war/"
if [ -f "$BUILD_DIR/war/pdfgen/generate_appointment_pdf.sh" ]; then
  chmod -R 755 "$BUILD_DIR/war/pdfgen"
else
  echo "Fehler: Datei pdfgen/generate_appointment_pdf.sh wurde nicht gefunden."
  exit 1
fi
cd "$BUILD_DIR/war"
jar -cvf "$WAR_FILE" *
cd "$BASE_DIR"
echo "WAR-Paket erstellt: $WAR_FILE"
