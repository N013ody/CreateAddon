@echo off
setlocal
set "BLOCKBENCH_EXE=%LOCALAPPDATA%\Programs\Blockbench\Blockbench.exe"
if not exist "%BLOCKBENCH_EXE%" (
  echo Blockbench.exe was not found at "%BLOCKBENCH_EXE%".
  exit /b 1
)
start "" "%BLOCKBENCH_EXE%" "%~dp0..\src\main\resources\assets\createaddon\blockbench"
