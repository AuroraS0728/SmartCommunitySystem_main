$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$outDir = Join-Path $root "visio-diagrams"
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
        [double] $FontSizePt = 10.5,
        [bool] $Bold = $false,
        [bool] $Center = $true,
        [bool] $NoFill = $false,
        [bool] $NoLine = $false
    )

    $Shape.CellsU("LineColor").FormulaU = "RGB(0,0,0)"
    $Shape.CellsU("Char.Color").FormulaU = "RGB(0,0,0)"
    $Shape.CellsU("LineWeight").FormulaU = "0.010 in"

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

    $Shape.CellsU("Char.Font").FormulaU = "FONT(`"SimSun`")"
    $Shape.CellsU("Char.Size").FormulaU = "$FontSizePt pt"
    $Shape.CellsU("Char.Style").FormulaU = $(if ($Bold) { "1" } else { "0" })
    $Shape.CellsU("Para.HorzAlign").FormulaU = $(if ($Center) { "1" } else { "0" })
    $Shape.CellsU("VerticalAlign").FormulaU = "1"
}

function Add-TextOnly {
    param(
        [Parameter(Mandatory = $true)] $Page,
        [double] $Cx,
        [double] $Cy,
        [double] $W,
        [double] $H,
        [string] $Text,
        [double] $FontSizePt = 10.5,
        [bool] $Bold = $false
    )

    $shape = $Page.DrawRectangle($Cx - $W / 2, $Cy - $H / 2, $Cx + $W / 2, $Cy + $H / 2)
    $shape.Text = $Text
    Set-ShapeStyle -Shape $shape -FontSizePt $FontSizePt -Bold $Bold -NoFill $true -NoLine $true
    return $shape
}

function Add-LineSegment {
    param(
        [Parameter(Mandatory = $true)] $Page,
        [double] $X1,
        [double] $Y1,
        [double] $X2,
        [double] $Y2
    )

    $shape = $Page.DrawLine($X1, $Y1, $X2, $Y2)
    $shape.CellsU("LineColor").FormulaU = "RGB(0,0,0)"
    $shape.CellsU("LineWeight").FormulaU = "0.010 in"
    return $shape
}

function Add-Polyline {
    param(
        [Parameter(Mandatory = $true)] $Page,
        [Parameter(Mandatory = $true)][double[]] $Points
    )

    for ($i = 0; $i -lt $Points.Count - 2; $i += 2) {
        Add-LineSegment -Page $Page -X1 $Points[$i] -Y1 $Points[$i + 1] -X2 $Points[$i + 2] -Y2 $Points[$i + 3] | Out-Null
    }
}

function Add-TableBox {
    param(
        [Parameter(Mandatory = $true)] $Page,
        [double] $Cx,
        [double] $Cy,
        [double] $W,
        [double] $H,
        [string] $Title
    )

    $x1 = $Cx - $W / 2
    $x2 = $Cx + $W / 2
    $y1 = $Cy - $H / 2
    $y2 = $Cy + $H / 2

    $shape = $Page.DrawRectangle($x1, $y1, $x2, $y2)
    $shape.Text = ""
    Set-ShapeStyle -Shape $shape

    $headerY = $y2 - 0.34
    $bodyMidY = $y1 + 0.18
    $midX = $Cx

    Add-LineSegment -Page $Page -X1 $x1 -Y1 $headerY -X2 $x2 -Y2 $headerY | Out-Null
    Add-LineSegment -Page $Page -X1 $midX -Y1 $y1 -X2 $midX -Y2 $headerY | Out-Null
    Add-LineSegment -Page $Page -X1 $x1 -Y1 $bodyMidY -X2 $x2 -Y2 $bodyMidY | Out-Null

    Add-TextOnly -Page $Page -Cx $Cx -Cy ($y2 - 0.17) -W ($W - 0.1) -H 0.24 -Text $Title -FontSizePt 10 | Out-Null

    return @{
        Left = $x1
        Right = $x2
        Top = $y2
        Bottom = $y1
        HeaderBottom = $headerY
        CenterX = $Cx
        CenterY = $Cy
    }
}

function Add-Cardinality {
    param(
        [Parameter(Mandatory = $true)] $Page,
        [double] $X,
        [double] $Y,
        [string] $Text
    )

    Add-TextOnly -Page $Page -Cx $X -Cy $Y -W 0.2 -H 0.18 -Text $Text -FontSizePt 11 | Out-Null
}

$title = U "6KGo6Ze06YC76L6R5YWz57O75Zu+"
$adminTable = U "566h55CG5ZGY6KGo"
$foundTable = U "5oub6aKG5bm/5Zy66KGo"
$suggestionTable = U "5bmz5Y+w5bu66K6u6KGo"
$announcementTable = U "5YWs5ZGK5L+h5oGv6KGo"
$lostTable = U "5aSx54mp5bm/5Zy66KGo"
$categoryTable = U "54mp5ZOB57G75Yir6KGo"
$userTable = U "55So5oi36KGo"
$favoriteTable = U "5pS26JeP6KGo"
$messageTable = U "5raI5oGv6KGo"
$friendTable = U "5aW95Y+L6KGo"

$manageFound = U "566h55CG5oub6aKG5L+h5oGv"
$replySuggestion = U "5Zue562U5bu66K6u"
$manageAnnouncement = U "566h55CG5YWs5ZGK"
$manageLost = U "566h55CG5aSx54mp5L+h5oGv"
$addCategory = U "5re75Yqg54mp5ZOB57G75Yir"
$manageUser = U "566h55CG55So5oi3"
$viewAnnouncement = U "5p+l55yL5YWs5ZGK"
$submitSuggestion = U "5o+Q5Ye65bu66K6u"
$addLost = U "5re75Yqg5aSx54mp5L+h5oGv"
$selectCategory = U "6YCJ5oup54mp5ZOB57G75Yir"
$addFound = U "5re75Yqg5oub6aKG5L+h5oGv"
$addFavorite = U "5re75Yqg5pS26JeP"
$sendMessage = U "5Y+R6YCB5raI5oGv"
$chatFriend = U "6IGK5aSp5aW95Y+L"
$manageFriend = U "566h55CG5aW95Y+L"

$fileName = $title + ".vsdx"
$outFile = Join-Path $outDir $fileName

$visio = $null
$doc = $null

try {
    $visio = New-Object -ComObject Visio.Application
    $visio.Visible = $false
    $doc = $visio.Documents.Add("")
    $page = $visio.ActivePage
    $page.Name = $title
    $page.PageSheet.CellsU("PageWidth").FormulaU = "11.8 in"
    $page.PageSheet.CellsU("PageHeight").FormulaU = "8.8 in"
    $page.PageSheet.CellsU("PrintPageOrientation").FormulaU = "2"

    $admin = Add-TableBox -Page $page -Cx 6.0 -Cy 7.55 -W 1.05 -H 0.8 -Title $adminTable
    $found = Add-TableBox -Page $page -Cx 1.8 -Cy 5.4 -W 1.05 -H 0.8 -Title $foundTable
    $suggest = Add-TableBox -Page $page -Cx 3.75 -Cy 5.4 -W 1.05 -H 0.8 -Title $suggestionTable
    $ann = Add-TableBox -Page $page -Cx 5.95 -Cy 5.4 -W 1.05 -H 0.8 -Title $announcementTable
    $lost = Add-TableBox -Page $page -Cx 7.85 -Cy 5.4 -W 1.05 -H 0.8 -Title $lostTable
    $category = Add-TableBox -Page $page -Cx 9.9 -Cy 5.4 -W 1.05 -H 0.8 -Title $categoryTable
    $user = Add-TableBox -Page $page -Cx 5.95 -Cy 3.3 -W 1.05 -H 0.8 -Title $userTable
    $favorite = Add-TableBox -Page $page -Cx 3.75 -Cy 1.25 -W 1.05 -H 0.8 -Title $favoriteTable
    $message = Add-TableBox -Page $page -Cx 5.95 -Cy 1.25 -W 1.05 -H 0.8 -Title $messageTable
    $friend = Add-TableBox -Page $page -Cx 8.05 -Cy 1.25 -W 1.05 -H 0.8 -Title $friendTable

    # 管理员 -> 招领广场
    Add-Polyline -Page $page -Points @(5.45, 7.75, 1.8, 7.75, 1.8, 5.82)
    Add-TextOnly -Page $page -Cx 3.0 -Cy 7.92 -W 1.5 -H 0.2 -Text $manageFound -FontSizePt 9.5 | Out-Null
    Add-Cardinality -Page $page -X 5.0 -Y 7.92 -Text "*"
    Add-Cardinality -Page $page -X 1.55 -Y 6.05 -Text "*"

    # 管理员 -> 平台建议
    Add-Polyline -Page $page -Points @(5.72, 7.35, 3.75, 7.35, 3.75, 5.82)
    Add-TextOnly -Page $page -Cx 4.0 -Cy 7.52 -W 0.9 -H 0.2 -Text $replySuggestion -FontSizePt 9.5 | Out-Null
    Add-Cardinality -Page $page -X 5.15 -Y 6.95 -Text "*"
    Add-Cardinality -Page $page -X 3.95 -Y 6.05 -Text "*"

    # 管理员 -> 公告信息
    Add-LineSegment -Page $page -X1 6.0 -Y1 7.15 -X2 6.0 -Y2 5.82 | Out-Null
    Add-TextOnly -Page $page -Cx 6.15 -Cy 6.3 -W 0.8 -H 0.2 -Text $manageAnnouncement -FontSizePt 9.5 | Out-Null
    Add-Cardinality -Page $page -X 6.18 -Y 6.95 -Text "*"
    Add-Cardinality -Page $page -X 6.18 -Y 6.05 -Text "*"

    # 管理员 -> 失物广场
    Add-Polyline -Page $page -Points @(6.28, 7.15, 6.28, 7.0, 7.85, 7.0, 7.85, 5.82)
    Add-TextOnly -Page $page -Cx 7.2 -Cy 7.16 -W 1.3 -H 0.2 -Text $manageLost -FontSizePt 9.5 | Out-Null
    Add-Cardinality -Page $page -X 6.32 -Y 6.95 -Text "*"
    Add-Cardinality -Page $page -X 8.05 -Y 6.05 -Text "*"

    # 管理员 -> 物品类别
    Add-Polyline -Page $page -Points @(6.55, 7.75, 10.55, 7.75, 10.55, 5.82)
    Add-TextOnly -Page $page -Cx 9.3 -Cy 7.92 -W 1.2 -H 0.2 -Text $addCategory -FontSizePt 9.5 | Out-Null
    Add-Cardinality -Page $page -X 6.88 -Y 7.92 -Text "*"
    Add-Cardinality -Page $page -X 10.1 -Y 6.05 -Text "*"

    # 管理员 -> 用户
    Add-Polyline -Page $page -Points @(5.25, 7.15, 5.25, 3.72, 5.42, 3.72)
    Add-TextOnly -Page $page -Cx 4.95 -Cy 5.2 -W 0.75 -H 0.2 -Text $manageUser -FontSizePt 9.5 | Out-Null
    Add-Cardinality -Page $page -X 5.07 -Y 6.55 -Text "*"
    Add-Cardinality -Page $page -X 5.18 -Y 3.55 -Text "1"

    # 用户 -> 公告信息
    Add-LineSegment -Page $page -X1 5.95 -Y1 3.72 -X2 5.95 -Y2 4.98 | Out-Null
    Add-TextOnly -Page $page -Cx 5.95 -Cy 4.05 -W 0.9 -H 0.2 -Text $viewAnnouncement -FontSizePt 9.5 | Out-Null
    Add-Cardinality -Page $page -X 6.18 -Y 4.88 -Text "*"
    Add-Cardinality -Page $page -X 6.18 -Y 3.95 -Text "*"

    # 用户 -> 平台建议
    Add-Polyline -Page $page -Points @(5.42, 3.52, 3.45, 3.52, 3.45, 4.98, 3.75, 4.98)
    Add-TextOnly -Page $page -Cx 4.0 -Cy 3.68 -W 0.9 -H 0.2 -Text $submitSuggestion -FontSizePt 9.5 | Out-Null
    Add-Cardinality -Page $page -X 5.25 -Y 3.68 -Text "1"
    Add-Cardinality -Page $page -X 3.45 -Y 4.88 -Text "*"

    # 用户 -> 招领广场
    Add-Polyline -Page $page -Points @(5.42, 3.25, 1.65, 3.25, 1.65, 4.98, 1.8, 4.98)
    Add-TextOnly -Page $page -Cx 2.55 -Cy 3.42 -W 1.1 -H 0.2 -Text $addFound -FontSizePt 9.5 | Out-Null
    Add-Cardinality -Page $page -X 5.22 -Y 3.18 -Text "1"
    Add-Cardinality -Page $page -X 1.7 -Y 4.88 -Text "*"

    # 用户 -> 失物广场
    Add-Polyline -Page $page -Points @(6.48, 3.55, 8.15, 3.55, 8.15, 4.98, 7.85, 4.98)
    Add-TextOnly -Page $page -Cx 7.35 -Cy 3.72 -W 1.1 -H 0.2 -Text $addLost -FontSizePt 9.5 | Out-Null
    Add-Cardinality -Page $page -X 6.15 -Y 3.68 -Text "1"
    Add-Cardinality -Page $page -X 8.1 -Y 4.88 -Text "*"

    # 用户 -> 物品类别
    Add-Polyline -Page $page -Points @(6.48, 3.18, 10.25, 3.18, 10.25, 4.98, 9.9, 4.98)
    Add-TextOnly -Page $page -Cx 8.75 -Cy 3.35 -W 1.25 -H 0.2 -Text $selectCategory -FontSizePt 9.5 | Out-Null
    Add-Cardinality -Page $page -X 6.15 -Y 3.18 -Text "1"
    Add-Cardinality -Page $page -X 10.28 -Y 4.88 -Text "*"

    # 用户 -> 收藏
    Add-Polyline -Page $page -Points @(5.42, 3.02, 3.75, 3.02, 3.75, 1.67)
    Add-TextOnly -Page $page -Cx 3.45 -Cy 2.58 -W 0.9 -H 0.2 -Text $addFavorite -FontSizePt 9.5 | Out-Null
    Add-Cardinality -Page $page -X 5.18 -Y 2.82 -Text "1"
    Add-Cardinality -Page $page -X 3.95 -Y 1.85 -Text "*"

    # 用户 -> 消息
    Add-LineSegment -Page $page -X1 5.95 -Y1 2.88 -X2 5.95 -Y2 1.67 | Out-Null
    Add-TextOnly -Page $page -Cx 5.95 -Cy 2.45 -W 0.8 -H 0.2 -Text $sendMessage -FontSizePt 9.5 | Out-Null
    Add-Cardinality -Page $page -X 6.18 -Y 2.78 -Text "1"
    Add-Cardinality -Page $page -X 6.18 -Y 1.85 -Text "*"

    # 用户 -> 好友
    Add-Polyline -Page $page -Points @(6.48, 2.88, 8.05, 2.88, 8.05, 1.67)
    Add-TextOnly -Page $page -Cx 7.55 -Cy 2.58 -W 0.8 -H 0.2 -Text $chatFriend -FontSizePt 9.5 | Out-Null
    Add-Cardinality -Page $page -X 6.62 -Y 2.82 -Text "*"
    Add-Cardinality -Page $page -X 8.23 -Y 1.85 -Text "*"

    # 管理员 -> 好友
    Add-Polyline -Page $page -Points @(6.55, 7.75, 10.95, 7.75, 10.95, 0.95, 8.58, 0.95)
    Add-TextOnly -Page $page -Cx 10.85 -Cy 4.45 -W 0.75 -H 0.2 -Text $manageFriend -FontSizePt 9.5 | Out-Null
    Add-Cardinality -Page $page -X 6.82 -Y 7.92 -Text "*"
    Add-Cardinality -Page $page -X 8.65 -Y 1.15 -Text "*"

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
