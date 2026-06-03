@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."

for /d %%J in ("%SCRIPT_DIR%jdk-21\jdk-*") do set "JAVA_HOME=%%~fJ"

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo Could not find project-local JDK 21 under "%SCRIPT_DIR%jdk-21".
  exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"

set "GRADLE_PROXY_OPTS=-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7897 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7897 -Djava.net.preferIPv4Stack=true"
if defined GRADLE_OPTS (
  set "GRADLE_OPTS=%GRADLE_OPTS% %GRADLE_PROXY_OPTS%"
) else (
  set "GRADLE_OPTS=%GRADLE_PROXY_OPTS%"
)

call "%PROJECT_ROOT%\gradlew.bat" %*
