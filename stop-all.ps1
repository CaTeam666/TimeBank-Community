param()

$ErrorActionPreference = 'Stop'

function Stop-ProcessByPort {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port,
        [Parameter(Mandatory = $true)]
        [string]$Name
    )

    $connections = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if (-not $connections) {
        Write-Host "$Name is not running on port $Port."
        return
    }

    $pids = $connections | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($procId in $pids) {
        try {
            $process = Get-Process -Id $procId -ErrorAction Stop
            Stop-Process -Id $procId -Force -ErrorAction Stop
            Write-Host "Stopped $Name on port ${Port}: $($process.ProcessName) ($procId)"
        } catch {
            Write-Host "Failed to stop $Name on port ${Port}: process ${procId}. $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }
}

Stop-ProcessByPort -Port 8080 -Name 'Backend'
Stop-ProcessByPort -Port 3000 -Name 'Family Web'
Stop-ProcessByPort -Port 3002 -Name 'Admin Web'

Write-Host ''
Write-Host 'Stop check finished.'
