
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.\

echo %cd% 

CALL groovy %DIRNAME%%1.groovy %DIRNAME% %*