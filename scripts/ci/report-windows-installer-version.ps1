# Reports Gradle product version, jpackage version, and Windows file version metadata for packaged MSI/EXE.
$ErrorActionPreference = 'Stop'

Write-Host "Git ref: $env:GITHUB_REF_NAME @ $env:GITHUB_SHA"
git log -1 --oneline

$gradleOut = & bash -lc './gradlew printProductVersion --no-daemon 2>&1' | Out-String
Write-Host $gradleOut
$gradleVersion = if ($gradleOut -match 'dataweave\.gradleVersion=(.+)') { $Matches[1].Trim() } else { '(unknown)' }
$jpackageVersion = if ($gradleOut -match 'dataweave\.jpackageVersion=(.+)') { $Matches[1].Trim() } else { '(unknown)' }

$binRoot = Join-Path (Get-Location) 'desktop/build/compose/binaries'
$msi = Get-ChildItem -Path $binRoot -Recurse -Filter '*.msi' -ErrorAction SilentlyContinue | Select-Object -First 1
$exe = Get-ChildItem -Path $binRoot -Recurse -Filter '*.exe' -ErrorAction SilentlyContinue |
  Where-Object { $_.Name -like 'DataWeave-*.exe' } | Select-Object -First 1

if (-not $msi) { throw 'MSI not found under desktop/build/compose/binaries' }

$msiHash = (Get-FileHash -Algorithm SHA256 $msi.FullName).Hash.ToLower()
$msiFileVersion = $msi.VersionInfo.FileVersion
$msiProductVersion = $msi.VersionInfo.ProductVersion

Write-Host "MSI path: $($msi.FullName)"
Write-Host "MSI name: $($msi.Name)"
Write-Host "MSI FileVersion: $msiFileVersion"
Write-Host "MSI ProductVersion: $msiProductVersion"
Write-Host "SHA256: $msiHash"

$exeLine = ''
if ($exe) {
  $exeLine = "| EXE | ``$($exe.Name)`` (FileVersion=$($exe.VersionInfo.FileVersion)) |"
  Write-Host "EXE name: $($exe.Name)"
  Write-Host "EXE FileVersion: $($exe.VersionInfo.FileVersion)"
}

if ($jpackageVersion -ne '(unknown)' -and $msi.Name -notlike "*$jpackageVersion*") {
  throw "MSI file name '$($msi.Name)' does not contain expected jpackage version '$jpackageVersion'"
}

$summary = @"
## TEST Windows installer (not a release)

| Field | Value |
|-------|-------|
| Gradle product version | ``$gradleVersion`` |
| jpackage / installer version | ``$jpackageVersion`` |
| MSI | ``$($msi.Name)`` (FileVersion=$msiFileVersion) |
$exeLine
| SHA256 (MSI) | ``$msiHash`` |
| Ref | ``$env:GITHUB_REF_NAME`` @ ``$env:GITHUB_SHA`` |

Download artifact **TEST-windows-msi-$env:GITHUB_SHA** from this workflow run.
"@

if ($env:GITHUB_STEP_SUMMARY) {
  Add-Content -Path $env:GITHUB_STEP_SUMMARY -Value $summary
}
