#!/bin/bash

OUTPUT_FILE="gesamter_quellcode.txt"

touch "$OUTPUT_FILE"

find . -type f -print0 | while IFS= read -r -d $'\0' file; do
  echo "========================================" >> "$OUTPUT_FILE"
  echo "Datei: $file" >> "$OUTPUT_FILE"
  echo "========================================" >> "$OUTPUT_FILE"
  cat "$file" >> "$OUTPUT_FILE"
  echo "" >> "$OUTPUT_FILE"
done

echo "Der Quellcode aller Dateien wurde in '$OUTPUT_FILE' gespeichert."
