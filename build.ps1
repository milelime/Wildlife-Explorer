# Cross-platform build on Windows PowerShell (needs JDK on PATH).
# Usage: .\build.ps1           (compile only)
#        .\build.ps1 run        (compile + launch GUI)
#        .\build.ps1 clean      (remove build/)
param(
    [Parameter(Position = 0)]
    [ValidateSet("build", "run", "clean")]
    [string]$Command = "build"
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Src = Join-Path $Root "src/main/java"
$Out = Join-Path $Root "build/classes"

function Invoke-Build {
    New-Item -ItemType Directory -Force -Path $Out | Out-Null
    $files = @(Get-ChildItem -Path $Src -Recurse -Filter "*.java" | ForEach-Object { $_.FullName })
    if ($files.Count -eq 0) {
        throw "No Java sources under $Src"
    }
    & javac -d $Out @files
    Write-Host "Built classes -> $Out"
}

switch ($Command) {
    "build" { Invoke-Build }
    "run" {
        Invoke-Build
        & java -cp $Out wildlifeexplorer.Main
    }
    "clean" {
        $buildDir = Join-Path $Root "build"
        if (Test-Path $buildDir) {
            Remove-Item -Recurse -Force $buildDir
            Write-Host "Removed $buildDir"
        }
    }
}
