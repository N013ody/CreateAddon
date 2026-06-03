Option Explicit

Dim shell
Dim fso
Dim scriptDir
Dim projectDir
Dim launcher

Set shell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
scriptDir = fso.GetParentFolderName(WScript.ScriptFullName)
projectDir = fso.GetParentFolderName(scriptDir)
launcher = projectDir & "\.codex-tools\mcmod-run-client.cmd"

shell.CurrentDirectory = projectDir
shell.Run """" & launcher & """", 0, False
