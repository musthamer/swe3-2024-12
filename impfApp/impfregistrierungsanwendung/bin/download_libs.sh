#!/bin/bash
set -e
source "$(dirname "$0")/config.sh"
mkdir -p "$LIB_DIR"
ZXING_CORE_URL="https://repo1.maven.org/maven2/com/google/zxing/core/3.5.0/core-3.5.0.jar"
PDFBOX_URL="https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox/2.0.29/pdfbox-2.0.29.jar"
ZXING_JAVASE_URL="https://repo1.maven.org/maven2/com/google/zxing/javase/3.5.0/javase-3.5.0.jar"
COMMONS_LOGGING_URL="https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar"
FONTBOX_URL="https://repo1.maven.org/maven2/org/apache/pdfbox/fontbox/2.0.29/fontbox-2.0.29.jar"
COMMONS_POOL_URL="https://repo1.maven.org/maven2/org/apache/commons/commons-pool2/2.11.1/commons-pool2-2.11.1.jar"
SLF4J_API_URL="https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.3/slf4j-api-2.0.3.jar"
SLF4J_JDK14_URL="https://repo1.maven.org/maven2/org/slf4j/slf4j-jdk14/2.0.3/slf4j-jdk14-2.0.3.jar"





download_lib() {
  [ -f "$LIB_DIR/$2" ] || wget -q -P "$LIB_DIR" "$1"
}
echo "Lade benötigte Bibliotheken herunter..."
download_lib "$SERVLET_API_URL" "$SERVLET_API_JAR"
download_lib "$JDBC_DRIVER_URL" "$JDBC_DRIVER"
download_lib "$JEDIS_URL" "$JEDIS_JAR"
download_lib "$JSON_URL" "$JSON_JAR"
download_lib "$MAIL_URL" "$MAIL_JAR"
download_lib "$ZXING_CORE_URL" "core-3.5.0.jar"
download_lib "$PDFBOX_URL" "pdfbox-2.0.29.jar"
download_lib "$ZXING_JAVASE_URL" "javase-3.5.0.jar"
download_lib "$COMMONS_LOGGING_URL" "commons-logging-1.2.jar"
download_lib "$FONTBOX_URL" "fontbox-2.0.29.jar"
download_lib "$COMMONS_POOL_URL" "commons-pool2-2.11.1.jar"
download_lib "$SLF4J_API_URL" "slf4j-api-2.0.3.jar"
download_lib "$SLF4J_JDK14_URL" "slf4j-jdk14-2.0.3.jar"

cp "$LIB_DIR/"*.jar "$BASE_DIR/app/WEB-INF/lib/"

sudo cp "$LIB_DIR/$JDBC_DRIVER" "$TOMCAT_DIR/lib/"
echo "Bibliotheken wurden heruntergeladen und kopiert."
