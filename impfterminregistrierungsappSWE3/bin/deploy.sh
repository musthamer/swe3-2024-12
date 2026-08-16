#!/usr/bin/env bash
source local/config.txt || exit 1

echo "DEPLOY:   target/webapp.war 
DEPLOY:   TO $baseurl/$manager 
DEPLOY:   AS $webapp"
if curl --silent --location --netrc-file local/netrc --fail \
  --upload-file "target/webapp.war" "$baseurl/$manager/text/deploy?path=/$webapp&update=true"; then
  echo "DEPLOY:   success"
  exit 0
fi

# Fallback for local Docker setups when Manager API is not reachable/allowed.
if docker ps --format '{{.Names}}' | grep -qx "manager"; then
  echo "DEPLOY:   manager API failed, using docker cp fallback"
  docker exec manager sh -lc "rm -rf /usr/local/tomcat/webapps/$webapp /usr/local/tomcat/webapps/$webapp.war"
  docker cp "target/webapp.war" "manager:/usr/local/tomcat/webapps/$webapp.war"
  echo "DEPLOY:   success (docker fallback)"
  exit 0
fi

echo "DEPLOY:   failure"
exit 1

