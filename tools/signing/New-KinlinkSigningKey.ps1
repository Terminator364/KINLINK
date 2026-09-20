[CmdletBinding()]
param(
    [string]$Destination = "$env:USERPROFILE\Documents\KINLINK-private",
    [string]$Alias = "kinlink-release"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$keytool = (Get-Command keytool.exe -ErrorAction SilentlyContinue).Source
if (-not $keytool -and $env:JAVA_HOME) {
    $candidate = Join-Path $env:JAVA_HOME "bin\keytool.exe"
    if (Test-Path $candidate) { $keytool = $candidate }
}
if (-not $keytool) {
    throw "keytool.exe is required. Install a JDK or Android Studio, then run this script again."
}

New-Item -ItemType Directory -Force -Path $Destination | Out-Null
$keystore = Join-Path $Destination "kinlink-release.jks"
if (Test-Path $keystore) {
    throw "Refusing to overwrite an existing KINLINK key: $keystore"
}

$storeSecure = Read-Host "Choose a keystore password" -AsSecureString
$keySecure = Read-Host "Choose a key password" -AsSecureString

function Convert-SecureStringToPlainText([Security.SecureString]$Value) {
    $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($Value)
    try { return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer) }
}

$storePassword = Convert-SecureStringToPlainText $storeSecure
$keyPassword = Convert-SecureStringToPlainText $keySecure
try {
    $arguments = @(
        "-genkeypair", "-v",
        "-keystore", $keystore,
        "-storetype", "JKS",
        "-storepass", $storePassword,
        "-alias", $Alias,
        "-keypass", $keyPassword,
        "-keyalg", "RSA",
        "-keysize", "4096",
        "-validity", "9125",
        "-dname", "CN=KINLINK, OU=Offline Release Signing, O=KINLINK, L=Kinshasa, C=CD"
    )
    & $keytool @arguments

    & $keytool "-list" "-v" "-keystore" $keystore "-storepass" $storePassword "-alias" $Alias |
        Select-String "SHA256:"
}
finally {
    $storePassword = $null
    $keyPassword = $null
}

Write-Host ""
Write-Host "KINLINK signing key created locally:"
Write-Host $keystore
Write-Host "Keep one offline backup. Never put this file or its passwords in GitHub or public Drive."
