#!/bin/bash
set -e

BASE_DIR=".."              
LIB_DIR="$BASE_DIR/lib"
APP_DIR="$BASE_DIR/app"
TOMCAT_DIR="/opt/tomcat"

# Bibliotheksnamen und URLs
SERVLET_API_JAR="jakarta.servlet-api-6.0.0.jar"
JDBC_DRIVER="mariadb-java-client-3.3.1.jar"
JEDIS_JAR="jedis-5.2.0.jar"

SERVLET_API_URL="https://repo1.maven.org/maven2/jakarta/servlet/jakarta.servlet-api/6.0.0/$SERVLET_API_JAR"
JDBC_DRIVER_URL="https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/3.3.1/$JDBC_DRIVER"
JEDIS_URL="https://repo1.maven.org/maven2/redis/clients/jedis/5.2.0/$JEDIS_JAR"

mkdir -p "$LIB_DIR"

download_lib() {
  local url=$1
  local dest="$LIB_DIR/$(basename $url)"
  if [ ! -f "$dest" ]; then
    echo "Lade $(basename $dest) herunter ..."
    wget -q -P "$LIB_DIR" "$url"
  else
    echo "$(basename $dest) ist bereits vorhanden."
  fi
}

download_lib "$SERVLET_API_URL"
download_lib "$JDBC_DRIVER_URL"
download_lib "$JEDIS_URL"

echo "Kopiere Bibliotheken in WEB-INF/lib ..."
cp "$LIB_DIR/"*.jar "$APP_DIR/WEB-INF/lib/"

echo "Kopiere JDBC-Treiber in Tomcat lib ..."
sudo cp "$LIB_DIR/$JDBC_DRIVER" "$TOMCAT_DIR/lib/"

echo "Bibliotheken wurden erfolgreich heruntergeladen und kopiert."
