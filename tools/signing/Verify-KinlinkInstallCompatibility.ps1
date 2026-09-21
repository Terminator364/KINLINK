param(
    [Parameter(Mandatory = $true)]
    [string]$CandidateApk,

    [Parameter(Mandatory = $true)]
    [int]$InstalledVersionCode,

    [string]$ExpectedPackage = "com.terminator364.kinlink",

    [ValidatePattern("^[0-9a-fA-F]{64}$")]
    [string]$ExpectedSignerSha256 = "2a22808df1de43eb87daa4cc37f3146e23c8b496d7f8fc5c3b314073539558b3",

    [ValidatePattern("^[0-9a-fA-F]{64}$")]
    [string]$ExpectedApkSha256
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $CandidateApk -PathType Leaf)) {
    throw "Candidate APK not found: $CandidateApk"
}

$androidHome = $env:ANDROID_HOME
if ([string]::IsNullOrWhiteSpace($androidHome)) { $androidHome = $env:ANDROID_SDK_ROOT }
if ([string]::IsNullOrWhiteSpace($androidHome)) {
    throw "ANDROID_HOME or ANDROID_SDK_ROOT must point to the Android SDK."
}

$buildTools = Get-ChildItem -LiteralPath (Join-Path $androidHome "build-tools") -Directory |
    Sort-Object Name -Descending |
    Select-Object -First 1
if ($null -eq $buildTools) { throw "Android build-tools not found." }

$aapt = Join-Path $buildTools.FullName "aapt.exe"
$apksigner = Join-Path $buildTools.FullName "apksigner.bat"
if (-not (Test-Path $aapt)) { throw "aapt not found: $aapt" }
if (-not (Test-Path $apksigner)) { throw "apksigner not found: $apksigner" }

$badging = (& $aapt dump badging $CandidateApk | Select-Object -First 1)
if ($LASTEXITCODE -ne 0) { throw "aapt badging failed." }

$pkg = [regex]::Match($badging, "name='([^']+)'").Groups[1].Value
$versionCodeText = [regex]::Match($badging, "versionCode='([0-9]+)'").Groups[1].Value
$versionName = [regex]::Match($badging, "versionName='([^']*)'").Groups[1].Value
if ([string]::IsNullOrWhiteSpace($versionCodeText)) { throw "Could not parse versionCode." }
$versionCode = [int]$versionCodeText

if ($pkg -ne $ExpectedPackage) {
    throw "Package mismatch. Expected $ExpectedPackage, got $pkg"
}
if ($versionCode -le $InstalledVersionCode) {
    throw "Unsafe in-place update: candidate versionCode $versionCode is not greater than installed $InstalledVersionCode."
}

$verify = & $apksigner verify --verbose --print-certs $CandidateApk 2>&1
if ($LASTEXITCODE -ne 0) { throw "APK signature verification failed." }
$verifyText = $verify -join [Environment]::NewLine
$m = [regex]::Match($verifyText, "certificate SHA-256 digest:\s*([0-9a-fA-F:]{64,95})")
if (-not $m.Success) { throw "Could not parse signer SHA-256." }
$signer = $m.Groups[1].Value.Replace(":", "").ToLowerInvariant()
if ($signer -ne $ExpectedSignerSha256.ToLowerInvariant()) {
    throw "Signer mismatch. Expected $ExpectedSignerSha256, got $signer"
}

$apkHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $CandidateApk).Hash.ToLowerInvariant()
if (-not [string]::IsNullOrWhiteSpace($ExpectedApkSha256) -and
    $apkHash -ne $ExpectedApkSha256.ToLowerInvariant()) {
    throw "APK SHA-256 mismatch. Expected $ExpectedApkSha256, got $apkHash"
}

Write-Host "KINLINK install-compatibility PASS"
Write-Host "Package:      $pkg"
Write-Host "VersionCode:  $versionCode > installed $InstalledVersionCode"
Write-Host "VersionName:  $versionName"
Write-Host "Signer SHA:   $signer"
Write-Host "APK SHA-256:  $apkHash"
