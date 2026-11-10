Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "      SMART HOSTEL MANAGEMENT SYSTEM - WEB DASHBOARD" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "Starting embedded web server at http://localhost:8080 ..." -ForegroundColor Green

Start-Process "http://localhost:8080"

$javaBin = "C:\Users\saisr\.jdks\openjdk-26.0.1\bin\java.exe"
if (Test-Path $javaBin) {
    & $javaBin --enable-native-access=ALL-UNNAMED -cp "bin;lib/*" com.hostel.WebServer
} else {
    java -cp "bin;lib/*" com.hostel.WebServer
}
