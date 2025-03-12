#!/bin/bash 

git pull
git add . 
read COMMIT
git commit -m "{`$COMMIT`}" 
git push
