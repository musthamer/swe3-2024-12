#!/bin/bash
set -e
source "$(dirname "$0")/config.sh"
echo "Deploye WAR-Paket nach Tomcat..."

sudo cp "$WAR_FILE" "$WEBAPP_DIR".war
sleep 10

#  Hinzufügen der PDF-, QR-Code- und E-Mail-Verzeichnisse
if [ -d "$BUILD_DIR/pdf" ]; then
  sudo cp -r "$BUILD_DIR/pdf" "$TOMCAT_DIR/webapps/$APP_NAME/"
fi

if [ -d "$BUILD_DIR/qrcodes" ]; then
  sudo cp -r "$BUILD_DIR/qrcodes" "$TOMCAT_DIR/webapps/$APP_NAME/"
fi

if [ -d "$BUILD_DIR/email_logs" ]; then
  sudo cp -r "$BUILD_DIR/email_logs" "$TOMCAT_DIR/webapps/$APP_NAME/"
fi

if [ -d "$TOMCAT_DIR/webapps/$APP_NAME/pdfgen" ]; then
  sudo chmod -R 755 "$TOMCAT_DIR/webapps/$APP_NAME/pdfgen"
else
  echo "WARNUNG: Verzeichnis '$TOMCAT_DIR/webapps/$APP_NAME/pdfgen' nicht gefunden."
  echo "Bitte prüfen Sie, ob Tomcat die Anwendung vollständig entpackt hat."
fi

echo "Deployment abgeschlossen. Bitte Tomcat ggf. neu starten."

