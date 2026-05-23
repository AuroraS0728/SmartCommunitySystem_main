$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$outDir = Join-Path $root "visio-diagrams"
$importDir = Join-Path $outDir "visio-import"
$manifestPath = Join-Path $importDir "manifest.json"

if (-not (Test-Path $manifestPath)) {
    throw "Missing manifest: $manifestPath"
}

$items = Get-Content -Raw -Path $manifestPath | ConvertFrom-Json

$visio = $null

try {
    $visio = New-Object -ComObject Visio.Application
    $visio.Visible = $false

    foreach ($item in $items) {
        $src = [string]$item.src
        $title = [string]$item.title
        $dst = Join-Path $outDir ($title + ".vsdx")

        if (Test-Path $dst) {
            Remove-Item $dst -Force
        }

        $doc = $null
        try {
            $doc = $visio.Documents.Open($src)
            $doc.SaveAs($dst)
            Write-Output $dst
        }
        finally {
            if ($doc -ne $null) {
                try { $doc.Close() } catch {}
            }
        }
    }
}
finally {
    if ($visio -ne $null) {
        try { $visio.Quit() } catch {}
        try { [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($visio) } catch {}
    }
    [GC]::Collect()
    [GC]::WaitForPendingFinalizers()
}
