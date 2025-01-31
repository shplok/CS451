@echo off

REM Wrapper script for running iota.Main program.

set BASE_DIR=%~dp0
set j="%BASE_DIR%\..\..\"
set JAVA=java
set CPATH="%BASE_DIR%\..\lib\iota.jar"
if "%CLASSPATH%" == "" goto runApp
set CPATH=%CPATH%;"%CLASSPATH%"

:runApp
%JAVA% -classpath %CPATH% iota.Main "iota" %*

set JAVA=
set BASE_DIR=
set CPATH=
