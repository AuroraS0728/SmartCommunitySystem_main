$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$outDir = Join-Path $root "visio-diagrams"
$outFile = Join-Path $outDir "architecture-sequence-diagram-editable.vsdx"

if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir | Out-Null
}

function U {
    param([Parameter(Mandatory = $true)][string] $Base64)
    return [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($Base64))
}

function Set-ShapeStyle {
    param(
        [Parameter(Mandatory = $true)] $Shape,
        [string] $FontName = "SimSun",
        [double] $FontSizePt = 11,
        [bool] $Bold = $false,
        [bool] $Center = $true,
        [bool] $NoFill = $false,
        [bool] $NoLine = $false
    )

    $Shape.CellsU("LineColor").FormulaU = "RGB(0,0,0)"
    $Shape.CellsU("Char.Color").FormulaU = "RGB(0,0,0)"
    $Shape.CellsU("LineWeight").FormulaU = "0.012 in"

    if ($NoFill) {
        $Shape.CellsU("FillPattern").FormulaU = "0"
    } else {
        $Shape.CellsU("FillPattern").FormulaU = "1"
        $Shape.CellsU("FillForegnd").FormulaU = "RGB(255,255,255)"
        $Shape.CellsU("FillBkgnd").FormulaU = "RGB(255,255,255)"
    }

    if ($NoLine) {
        $Shape.CellsU("LinePattern").FormulaU = "0"
    }

    $Shape.CellsU("Char.Font").FormulaU = "FONT(`"$FontName`")"
    $Shape.CellsU("Char.Size").FormulaU = "$FontSizePt pt"
    $Shape.CellsU("Char.Style").FormulaU = $(if ($Bold) { "1" } else { "0" })
    $Shape.CellsU("Para.HorzAlign").FormulaU = $(if ($Center) { "1" } else { "0" })
    $Shape.CellsU("VerticalAlign").FormulaU = "1"
}

function Add-RectText {
    param(
        [Parameter(Mandatory = $true)] $Page,
        [double] $Cx,
        [double] $Cy,
        [double] $W,
        [double] $H,
        [string] $Text,
        [string] $FontName = "SimSun",
        [double] $FontSizePt = 11,
        [bool] $Bold = $false
    )

    $shape = $Page.DrawRectangle($Cx - $W / 2, $Cy - $H / 2, $Cx + $W / 2, $Cy + $H / 2)
    $shape.Text = $Text
    Set-ShapeStyle -Shape $shape -FontName $FontName -FontSizePt $FontSizePt -Bold $Bold
    return $shape
}

function Add-TextOnly {
    param(
        [Parameter(Mandatory = $true)] $Page,
        [double] $Cx,
        [double] $Cy,
        [double] $W,
        [double] $H,
        [string] $Text,
        [string] $FontName = "SimSun",
        [double] $FontSizePt = 10.5,
        [bool] $Bold = $false
    )

    $shape = $Page.DrawRectangle($Cx - $W / 2, $Cy - $H / 2, $Cx + $W / 2, $Cy + $H / 2)
    $shape.Text = $Text
    Set-ShapeStyle -Shape $shape -FontName $FontName -FontSizePt $FontSizePt -Bold $Bold -NoFill $true -NoLine $true
    return $shape
}

function Add-LineArrow {
    param(
        [Parameter(Mandatory = $true)] $Page,
        [double] $X1,
        [double] $Y1,
        [double] $X2,
        [double] $Y2,
        [bool] $Dashed = $false
    )

    $shape = $Page.DrawLine($X1, $Y1, $X2, $Y2)
    $shape.CellsU("LineColor").FormulaU = "RGB(0,0,0)"
    $shape.CellsU("LineWeight").FormulaU = "0.012 in"
    $shape.CellsU("EndArrow").FormulaU = "13"
    $shape.CellsU("LinePattern").FormulaU = $(if ($Dashed) { "2" } else { "1" })
    return $shape
}

function Add-PolylineArrow {
    param(
        [Parameter(Mandatory = $true)] $Page,
        [Parameter(Mandatory = $true)][double[]] $Points
    )

    if ($Points.Count -lt 4 -or $Points.Count % 2 -ne 0) {
        throw "Points must contain x/y pairs."
    }

    $segmentCount = ($Points.Count / 2) - 1
    $segments = @()
    for ($i = 0; $i -lt $segmentCount; $i++) {
        $x1 = $Points[$i * 2]
        $y1 = $Points[$i * 2 + 1]
        $x2 = $Points[$i * 2 + 2]
        $y2 = $Points[$i * 2 + 3]
        $seg = $Page.DrawLine($x1, $y1, $x2, $y2)
        $seg.CellsU("LineColor").FormulaU = "RGB(0,0,0)"
        $seg.CellsU("LineWeight").FormulaU = "0.012 in"
        $seg.CellsU("LinePattern").FormulaU = "1"
        $segments += $seg
    }
    $segments[-1].CellsU("EndArrow").FormulaU = "13"
    return $segments
}

$titleText = U "57O757uf5p625p6E57G76aG65bqP5Zu+"
$userPageClass = U "55So5oi356uv6aG16Z2i57G7"
$controllerClass = U "5o6n5Yi25Zmo5o6l5Y+j57G7"
$authClass = U "5p2D6ZmQ6K6k6K+B57G7"
$serviceClass = U "5Lia5Yqh5pyN5Yqh57G7"
$dataClass = U "5pWw5o2u6K6/6Zeu57G7"
$exceptionClass = U "5byC5bi45aSE55CG57G7"
$requestBiz = U "6K+35rGC5Lia5Yqh5aSE55CG"
$checkLogin = U "5qCh6aqM55m75b2V5p2D6ZmQ"
$returnAuth = U "6L+U5Zue5p2D6ZmQ57uT5p6c"
$authLack = U "5p2D6ZmQ5LiN6Laz"
$authPassBiz = U "5p2D6ZmQ6YCa6L+H77yM6K+35rGC5Lia5Yqh5aSE55CG"
$businessRule = U "5omn6KGM5Lia5Yqh6KeE5YiZ"
$readData = U "6K+75Y+W5pWw5o2u"
$writeData = U "5YaZ5YWl5pWw5o2u"
$handleException = U "5byC5bi45aSE55CG"
$returnHandle = U "6L+U5Zue5aSE55CG57uT5p6c"
$showHandle = U "5pi+56S65aSE55CG57uT5p6c"

$visio = $null
$doc = $null

try {
    $visio = New-Object -ComObject Visio.Application
    $visio.Visible = $false
    $doc = $visio.Documents.Add("")
    $page = $visio.ActivePage
    $page.Name = $titleText

    $page.PageSheet.CellsU("PageWidth").FormulaU = "16 in"
    $page.PageSheet.CellsU("PageHeight").FormulaU = "9 in"
    $page.PageSheet.CellsU("PrintPageOrientation").FormulaU = "2"

    Add-TextOnly -Page $page -Cx 8 -Cy 8.55 -W 4.2 -H 0.35 -Text $titleText -FontName "SimSun" -FontSizePt 20 -Bold $true | Out-Null

    $participants = @(
        @{ Text = $userPageClass; X = 1.6 },
        @{ Text = $controllerClass; X = 4.2 },
        @{ Text = $authClass; X = 6.8 },
        @{ Text = $serviceClass; X = 9.6 },
        @{ Text = $dataClass; X = 12.2 },
        @{ Text = $exceptionClass; X = 14.8 }
    )

    foreach ($p in $participants) {
        Add-RectText -Page $page -Cx $p.X -Cy 7.85 -W 1.9 -H 0.48 -Text $p.Text -FontName "SimSun" -FontSizePt 11 | Out-Null
        $life = $page.DrawLine($p.X, 7.6, $p.X, 0.75)
        $life.CellsU("LineColor").FormulaU = "RGB(0,0,0)"
        $life.CellsU("LineWeight").FormulaU = "0.01 in"
        $life.CellsU("LinePattern").FormulaU = "2"
        $life.CellsU("EndArrow").FormulaU = "13"
    }

    Add-LineArrow -Page $page -X1 1.6 -Y1 7.2 -X2 4.2 -Y2 7.2 | Out-Null
    Add-TextOnly -Page $page -Cx 2.9 -Cy 7.34 -W 1.45 -H 0.2 -Text $requestBiz | Out-Null

    Add-LineArrow -Page $page -X1 4.2 -Y1 6.55 -X2 6.8 -Y2 6.55 | Out-Null
    Add-TextOnly -Page $page -Cx 5.5 -Cy 6.69 -W 1.55 -H 0.2 -Text $checkLogin | Out-Null

    Add-LineArrow -Page $page -X1 6.8 -Y1 5.95 -X2 4.2 -Y2 5.95 -Dashed $true | Out-Null
    Add-TextOnly -Page $page -Cx 5.5 -Cy 6.09 -W 1.45 -H 0.2 -Text $returnAuth | Out-Null

    Add-LineArrow -Page $page -X1 4.2 -Y1 5.35 -X2 1.6 -Y2 5.35 | Out-Null
    Add-TextOnly -Page $page -Cx 2.9 -Cy 5.49 -W 0.9 -H 0.2 -Text $authLack | Out-Null

    Add-LineArrow -Page $page -X1 4.2 -Y1 4.55 -X2 9.6 -Y2 4.55 | Out-Null
    Add-TextOnly -Page $page -Cx 6.9 -Cy 4.69 -W 2.2 -H 0.22 -Text $authPassBiz | Out-Null

    Add-PolylineArrow -Page $page -Points @(9.6, 4.05, 10.4, 4.05, 10.4, 3.35, 9.6, 3.35) | Out-Null
    Add-TextOnly -Page $page -Cx 10.55 -Cy 3.68 -W 1.1 -H 0.2 -Text $businessRule | Out-Null

    Add-LineArrow -Page $page -X1 9.6 -Y1 3.0 -X2 12.2 -Y2 3.0 | Out-Null
    Add-TextOnly -Page $page -Cx 10.9 -Cy 3.14 -W 0.9 -H 0.2 -Text $readData | Out-Null

    Add-LineArrow -Page $page -X1 9.6 -Y1 2.35 -X2 12.2 -Y2 2.35 | Out-Null
    Add-TextOnly -Page $page -Cx 10.9 -Cy 2.49 -W 0.9 -H 0.2 -Text $writeData | Out-Null

    Add-LineArrow -Page $page -X1 12.2 -Y1 1.7 -X2 14.8 -Y2 1.7 | Out-Null
    Add-TextOnly -Page $page -Cx 13.5 -Cy 1.84 -W 0.9 -H 0.2 -Text $handleException | Out-Null

    Add-LineArrow -Page $page -X1 14.8 -Y1 1.0 -X2 9.6 -Y2 1.0 -Dashed $true | Out-Null
    Add-TextOnly -Page $page -Cx 12.2 -Cy 1.14 -W 1.2 -H 0.2 -Text $returnHandle | Out-Null

    Add-LineArrow -Page $page -X1 9.6 -Y1 0.5 -X2 4.2 -Y2 0.5 -Dashed $true | Out-Null
    Add-TextOnly -Page $page -Cx 6.9 -Cy 0.64 -W 1.2 -H 0.2 -Text $returnHandle | Out-Null

    Add-LineArrow -Page $page -X1 4.2 -Y1 0.18 -X2 1.6 -Y2 0.18 -Dashed $true | Out-Null
    Add-TextOnly -Page $page -Cx 2.9 -Cy 0.32 -W 1.2 -H 0.2 -Text $showHandle | Out-Null

    if (Test-Path $outFile) {
        Remove-Item $outFile -Force
    }
    $doc.SaveAs($outFile)
    Write-Output $outFile
}
finally {
    if ($doc -ne $null) {
        try { $doc.Close() } catch {}
    }
    if ($visio -ne $null) {
        try { $visio.Quit() } catch {}
        try { [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($visio) } catch {}
    }
    [GC]::Collect()
    [GC]::WaitForPendingFinalizers()
}
