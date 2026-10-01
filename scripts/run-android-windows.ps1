[CmdletBinding()]
param(
    [string]$ApiUrl,
    [string]$Device,
    [switch]$Logs,
    [switch]$NoBuild,
    [switch]$Clean,
    [switch]$CheckOnly
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$ApplicationId = 'id.tandara.parent'
$GradlewBat = Join-Path $ProjectRoot 'gradlew.bat'
$ApkPath = Join-Path $ProjectRoot 'app\build\outputs\apk\debug\app-debug.apk'

function Test-HttpUrl {
    param([string]$Value)
    if ([string]::IsNullOrWhiteSpace($Value)) { return $false }
    return $Value -match '^(https?://)[^\s]+/?$'
}

function Get-PreferredJavaHome {
    $candidates = @()
    if ($env:JAVA_HOME) { $candidates += $env:JAVA_HOME }

    $javaRoots = @(
        'C:\Program Files\Microsoft\jdk-17',
        'C:\Program Files\Microsoft\jdk-21',
        'C:\Program Files\Eclipse Adoptium',
        'C:\Program Files\Java',
        'C:\Program Files\OpenJDK',
        'C:\Program Files\Zulu'
    )

    foreach ($root in $javaRoots) {
        if (Test-Path $root) {
            $candidates += Get-ChildItem -Path $root -Directory -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName }
        }
    }

    foreach ($candidate in $candidates) {
        if (-not $candidate) { continue }
        $javaBin = Join-Path $candidate 'bin\java.exe'
        $javacBin = Join-Path $candidate 'bin\javac.exe'
        if ((Test-Path $javaBin) -and (Test-Path $javacBin)) {
            $versionOut = & $javaBin -version 2>&1 | Select-String 'version' | Select-Object -First 1
            if ($versionOut) {
                $versionText = $versionOut.ToString()
                if ($versionText -match 'version "(?<maj>\d+)' ) {
                    $major = [int]$Matches.maj
                    if ($major -ge 17 -and $major -le 25) {
                        return $candidate
                    }
                }
            }
        }
    }

    return $null
}

function Get-AndroidSdkRoot {
    $candidates = @()
    if ($env:ANDROID_HOME) { $candidates += $env:ANDROID_HOME }
    if ($env:ANDROID_SDK_ROOT) { $candidates += $env:ANDROID_SDK_ROOT }

    $localPropertiesPath = Join-Path $ProjectRoot 'local.properties'
    if (Test-Path $localPropertiesPath) {
        $localValue = Get-Content $localPropertiesPath -ErrorAction SilentlyContinue | Where-Object { $_ -match '^sdk\.dir=' }
        if ($localValue) {
            $sdkPath = ($localValue -split '=', 2)[1].Trim()
            if ($sdkPath) { $candidates += $sdkPath }
        }
    }

    $candidates += "$env:LOCALAPPDATA\Android\Sdk"
    $candidates += 'C:\Users\LENOVO\AppData\Local\Android\Sdk'

    foreach ($candidate in $candidates) {
        if ($candidate -and (Test-Path $candidate)) {
            return $candidate
        }
    }

    return $null
}

if ($CheckOnly) {
    if ($ApiUrl -or $Device -or $Logs -or $NoBuild -or $Clean) {
        throw 'The -CheckOnly option cannot be combined with -ApiUrl, -Device, -Logs, -NoBuild, or -Clean.'
    }
}

if (-not (Test-Path $GradlewBat)) {
    throw "Missing Gradle wrapper: $GradlewBat"
}

$JavaHome = Get-PreferredJavaHome
if (-not $JavaHome) {
    throw 'No compatible JDK 17+ was found on this Windows machine. Install Microsoft OpenJDK 17 or a supported JDK and re-run this script.'
}
$env:JAVA_HOME = $JavaHome
$env:Path = "$JavaHome\bin;$env:Path"

$AndroidSdkRoot = Get-AndroidSdkRoot
if (-not $AndroidSdkRoot) {
    throw 'Android SDK not found. Install Android Studio or the Android SDK, then re-run this script.'
}
$env:ANDROID_HOME = $AndroidSdkRoot
$env:ANDROID_SDK_ROOT = $AndroidSdkRoot
$env:Path = "$AndroidSdkRoot\platform-tools;$env:Path"

$AdbPath = Join-Path $AndroidSdkRoot 'platform-tools\adb.exe'
if (-not (Test-Path $AdbPath)) {
    throw "ADB not found under $AndroidSdkRoot\platform-tools. Install Android Platform Tools and retry."
}

$platformDir = Join-Path $AndroidSdkRoot 'platforms'
$platformCandidates = @('android-36.1', 'android-36', 'android-35')
$platformFound = $false
foreach ($candidate in $platformCandidates) {
    if (Test-Path (Join-Path $platformDir $candidate)) {
        $platformFound = $true
        break
    }
}
if (-not $platformFound) {
    throw "Required Android platform is missing under $platformDir. Install SDK platform android-36 (or android-36.1) before building."
}

$buildToolsDir = Join-Path $AndroidSdkRoot 'build-tools'
if (-not (Test-Path $buildToolsDir)) {
    throw "Android build-tools directory is missing under $AndroidSdkRoot. Install the required Android SDK build-tools."
}
$latestBuildTools = Get-ChildItem -Path $buildToolsDir -Directory -ErrorAction SilentlyContinue | Sort-Object Name | Select-Object -Last 1
if (-not $latestBuildTools) {
    throw "No Android build-tools were found. Install the required SDK build-tools package."
}

if ($CheckOnly) {
    Write-Host 'Windows Android environment: PASS'
    Write-Host "JAVA_HOME=$env:JAVA_HOME"
    Write-Host "ANDROID_HOME=$env:ANDROID_HOME"
    Write-Host "ADB=$AdbPath"
    Write-Host "BUILD_TOOLS=$($latestBuildTools.FullName)"
    exit 0
}

if ($ApiUrl) {
    if (-not (Test-HttpUrl $ApiUrl)) {
        throw "Invalid backend URL '$ApiUrl'. Use a value like 'http://192.168.1.25:8000/' or 'https://example.com/'."
    }
}

$deviceRows = & $AdbPath devices -l 2>$null | Select-Object -Skip 1
if (-not $deviceRows) {
    throw 'No Android device is connected. Connect the phone by USB, enable USB debugging, and authorize the device before retrying.'
}

$selectedDevice = $null
if ($Device) {
    $selectedDevice = $deviceRows | Where-Object { $_ -match "^$([regex]::Escape($Device))\s" } | Select-Object -First 1
    if (-not $selectedDevice) {
        throw "Requested device '$Device' was not found in adb devices -l."
    }
} else {
    $deviceCount = ($deviceRows | Where-Object { $_ -match '^\S+\s+\S+' }).Count
    if ($deviceCount -ne 1) {
        throw "Multiple Android devices are connected. Re-run with -Device <serial> to select one explicitly."
    }
    $selectedDevice = $deviceRows | Select-Object -First 1
}

$serial = ($selectedDevice -split '\s+', 3)[0]
$state = ($selectedDevice -split '\s+', 3)[1]

switch ($state) {
    'unauthorized' { throw "Device $serial is unauthorized. Approve USB debugging on the phone, then retry." }
    'offline' { throw "Device $serial is offline. Reconnect the phone or restart USB debugging, then retry." }
    'device' { }
    default { throw "Device $serial is not ready (ADB state: $state)." }
}

if (-not $NoBuild) {
    if ($Clean) {
        Write-Host 'Running Gradle clean...'
        & $GradlewBat clean --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'Gradle clean failed.' }
    }

    $buildArgs = @('assembleDebug', '--console=plain')
    if ($ApiUrl) {
        $buildArgs += "-PTANDARA_API_BASE_URL=$ApiUrl"
    }

    Write-Host "Building debug APK for $ApplicationId..."
    & $GradlewBat @buildArgs
    if ($LASTEXITCODE -ne 0) { throw 'Gradle debug build failed.' }
}

if (-not (Test-Path $ApkPath)) {
    throw "Debug APK was not generated at $ApkPath"
}

$installArgs = @('-s', $serial, 'install', '-r', $ApkPath)
Write-Host "Installing $ApkPath to $serial..."
& $AdbPath @installArgs
if ($LASTEXITCODE -ne 0) { throw 'ADB install failed.' }

$component = "$ApplicationId/$ApplicationId.MainActivity"
Write-Host "Launching $component..."
& $AdbPath -s $serial shell am start -W -n $component
if ($LASTEXITCODE -ne 0) { throw 'App launch failed.' }

if ($Logs) {
    Write-Host 'Recent logcat output:'
    & $AdbPath -s $serial logcat -d -t 200
}

Write-Host 'Android launcher workflow completed successfully.'
Write-Host "APK: $ApkPath"
Write-Host "Backend URL: $ApiUrl"
