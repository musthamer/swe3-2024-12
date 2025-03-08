#!/usr/bin/env bash

function doget {
  local dst=$1; local version=$2; local fullpath="$3"
  local name=$dst-$version.jar
  local url="https://repo1.maven.org/maven2/$fullpath/$dst/$version/$dst-$version.jar"
  curl -f -s -o "lib/$name" "$url"
  echo "$name $?"
}

rm -f lib/*

doget mariadb-java-client 3.5.1 org/mariadb/jdbc
