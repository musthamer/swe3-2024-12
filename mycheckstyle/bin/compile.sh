#!/usr/bin/env bash
JAVAFILES=$(find ~/swe3-2024-12/mycheckstyle -name '*.java')
echo "COMPILE: $(echo $JAVAFILES|wc -w)"

javac --release 21 \
  -cp "/home/mohalzubaidy/swe3-2024-12/mycheckstyle/lib/checkstyle-all.jar" \
  -sourcepath src \
  -d ~/swe3-2024-12/mycheckstyle/build/ \
  $JAVAFILES

if [ $? -eq 0 ]; then
    echo "COMPILE: success"
else
    echo "COMPILE: failure"
    exit 1
fi
