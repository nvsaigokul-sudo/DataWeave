#Requires -Version 5.1
<#
.SYNOPSIS
  Collects DataWeave Windows install / JVM launch diagnostics (no secrets).

  Aligns with packaged app output from: DataWeave.exe --diagnose

.EXAMPLE
  .\DataWeave-Windows-Diagnostics.ps1
  .\DataWeave-Windows-Diagnostics.ps1 -InstallRoot "C:\Program Files\DataWeave"
#>
[CmdletBinding()]
param(
    [string]$InstallRoot = "",
    [switch]$RunPackagedDiagnose
)

$ErrorActionPreference = 'Continue'

function Write-Section([string]$Title) {
    Write-Output ""
    Write-Output "=== $Title ==="
}

function Find-DataWeaveInstall {
    param([string[]]$Roots)
    foreach ($root in $Roots) {
        if (-not $root) { continue }
        $exe = Join-Path $root 'DataWeave.exe'
        if (Test-Path $exe) {
            return (Resolve-Path $root).Path
        }
    }
    return $null
}

function Read-ReleaseModules([string]$ReleasePath) {
    if (-not (Test-Path $ReleasePath)) { return $null }
    $lines = Get-Content -Path $ReleasePath -ErrorAction SilentlyContinue
    foreach ($line in $lines) {
        if ($line -match '^MODULES="(.+)"$') { return $Matches[1] }
        if ($line -match '^MODULES=(.+)$') { return $Matches[1].Trim('"') }
    }
    return $null
}

Write-Output "=== DataWeave launch diagnostics ==="
Write-Output "timestamp=$(Get-Date -Format o)"
Write-Output "hostname=$env:COMPUTERNAME"
Write-Output "user=$env:USERNAME"
Write-Output "arch=$([Environment]::Is64BitOperatingSystem)"
Write-Output "process.arch=$([Environment]::Is64BitProcess)"
Write-Output "os=$([Environment]::OSVersion.VersionString)"

Write-Section "Candidate install roots"
$candidates = @(
    $InstallRoot,
    (Join-Path ${env:ProgramFiles} 'DataWeave'),
    (Join-Path ${env:ProgramFiles(x86)} 'DataWeave'),
    (Join-Path $env:LOCALAPPDATA 'Programs\DataWeave'),
    (Join-Path $env:LOCALAPPDATA 'DataWeave')
) | Where-Object { $_ -and $_.Trim() -ne '' } | Select-Object -Unique

$found = @()
foreach ($c in $candidates) {
    $resolved = Find-DataWeaveInstall @($c)
    $label = if ($resolved) { "FOUND" } else { "missing" }
    Write-Output "candidate=$c status=$label"
    if ($resolved) { $found += $resolved }
}
$found = $found | Select-Object -Unique

if ($found.Count -eq 0) {
    Write-Output "install.root.resolved=(none — DataWeave.exe not found)"
} else {
    if ($found.Count -gt 1) {
        Write-Output "warning=multiple installs detected; diagnostics run for each"
    }
    foreach ($root in $found) {
        Write-Section "Install layout: $root"
        $exe = Join-Path $root 'DataWeave.exe'
        $appDir = Join-Path $root 'app'
        $runtimeDir = Join-Path $root 'runtime'
        $jvmDll = Join-Path $runtimeDir 'bin\server\jvm.dll'
        $cfg = Join-Path $appDir 'DataWeave.cfg'
        $release = Join-Path $runtimeDir 'release'
        $skiko = Join-Path $appDir 'skiko-windows-x64.dll'

        Write-Output "install.root.resolved=$root"
        Write-Output "install.exe.path=$exe"
        Write-Output "install.exe.exists=$(Test-Path $exe)"
        Write-Output "install.app.dir.exists=$(Test-Path $appDir)"
        Write-Output "install.runtime.dir.exists=$(Test-Path $runtimeDir)"
        Write-Output "install.jvm.dll.path=$jvmDll"
        Write-Output "install.jvm.dll.exists=$(Test-Path $jvmDll)"
        Write-Output "install.cfg.exists=$(Test-Path $cfg)"
        Write-Output "install.skiko.dll.exists=$(Test-Path $skiko)"

        if (Test-Path $cfg) {
            $skikoLine = Select-String -Path $cfg -Pattern 'skiko.library.path' -SimpleMatch | Select-Object -First 1
            $mainLine = Select-String -Path $cfg -Pattern 'app.mainclass' -SimpleMatch | Select-Object -First 1
            Write-Output "install.cfg.skiko.library.path=$($skikoLine.Line)"
            Write-Output "install.cfg.mainclass=$($mainLine.Line)"
        }

        $modules = Read-ReleaseModules $release
        Write-Output "install.runtime.modules=$modules"
        Write-Output "install.runtime.has_jdk.accessibility=$([bool]($modules -match 'jdk.accessibility'))"

        Write-Section "Shortcut working directories"
        $shellLinks = @(
            (Join-Path $env:APPDATA 'Microsoft\Windows\Start Menu\Programs\DataWeave\DataWeave.lnk'),
            (Join-Path $env:ProgramData 'Microsoft\Windows\Start Menu\Programs\DataWeave\DataWeave.lnk'),
            (Join-Path $env:USERPROFILE 'Desktop\DataWeave.lnk')
        )
        foreach ($lnk in $shellLinks) {
            if (-not (Test-Path $lnk)) {
                Write-Output "shortcut.path=$lnk exists=false"
                continue
            }
            $wsh = New-Object -ComObject WScript.Shell
            $sc = $wsh.CreateShortcut($lnk)
            Write-Output "shortcut.path=$lnk exists=true target=$($sc.TargetPath) workingDirectory=$($sc.WorkingDirectory)"
        }

        if ($RunPackagedDiagnose -and (Test-Path $exe)) {
            Write-Section "Packaged --diagnose ($root)"
            & $exe --diagnose 2>&1 | ForEach-Object { Write-Output $_ }
        }
    }
}

Write-Section "Accessibility"
$accessProps = Join-Path $env:USERPROFILE '.accessibility.properties'
Write-Output "accessibility.user.properties.path=$accessProps"
Write-Output "accessibility.user.properties.exists=$(Test-Path $accessProps)"
if (Test-Path $accessProps) {
    $preview = (Get-Content $accessProps -ErrorAction SilentlyContinue | Select-Object -First 20) -join ' | '
    Write-Output "accessibility.user.properties.preview=$preview"
}

Write-Section "Launch logs"
$logDir = Join-Path $env:LOCALAPPDATA 'DataWeave'
$launchLog = Join-Path $logDir 'launch.log'
$errLog = Join-Path $logDir 'launch-error.log'
Write-Output "launch.log.path=$launchLog"
Write-Output "launch.log.exists=$(Test-Path $launchLog)"
if (Test-Path $launchLog) {
    Write-Output "launch.log.tail=$((Get-Content $launchLog -Tail 30) -join ' | ')"
}
Write-Output "launch.error.log.path=$errLog"
Write-Output "launch.error.log.exists=$(Test-Path $errLog)"
if (Test-Path $errLog) {
    Write-Output "launch.error.log.tail=$((Get-Content $errLog -Tail 40) -join ' | ')"
}

Write-Section "Visual C++ runtime (jvm.dll peers)"
$vcDll = Join-Path ${env:ProgramFiles} 'DataWeave\runtime\bin\msvcp140.dll'
if (Test-Path $vcDll) {
    Write-Output "bundled.msvcp140=$vcDll exists=true"
} else {
    Write-Output "bundled.msvcp140=(not found under install)"
}
$sysVC = @(
    (Join-Path $env:SystemRoot 'System32\vcruntime140.dll'),
    (Join-Path $env:SystemRoot 'System32\msvcp140.dll')
)
foreach ($p in $sysVC) {
    Write-Output "system.vc.path=$p exists=$(Test-Path $p)"
}

Write-Output "=== end diagnostics ==="
