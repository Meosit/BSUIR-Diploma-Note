@echo off

if "%~1"=="" (
SET filename="note"
) else (
SET filename=%1
)

echo Filename without extension is: %filename%
echo
echo Stage 1 - pdflatex
echo ...
pdflatex -interaction=nonstopmode "%filename%.tex"
echo ############################################################################
echo Stage 2 - bibtex
echo ...
bibtex "%filename%.aux"
echo ############################################################################
echo Stage 3 - pdflatex
echo ...
pdflatex -interaction=batchmode "%filename%.tex"
echo ############################################################################
echo Stage 4 - pdflatex
echo ...
pdflatex -interaction=batchmode "%filename%.tex"
echo ############################################################################
echo Stage 5 - cleanup
echo ...
call clean.bat
