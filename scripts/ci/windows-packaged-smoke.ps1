# Installs the freshly built MSI under desktop/build/compose/binaries and runs packaged JVM/GUI smoke.
# Used by ci.yml and release.yml on windows-latest.
$ErrorActionPreference = 'Stop'

$accessProps = Join-Path $env:USERPROFILE '.accessibility.properties'
Set-Content -Path $accessProps -Value 'assistive_technologies=com.sun.java.accessibility.AccessBridge' -Encoding Ascii

$msi = Get-ChildItem -Path desktop/build/compose/binaries -Recurse -Filter '*.msi' | Select-Object -First 1
if (-not $msi) { throw 'MSI not found under desktop/build/compose/binaries' }
Write-Host "Installing $($msi.FullName)"
$log = Join-Path $env:RUNNER_TEMP 'dataweave-msi-install.log'
$proc = Start-Process msiexec.exe -ArgumentList @('/i', $msi.FullName, '/qn', '/norestart', '/L*v', $log) -Wait -PassThru
if ($proc.ExitCode -ne 0) {
  Get-Content $log -Tail 80
  throw "msiexec failed with exit code $($proc.ExitCode)"
}

$installRoot = Join-Path ${env:ProgramFiles} 'DataWeave'
$exe = Join-Path $installRoot 'DataWeave.exe'
$jvmDll = Join-Path $installRoot 'runtime/bin/server/jvm.dll'
$cfg = Join-Path $installRoot 'app/DataWeave.cfg'
$javaExe = Join-Path $installRoot 'runtime/bin/java.exe'
foreach ($path in @($exe, $jvmDll, $cfg, $javaExe)) {
  if (-not (Test-Path $path)) { throw "Missing expected install artifact: $path" }
}

$cfgText = Get-Content $cfg -Raw
if ($cfgText -notmatch 'DataWeaveBootstrapKt') {
  throw 'DataWeave.cfg app.mainclass is not com.dataweave.app.DataWeaveBootstrapKt'
}
if ($cfgText -match 'app\.version=([^\r\n]+)') {
  Write-Host "Packaged app.version=$($Matches[1])"
}

$expectedJpackage = $env:DATAWEAVE_EXPECT_JPACKAGE_VERSION
if ($expectedJpackage) {
  $gradleOut = & ./gradlew printProductVersion --no-daemon -q 2>&1 | Out-String
  if ($gradleOut -notmatch 'dataweave\.jpackageVersion=([^\r\n]+)') {
    throw 'Could not read jpackage version from printProductVersion'
  }
  $built = $Matches[1].Trim()
  if ($built -ne $expectedJpackage) {
    throw "Built jpackage version '$built' != expected '$expectedJpackage'"
  }
  if ($msi.Name -notlike "*$expectedJpackage*") {
    throw "MSI name '$($msi.Name)' does not include expected jpackage version '$expectedJpackage'"
  }
}

$release = Join-Path $installRoot 'runtime/release'
$releaseText = Get-Content $release -Raw
$requiredModules = @(
  'java.base', 'java.desktop', 'java.sql', 'java.xml', 'java.naming',
  'java.net.http', 'java.prefs', 'jdk.accessibility', 'jdk.crypto.ec', 'jdk.unsupported'
)
foreach ($mod in $requiredModules) {
  if ($releaseText -notmatch [regex]::Escape($mod)) {
    throw "Bundled runtime missing required module: $mod"
  }
}

function Assert-LaunchLogsHealthy {
  param([string]$Label)
  $errLog = Join-Path $env:LOCALAPPDATA 'DataWeave/launch-error.log'
  if (Test-Path $errLog) {
    Write-Host "[$Label] launch-error.log tail:"
    Get-Content $errLog -Tail 40 | Write-Host
    throw "[$Label] launch-error.log present after start attempt"
  }
}

function Test-NormalGuiAlive {
  param(
    [string]$Label,
    [string]$WorkingDirectory = $installRoot,
    [int]$Seconds = 15
  )
  Write-Host "[$Label] Starting DataWeave.exe with no args (cwd=$WorkingDirectory), wait ${Seconds}s"
  $p = Start-Process -FilePath $exe -WorkingDirectory $WorkingDirectory -PassThru
  Start-Sleep -Seconds $Seconds
  if ($p.HasExited) {
    Assert-LaunchLogsHealthy -Label $Label
    throw "[$Label] Normal GUI launch exited within ${Seconds}s (exit=$($p.ExitCode))"
  }
  Write-Host "[$Label] Process still alive after ${Seconds}s (pid=$($p.Id))"
  $launchLog = Join-Path $env:LOCALAPPDATA 'DataWeave/launch.log'
  if (Test-Path $launchLog) {
    Write-Host "[$Label] launch.log tail:"
    Get-Content $launchLog -Tail 15 | Write-Host
    if (-not (Select-String -Path $launchLog -Pattern 'starting Compose application' -Quiet)) {
      throw "[$Label] launch.log missing 'starting Compose application' (GUI path may have failed before Compose)"
    }
  } else {
    throw "[$Label] launch.log missing after normal GUI alive check"
  }
  Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue
}

function Invoke-DataWeaveConsoleSmoke {
  param(
    [string[]]$Arguments,
    [string]$Label,
    [string]$ExpectedPattern,
    [string]$WorkingDirectory = $installRoot,
    [int]$WaitSeconds = 300
  )
  $outFile = Join-Path $env:RUNNER_TEMP "dataweave-$Label.out"
  $errFile = Join-Path $env:RUNNER_TEMP "dataweave-$Label.err"
  if (Test-Path $outFile) { Remove-Item $outFile -Force }
  if (Test-Path $errFile) { Remove-Item $errFile -Force }
  $p = Start-Process -FilePath $exe -WorkingDirectory $WorkingDirectory -ArgumentList $Arguments `
    -RedirectStandardOutput $outFile -RedirectStandardError $errFile -PassThru -NoNewWindow
  $deadline = (Get-Date).AddSeconds($WaitSeconds)
  $matched = $false
  while ((Get-Date) -lt $deadline) {
    $stdout = Get-Content $outFile -Raw -ErrorAction SilentlyContinue
    $stderr = Get-Content $errFile -Raw -ErrorAction SilentlyContinue
    if ($stdout -match 'Failed to launch JVM' -or $stderr -match 'Failed to launch JVM') {
      if (-not $p.HasExited) { Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue }
      throw "[$Label] Packaged exe reported Failed to launch JVM"
    }
    if ($ExpectedPattern -and $stdout -match $ExpectedPattern) {
      $matched = $true
      break
    }
    if ($p.HasExited) { break }
    Start-Sleep -Seconds 2
  }
  if (-not $p.HasExited) {
    Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue
    $null = $p.WaitForExit(5000)
  }
  $stdout = Get-Content $outFile -Raw -ErrorAction SilentlyContinue
  $stderr = Get-Content $errFile -Raw -ErrorAction SilentlyContinue
  Write-Host "[$Label] exit=$($p.ExitCode) cwd=$WorkingDirectory matched=$matched"
  if ($stdout) { Write-Host $stdout }
  if ($stderr) { Write-Host $stderr }
  if ($p.ExitCode -ne 0 -and -not $matched) { throw "[$Label] DataWeave.exe exited $($p.ExitCode)" }
  if ($ExpectedPattern -and -not $matched) {
    throw "[$Label] Expected pattern '$ExpectedPattern' in stdout within ${WaitSeconds}s"
  }
}

function Invoke-DataWeaveGuiSmoke {
  param(
    [string]$Label,
    [string]$WorkingDirectory = $installRoot,
    [int]$WaitSeconds = 180
  )
  $launchLog = Join-Path $env:LOCALAPPDATA 'DataWeave/launch.log'
  Write-Host "[$Label] gui-smoke cwd=$WorkingDirectory (poll launch.log)"
  $p = Start-Process -FilePath $exe -WorkingDirectory $WorkingDirectory -ArgumentList @('--gui-smoke') -PassThru
  $deadline = (Get-Date).AddSeconds($WaitSeconds)
  $matched = $false
  while ((Get-Date) -lt $deadline) {
    if ($p.HasExited -and $p.ExitCode -ne 0) { break }
    if ((Test-Path $launchLog) -and (Select-String -Path $launchLog -Pattern 'GUI_SMOKE_OK' -Quiet)) {
      $matched = $true
      break
    }
    Start-Sleep -Seconds 2
  }
  if (-not $p.HasExited) {
    Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue
    $null = $p.WaitForExit(5000)
  }
  if (Test-Path $launchLog) {
    Write-Host "[$Label] launch.log tail:"
    Get-Content $launchLog -Tail 20 | Write-Host
  }
  Assert-LaunchLogsHealthy -Label $Label
  if (-not $matched) {
    throw "[$Label] GUI_SMOKE_OK not seen in launch.log within ${WaitSeconds}s (exit=$($p.ExitCode))"
  }
  Write-Host "[$Label] GUI smoke OK"
}

Invoke-DataWeaveConsoleSmoke -Arguments @('--diagnose') -Label 'diagnose' -ExpectedPattern '=== end diagnostics ==='
$diagOut = Get-Content (Join-Path $env:RUNNER_TEMP 'dataweave-diagnose.out') -Raw
if ($diagOut -notmatch 'install\.runtime\.missing_required_modules=\(none\)') {
  throw 'Packaged --diagnose reports missing required jlink modules'
}
if ($diagOut -notmatch 'java\.awt\.headless=') {
  throw 'Packaged --diagnose missing java.awt.headless field'
}

$jvmDebug = Join-Path $installRoot 'app/resources/DataWeave-JvmDebug.cmd'
if (-not (Test-Path $jvmDebug)) { throw "Missing JVM debug launcher: $jvmDebug" }
Write-Host 'Running DataWeave-JvmDebug.cmd --diagnose (bypass native launcher)'
$debugProc = Start-Process -FilePath 'cmd.exe' -ArgumentList @('/c', "`"$jvmDebug`"", '--diagnose') `
  -WorkingDirectory $installRoot -Wait -PassThru -NoNewWindow
if ($debugProc.ExitCode -ne 0) { throw "DataWeave-JvmDebug.cmd --diagnose failed exit=$($debugProc.ExitCode)" }
$debugLog = Join-Path $env:LOCALAPPDATA 'DataWeave/jvm-debug.log'
if (-not (Test-Path $debugLog)) { throw 'jvm-debug.log not written by DataWeave-JvmDebug.cmd' }
$debugText = Get-Content $debugLog -Raw
if ($debugText -notmatch '=== end diagnostics ===') {
  throw 'JVM debug launcher did not run packaged --diagnose successfully'
}
Invoke-DataWeaveConsoleSmoke -Arguments @('--headless-smoke') -Label 'headless-smoke' -ExpectedPattern 'HEADLESS_SMOKE_OK' -WaitSeconds 420
Invoke-DataWeaveGuiSmoke -Label 'gui-smoke-access-bridge-on'
Invoke-DataWeaveGuiSmoke -Label 'gui-smoke-bad-cwd' -WorkingDirectory $env:USERPROFILE

Remove-Item $accessProps -Force -ErrorAction SilentlyContinue
Write-Host 'Accessibility properties removed; re-testing GUI smoke without Access Bridge'
Invoke-DataWeaveGuiSmoke -Label 'gui-smoke-access-bridge-off'

Test-NormalGuiAlive -Label 'gui-normal-install-root' -WorkingDirectory $installRoot
Test-NormalGuiAlive -Label 'gui-normal-bad-cwd' -WorkingDirectory $env:USERPROFILE

$startMenuLnk = Join-Path $env:ProgramData 'Microsoft\Windows\Start Menu\Programs/DataWeave/DataWeave.lnk'
if (Test-Path $startMenuLnk) {
  $wsh = New-Object -ComObject WScript.Shell
  $sc = $wsh.CreateShortcut($startMenuLnk)
  Write-Host "Start Menu shortcut target=$($sc.TargetPath) workingDirectory=$($sc.WorkingDirectory)"
  if ($sc.WorkingDirectory) {
    Test-NormalGuiAlive -Label 'gui-normal-start-menu-cwd' -WorkingDirectory $sc.WorkingDirectory
  }
} else {
  Write-Host 'Start Menu shortcut not found; skipping shortcut CWD launch test'
}

$repoRoot = if ($env:GITHUB_WORKSPACE) { $env:GITHUB_WORKSPACE } else { (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path }
$diagScript = Join-Path $repoRoot 'scripts/diagnostics/DataWeave-Windows-Diagnostics.ps1'
if (-not (Test-Path $diagScript)) { throw "Missing diagnostics script: $diagScript" }
& $diagScript -InstallRoot $installRoot -RunPackagedDiagnose

Write-Host 'Windows packaged smoke completed OK'
