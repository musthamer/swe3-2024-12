#!/usr/bin/env bash
set -e
IFS=""
read -d";" firstname
read -d";" lastname
read -d";" birthdate
read -d";" street
read -d";" city
read -d";" postalcode
read -d";" starttime
read -d";" vaccine
read -d";" vaccinationcenter
read -d";" validationcode
mkdir -p ./tmp/latex-$$
cp confirmation.tex ./tmp/latex-$$
/usr/bin/qrencode -o ./tmp/latex-$$/qrcode.png -s20 "$validationcode"
echo '\renewcommand{\firstname}{'"$firstname"'}' >> ./tmp/latex-$$/datafile.tex
echo '\renewcommand{\lastname}{'"$lastname"'}' >> ./tmp/latex-$$/datafile.tex
echo '\renewcommand{\birthdate}{'"$birthdate"'}' >> ./tmp/latex-$$/datafile.tex
echo '\renewcommand{\street}{'"$street"'}' >> ./tmp/latex-$$/datafile.tex
echo '\renewcommand{\city}{'"$city"'}' >> ./tmp/latex-$$/datafile.tex
echo '\renewcommand{\postalcode}{'"$postalcode"'}' >> ./tmp/latex-$$/datafile.tex
echo '\renewcommand{\starttime}{'"$starttime"'}' >> ./tmp/latex-$$/datafile.tex
echo '\renewcommand{\vaccine}{'"$vaccine"'}' >> ./tmp/latex-$$/datafile.tex
echo '\renewcommand{\vaccinationcenter}{'"$vaccinationcenter"'}' >> ./tmp/latex-$$/datafile.tex
cd ./tmp/latex-$$
latexmk -no-shell-escape confirmation.tex >/dev/null && cat confirmation.pdf && rm -r ../latex-$$
