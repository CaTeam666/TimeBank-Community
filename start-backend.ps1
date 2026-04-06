param()

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
. (Join-Path $root 'env-loader.ps1')

$rootEnv = Join-Path $root '.env.local'
$backendDir = Join-Path $root 'ai-end'

$Host.UI.RawUI.WindowTitle = 'TimeBank Backend API :8080'
Set-Location -LiteralPath $backendDir
Import-EnvFile -Path $rootEnv

Write-Host 'Running: mvn spring-boot:run' -ForegroundColor Cyan
mvn spring-boot:run
if ($LASTEXITCODE -ne 0) {
    Write-Host 'Backend start failed.' -ForegroundColor Red
}
