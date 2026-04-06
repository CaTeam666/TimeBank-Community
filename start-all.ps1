param(
    [switch]$InstallDeps
)

$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendScript = Join-Path $root 'start-backend.ps1'
$familyScript = Join-Path $root 'start-family.ps1'
$adminScript = Join-Path $root 'start-admin.ps1'

foreach ($scriptPath in @($backendScript, $familyScript, $adminScript)) {
    if (-not (Test-Path -LiteralPath $scriptPath)) {
        throw "缺少启动脚本：$scriptPath"
    }
}

function Start-PwshWindow {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ScriptPath,
        [switch]$InstallDeps
    )

    $quotedScript = '"' + $ScriptPath + '"'
    $args = "-NoExit -ExecutionPolicy Bypass -File $quotedScript"
    if ($InstallDeps) {
        $args += ' -InstallDeps'
    }

    Start-Process powershell.exe -ArgumentList $args | Out-Null
}

Start-PwshWindow -ScriptPath $backendScript
Start-Sleep -Seconds 2
Start-PwshWindow -ScriptPath $familyScript -InstallDeps:$InstallDeps
Start-PwshWindow -ScriptPath $adminScript -InstallDeps:$InstallDeps

Write-Host 'Started 3 windows.' -ForegroundColor Green
Write-Host '1. Backend API -> http://localhost:8080'
Write-Host '2. Family Web -> http://localhost:3000'
Write-Host '3. Admin Web -> http://localhost:3002'
