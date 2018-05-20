@echo off

if "%~1"=="" (
SET filename="note"
) else (
SET filename=%1
)

echo Filename without extension is: %filename%
pdflatex -interaction=nonstopmode "%filename%.tex"
call clean.bat
echo Fast build completed at %DATE% %TIME%
