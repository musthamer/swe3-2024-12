#!/bin/bash
set -e
source "$(dirname "$0")/config.sh"
echo "Deploye WAR-Paket nach Tomcat..."
sudo cp "$WAR_FILE" "$WEBAPP_DIR".war
sleep 10
if [ -d "$TOMCAT_DIR/webapps/$APP_NAME/pdfgen" ]; then
  sudo chmod -R 755 "$TOMCAT_DIR/webapps/$APP_NAME/pdfgen"
else
  echo "WARNUNG: Verzeichnis '$TOMCAT_DIR/webapps/$APP_NAME/pdfgen' nicht gefunden."
  echo "Bitte prüfen Sie, ob Tomcat die Anwendung vollständig entpackt hat."
fi
echo "Deployment abgeschlossen. Bitte Tomcat ggf. neu starten."
