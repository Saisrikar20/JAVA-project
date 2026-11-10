@echo off
title Smart Hostel Web Application (http://localhost:8080)
cd /d "%~dp0"

echo ========================================================
echo       SMART HOSTEL MANAGEMENT SYSTEM - WEB DASHBOARD
echo ========================================================
echo Starting embedded web server at http://localhost:8080 ...
echo.

set JAVA_BIN=C:\Users\saisr\.jdks\openjdk-26.0.1\bin\java.exe

start http://localhost:8080

if exist "%JAVA_BIN%" (
    "%JAVA_BIN%" --enable-native-access=ALL-UNNAMED -cp "bin;lib/*" com.hostel.WebServer
) else (
    java -cp "bin;lib/*" com.hostel.WebServer
)

pause
