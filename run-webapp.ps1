param(
    [string]$DbUrl = "jdbc:mysql://localhost:3306/erp_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
    [string]$DbUser = "root",
    [string]$DbPassword = "",
    [int]$Port = 8081
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$mvn = Join-Path $root ".tools\apache-maven-3.9.9\bin\mvn.cmd"
$webapp = Join-Path $root "erp-system\webapp"

if (!(Test-Path $mvn)) {
    Write-Host "Maven not found at $mvn"
    exit 1
}

if ($DbPassword -eq "") {
    $secure = Read-Host -Prompt "Enter MySQL password for '$DbUser'" -AsSecureString
    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try {
        $DbPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
    }
}

if ([string]::IsNullOrWhiteSpace($DbPassword)) {
    Write-Host "MySQL password cannot be empty."
    exit 1
}

Set-Location $webapp
& $mvn -DskipTests package
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed."
    exit $LASTEXITCODE
}

$jar = Join-Path $webapp "target\erp-webapp-1.0.0.jar"
if (!(Test-Path $jar)) {
    Write-Host "Jar not found: $jar"
    exit 1
}

Write-Host "Starting web app on port $Port using DB user '$DbUser'"
& java `
  "-Dserver.port=$Port" `
  -jar $jar `
  "--spring.datasource.url=$DbUrl" `
  "--spring.datasource.username=$DbUser" `
  "--spring.datasource.password=$DbPassword"
