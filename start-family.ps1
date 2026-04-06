param(
    [switch]$InstallDeps
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
. (Join-Path $root 'env-loader.ps1')

$rootEnv = Join-Path $root '.env.local'
$projectDir = Join-Path $root 'timebank-family'
$projectEnv = Join-Path $projectDir '.env.local'

$Host.UI.RawUI.WindowTitle = 'TimeBank Family Web :3000'
Set-Location -LiteralPath $projectDir
Import-EnvFile -Path $rootEnv
Import-EnvFile -Path $projectEnv

if ($InstallDeps -or -not (Test-Path -LiteralPath (Join-Path $projectDir 'node_modules'))) {
    Write-Host 'Running: npm install' -ForegroundColor Cyan
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Host 'Family install failed.' -ForegroundColor Red
        return
    }
}

Write-Host 'Running: npm run dev' -ForegroundColor Cyan
npm run dev
if ($LASTEXITCODE -ne 0) {
    Write-Host 'Family start failed.' -ForegroundColor Red
}
