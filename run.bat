@echo off
title Smart Hostel Management System (CLI)
cd /d "%~dp0"

echo ========================================================
echo       SMART HOSTEL MANAGEMENT SYSTEM (CLI)
echo ========================================================
echo.

set JAVA_BIN=C:\Users\saisr\.jdks\openjdk-26.0.1\bin\java.exe

if exist "%JAVA_BIN%" (
    "%JAVA_BIN%" --enable-native-access=ALL-UNNAMED -cp "bin;lib/*" com.hostel.Main
) else (
    java -cp "bin;lib/*" com.hostel.Main
)

pause
