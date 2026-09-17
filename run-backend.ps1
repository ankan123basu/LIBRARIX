# LIBRARIX Backend Quick Start Script
$env:JAVA_HOME="D:\java21"
$env:Path="D:\java21\bin;" + $env:Path
Set-Location -Path "$PSScriptRoot\backend"
mvn spring-boot:run
