param(
    [Parameter(Mandatory = $true)]
    [string]$InputApk,

    [Parameter(Mandatory = $true)]
    [string]$OutputApk,

    [Parameter(Mandatory = $true)]
    [string]$KeystorePath,

    [string]$KeyAlias = "kinlink-update",

    [Parameter(Mandatory = $true)]
    [ValidatePattern("^[0-9a-fA-F]{64}$")]
    [string]$ExpectedInputSha256,

    [ValidatePattern("^[0-9a-fA-F]{64}$")]
    [string]$ExpectedSignerSha256 = "2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3"
)

$ErrorActionPreference = "Stop"

function Require-EnvironmentVariable([string]$Name) {
    $value = [Environment]::GetEnvironmentVariable($Name)
    if ([string]::IsNullOrWhiteSpace($value)) {
        throw "Required environment variable '$Name' is not set."
    }
}

Require-EnvironmentVariable "KINLINK_KEYSTORE_PASSWORD"
Require-EnvironmentVariable "KINLINK_KEY_PASSWORD"

if (-not (Test-Path -LiteralPath $InputApk -PathType Leaf)) {
    throw "Input APK not found: $InputApk"
}
if (-not (Test-Path -LiteralPath $KeystorePath -PathType Leaf)) {
    throw "Keystore not found: $KeystorePath"
}
if (Test-Path -LiteralPath $OutputApk) {
    throw "Refusing to overwrite existing output APK: $OutputApk"
}

$inputHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $InputApk).Hash.ToLowerInvariant()
if ($inputHash -ne $ExpectedInputSha256.ToLowerInvariant()) {
    throw "Input APK SHA-256 mismatch. Expected $ExpectedInputSha256, got $inputHash"
}

$androidHome = $env:ANDROID_HOME
if ([string]::IsNullOrWhiteSpace($androidHome)) {
    $androidHome = $env:ANDROID_SDK_ROOT
}
if ([string]::IsNullOrWhiteSpace($androidHome)) {
    throw "ANDROID_HOME or ANDROID_SDK_ROOT must point to the Android SDK."
}

$buildToolsRoot = Join-Path $androidHome "build-tools"
$buildTools = Get-ChildItem -LiteralPath $buildToolsRoot -Directory |
    Sort-Object -Property Name -Descending |
    Select-Object -First 1
if ($null -eq $buildTools) {
    throw "No Android build-tools directory found under $buildToolsRoot"
}

$apksigner = Join-Path $buildTools.FullName "apksigner.bat"
$zipalign = Join-Path $buildTools.FullName "zipalign.exe"
if (-not (Test-Path -LiteralPath $apksigner)) {
    throw "apksigner not found: $apksigner"
}
if (-not (Test-Path -LiteralPath $zipalign)) {
    throw "zipalign not found: $zipalign"
}

& $zipalign -c -P 16 4 $InputApk
if ($LASTEXITCODE -ne 0) {
    throw "Candidate APK is not zip-aligned. Refusing to mutate the CI artifact during signing."
}

$signArgs = @(
    "sign",
    "--ks", $KeystorePath,
    "--ks-key-alias", $KeyAlias,
    "--ks-pass", "env:KINLINK_KEYSTORE_PASSWORD",
    "--key-pass", "env:KINLINK_KEY_PASSWORD",
    "--out", $OutputApk,
    $InputApk
)
& $apksigner @signArgs
if ($LASTEXITCODE -ne 0) {
    throw "apksigner sign failed."
}

$verifyLines = & $apksigner verify --verbose --print-certs $OutputApk 2>&1
if ($LASTEXITCODE -ne 0) {
    throw "Signed APK verification failed."
}
$verifyText = ($verifyLines -join [Environment]::NewLine)
$match = [regex]::Match(
    $verifyText,
    "Signer #1 certificate SHA-256 digest:\s*([0-9a-fA-F:]{64,95})"
)
if (-not $match.Success) {
    throw "Could not read signer SHA-256 from apksigner output."
}

$actualSigner = $match.Groups[1].Value.Replace(":", "").ToLowerInvariant()
$expectedSigner = $ExpectedSignerSha256.ToLowerInvariant()
if ($actualSigner -ne $expectedSigner) {
    Remove-Item -LiteralPath $OutputApk -Force -ErrorAction SilentlyContinue
    throw "Signer mismatch. Expected $expectedSigner, got $actualSigner"
}

$outputHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $OutputApk).Hash.ToLowerInvariant()
$receiptPath = "$OutputApk.signing-receipt.json"
$receipt = [ordered]@{
    product = "KINLINK"
    input_apk = [IO.Path]::GetFileName($InputApk)
    input_sha256 = $inputHash
    output_apk = [IO.Path]::GetFileName($OutputApk)
    output_sha256 = $outputHash
    signer_cert_sha256 = $actualSigner
    verified = $true
    generated_at_utc = [DateTime]::UtcNow.ToString("o")
}
$receipt | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath $receiptPath -Encoding UTF8

Write-Host "KINLINK stable signing PASS"
Write-Host "Input SHA-256:  $inputHash"
Write-Host "Output SHA-256: $outputHash"
Write-Host "Signer SHA-256: $actualSigner"
Write-Host "Receipt: $receiptPath"
