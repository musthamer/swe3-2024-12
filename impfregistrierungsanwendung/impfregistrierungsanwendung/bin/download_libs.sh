#!/bin/bash
set -e
source "$(dirname "$0")/config.sh"
mkdir -p "$LIB_DIR"
download_lib() {
  [ -f "$LIB_DIR/$2" ] || wget -q -P "$LIB_DIR" "$1"
}
echo "Lade benötigte Bibliotheken herunter..."
download_lib "$SERVLET_API_URL" "$SERVLET_API_JAR"
download_lib "$JDBC_DRIVER_URL" "$JDBC_DRIVER"
download_lib "$JEDIS_URL" "$JEDIS_JAR"
download_lib "$JSON_URL" "$JSON_JAR"
download_lib "$MAIL_URL" "$MAIL_JAR"
mkdir -p "$BASE_DIR/app/WEB-INF/lib/"
cp "$LIB_DIR/"*.jar "$BASE_DIR/app/WEB-INF/lib/"
sudo cp "$LIB_DIR/$JDBC_DRIVER" "$TOMCAT_DIR/lib/"
echo "Bibliotheken wurden heruntergeladen und kopiert."
