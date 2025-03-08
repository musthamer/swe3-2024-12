#/bin/bash
source local/config.txt || exit 1
rm -rf build/* target/*
javafiles=$(find src -name '*.java')
javac --release 21 -cp 'lib/*' -d build/WEB-INF/classes "$javafiles"
jar -cf 'target/webapp.war' -C build . &&
curl --silent --location --netrc-file local/netrc --fail --upload-file target/webapp.war "$baseurl/$manager/text/deploy?path=/$webapp&update=true" &&
#curl -s "$baseurl/$webapp"
curl -s "https://informatik.hs-bremerhaven.de/$prefix-java/hello"
