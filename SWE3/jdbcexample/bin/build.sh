#!/usr/bin/env bash
rm -rf build && mkdir -p build || exit 1
export mariadbuser="${USER}"
export mariadbdatabase="${USER}_db"
export mariadbpassword="$(grep ^password ~/.my.cnf|cut -d'=' -f2)"

javac -d build src/hbv/jdbcexample/*.java &&
 java -cp "build:lib/*" hbv.jdbcexample.JDBCMain
