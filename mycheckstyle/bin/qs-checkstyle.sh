#!/usr/bin/env bash
java -cp build:"lib/*" com.puppycrawl.tools.checkstyle.Main -c misc/checkstyle-my-rules.xml src/hbv/checkstyledemo/
