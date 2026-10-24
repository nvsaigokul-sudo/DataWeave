@echo off
setlocal EnableExtensions EnableDelayedExpansion
rem Bypass jpackage native launcher when it shows "Failed to launch JVM" before Java main runs.
rem Installed path: "%ProgramFiles%\DataWeave\app\resources\DataWeave-JvmDebug.cmd"

set "INSTALL_ROOT=%~dp0..\.."
for %%I in ("%INSTALL_ROOT%") do set "INSTALL_ROOT=%%~fI"
set "APP_DIR=%INSTALL_ROOT%\app"
set "JAVA_EXE=%INSTALL_ROOT%\runtime\bin\java.exe"
set "LOG_DIR=%LOCALAPPDATA%\DataWeave"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%" 2>nul
set "LOG=%LOG_DIR%\jvm-debug.log"

(
  echo === DataWeave JVM debug launcher ===
  echo timestamp=%DATE% %TIME%
  echo install.root=%INSTALL_ROOT%
  echo app.dir=%APP_DIR%
  echo java.exe=%JAVA_EXE%
  echo cwd=%CD%
  echo args=%*
)>"%LOG%"

if not exist "%JAVA_EXE%" (
  echo ERROR: missing bundled java.exe at "%JAVA_EXE%">>"%LOG%"
  echo Missing bundled Java runtime. Reinstall DataWeave from the latest MSI.
  exit /b 1
)

pushd "%INSTALL_ROOT%"
set "CP="
for %%J in ("%APP_DIR%\*.jar") do (
  if defined CP (set "CP=!CP!;%%~fJ") else set "CP=%%~fJ"
)
if not defined CP (
  echo ERROR: no jars under "%APP_DIR%">>"%LOG%"
  echo No application jars found under app\
  popd
  exit /b 1
)

set "MAIN=com.dataweave.app.DataWeaveBootstrapKt"
echo classpath.entry.count=%APP_DIR%\*.jar>>"%LOG%"
echo mainclass=%MAIN%>>"%LOG%"

"%JAVA_EXE%" ^
  -Dcompose.application.resources.dir="%APP_DIR%\resources" ^
  -Dcompose.application.configure.swing.globals=true ^
  -Dskiko.library.path="%APP_DIR%" ^
  -cp "%CP%" %MAIN% %* >>"%LOG%" 2>&1
set "ERR=!ERRORLEVEL!"
echo exit.code=!ERR!>>"%LOG%"
popd
exit /b !ERR!
