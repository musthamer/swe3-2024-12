#!/bin/bash
set -e

APP_NAME="my-impfregistrierung"
BASE_DIR=".."            
SRC_DIR="$BASE_DIR/src"
BUILD_DIR="$BASE_DIR/build"
LIB_DIR="$BASE_DIR/lib"
APP_DIR="$BASE_DIR/app"
TOMCAT_DIR="/opt/tomcat"
WEBAPP_DIR="$TOMCAT_DIR/webapps/$APP_NAME"

SERVLET_API_JAR="jakarta.servlet-api-6.0.0.jar"
JDBC_DRIVER="mariadb-java-client-3.3.1.jar"
JEDIS_JAR="jedis-5.2.0.jar"

echo "Kompiliere Java-Code ..."
javac -d "$BUILD_DIR" -cp "$LIB_DIR/$SERVLET_API_JAR:$LIB_DIR/$JDBC_DRIVER:$LIB_DIR/$JEDIS_JAR" $(find "$SRC_DIR" -name "*.java")
echo "Kompilierung abgeschlossen."

echo "Deployment nach $WEBAPP_DIR ..."
if [ ! -w "$TOMCAT_DIR" ]; then
    echo "Keine Schreibrechte für $TOMCAT_DIR. Bitte das Skript mit sudo ausführen."
    exit 1
fi

echo "Stoppe Tomcat ..."
sudo "$TOMCAT_DIR/bin/shutdown.sh" || true
sleep 2

rm -rf "$WEBAPP_DIR"
mkdir -p "$WEBAPP_DIR/WEB-INF/classes"

echo "Kopiere Webapplikation-Dateien ..."
cp -r "$APP_DIR/"* "$WEBAPP_DIR/"

echo "Kopiere kompilierte Klassen nach WEB-INF/classes ..."
cp -r "$BUILD_DIR/"* "$WEBAPP_DIR/WEB-INF/classes/"

echo "Starte Tomcat ..."
sudo "$TOMCAT_DIR/bin/startup.sh"

echo "Deployment abgeschlossen. Die Anwendung ist erreichbar unter:"
echo "http://localhost:8080/$APP_NAME/static/index.html"
