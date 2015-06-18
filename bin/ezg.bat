if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.\

echo %cd%

set CLASSPATH=%EASYGSP_HOME%\lib\*;%EASYGSP_HOME%\bin\*

CALL groovy %DIRNAME%%1.groovy %DIRNAME% %*

