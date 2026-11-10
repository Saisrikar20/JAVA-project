# Smart Hostel Management System (CLI) Run Script
Set-Location $PSScriptRoot

$javaBin = "C:\Users\saisr\.jdks\openjdk-26.0.1\bin\java.exe"

if (Test-Path $javaBin) {
    & $javaBin --enable-native-access=ALL-UNNAMED -cp "bin;lib/*" com.hostel.Main
} else {
    java -cp "bin;lib/*" com.hostel.Main
}
