#!/usr/bin/env bash
source local/config.txt || exit 1
mkdir -p build target
echo "PREPARE"
cp -r app/* build
sed -e "s/REDISSERVER/${redisserver}/g" \
    -e "s/REDISPASSWORD/${redispassword}/g" \
    -e "s|BASEURL|${baseurl}|g" \
    -e "s/WEBAPP/${webapp}/g" \
    app/WEB-INF/web.xml > build/WEB-INF/web.xml
sed  -e "s/DBSERVER/${dbserver}/g" -e "s/DBUSER/${dbuser}/g" -e "s/DBPASSWORD/${dbpassword}/g" -e "s/DBNAME/${dbname}/g" app/META-INF/context.xml  > build/META-INF/context.xml
