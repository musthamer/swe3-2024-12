#!/bin/bash
git pull
git add .
echo "AUFGABE|NEUE FUNKTION|FEHLERBEHEBUNG|CODE-VERBESSERUNG: Beschreibung"
read COMMIT
git commit -m "$COMMIT"
git push

