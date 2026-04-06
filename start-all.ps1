param(
    [switch]$InstallDeps
)

$ErrorActionPreference = 'Stop'

function Quote-Single {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Value
    )

    return "'" + $Value.Replace("'", "''") + "'"
}

function Start-ProjectWindow {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name,
        [Parameter(Mandatory = $true)]
        [string]$WorkingDirectory,
        [Parameter(Mandatory = $true)]
        [string[]]$Commands
    )

    $quotedDir = Quote-Single -Value $WorkingDirectory
    $commandText = @(
        '$Host.UI.RawUI.WindowTitle = ' + (Quote-Single -Value $Name)
        'Set-Location -LiteralPath ' + $quotedDir
    ) + $Commands

    Start-Process powershell.exe -ArgumentList @(
        '-NoExit',
        '-ExecutionPolicy', 'Bypass',
        '-Command', ($commandText -join '; ')
    ) | Out-Null
}

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

$backendDir = Join-Path $root 'ai-end'
$familyDir = Join-Path $root 'timebank-family'
$adminDir = Join-Path $root 'timebank-backend'

$requiredDirs = @($backendDir, $familyDir, $adminDir)
foreach ($dir in $requiredDirs) {
    if (-not (Test-Path -LiteralPath $dir)) {
        throw "目录不存在：$dir"
    }
}

$familyCommands = @()
if ($InstallDeps -or -not (Test-Path -LiteralPath (Join-Path $familyDir 'node_modules'))) {
    $familyCommands += 'npm install'
    $familyCommands += 'if ($LASTEXITCODE -ne 0) { Write-Host ''timebank-family 依赖安装失败'' -ForegroundColor Red; return }'
}
$familyCommands += 'npm run dev'

$adminCommands = @()
if ($InstallDeps -or -not (Test-Path -LiteralPath (Join-Path $adminDir 'node_modules'))) {
    $adminCommands += 'npm install'
    $adminCommands += 'if ($LASTEXITCODE -ne 0) { Write-Host ''timebank-backend 依赖安装失败'' -ForegroundColor Red; return }'
}
$adminCommands += 'npm run dev'

Start-ProjectWindow -Name 'TimeBank Backend API :8080' -WorkingDirectory $backendDir -Commands @(
    'mvn spring-boot:run'
)

Start-Sleep -Seconds 2

Start-ProjectWindow -Name 'TimeBank Family Web :3000' -WorkingDirectory $familyDir -Commands $familyCommands
Start-ProjectWindow -Name 'TimeBank Admin Web :3002' -WorkingDirectory $adminDir -Commands $adminCommands

Write-Host "Started 3 windows." -ForegroundColor Green
Write-Host "1. Backend API -> http://localhost:8080"
Write-Host "2. Family Web -> http://localhost:3000"
Write-Host "3. Admin Web -> http://localhost:3002"
Write-Host ""
Write-Host "Install frontend deps first if needed:"
Write-Host "./start-all.ps1 -InstallDeps"
