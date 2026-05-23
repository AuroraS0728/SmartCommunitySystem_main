Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function New-VisioApp {
    $app = New-Object -ComObject Visio.Application
    $app.Visible = $false
    return $app
}

function Set-PageSize($page, [double]$width, [double]$height) {
    $page.PageSheet.CellsU("PageWidth").FormulaU = "$width in"
    $page.PageSheet.CellsU("PageHeight").FormulaU = "$height in"
}

function Convert-Rect([double]$pageHeight, [double]$x, [double]$y, [double]$w, [double]$h, [double]$scale = 0.01) {
    $x1 = $x * $scale
    $x2 = ($x + $w) * $scale
    $y1 = $pageHeight - (($y + $h) * $scale)
    $y2 = $pageHeight - ($y * $scale)
    return @{
        X1 = $x1
        X2 = $x2
        Y1 = $y1
        Y2 = $y2
        XC = ($x1 + $x2) / 2.0
        YC = ($y1 + $y2) / 2.0
        W  = $w * $scale
        H  = $h * $scale
    }
}

function Convert-Point([double]$pageHeight, [double]$x, [double]$y, [double]$scale = 0.01) {
    return @{
        X = $x * $scale
        Y = $pageHeight - ($y * $scale)
    }
}

function Set-ShapeTextStyle($shape, [double]$fontSize, [bool]$center = $true, [bool]$bold = $false) {
    $shape.CellsU("Char.Size").FormulaU = "$fontSize pt"
    if ($bold) {
        $shape.CellsU("Char.Style").FormulaU = "1"
    }
    if ($center) {
        $shape.CellsU("Para.HorzAlign").FormulaU = "1"
        $shape.CellsU("VerticalAlign").FormulaU = "1"
    }
    $shape.CellsU("LeftMargin").FormulaU = "0.04 in"
    $shape.CellsU("RightMargin").FormulaU = "0.04 in"
    $shape.CellsU("TopMargin").FormulaU = "0.03 in"
    $shape.CellsU("BottomMargin").FormulaU = "0.03 in"
}

function Add-Box($page, [double]$pageHeight, [double]$x, [double]$y, [double]$w, [double]$h, [string]$text, [double]$fontSize = 12, [bool]$bold = $false, [bool]$showLine = $true, [bool]$showFill = $false, [bool]$center = $true) {
    $r = Convert-Rect -pageHeight $pageHeight -x $x -y $y -w $w -h $h
    $shape = $page.DrawRectangle($r.X1, $r.Y1, $r.X2, $r.Y2)
    $shape.Text = $text
    if (-not $showFill) {
        $shape.CellsU("FillPattern").FormulaU = "0"
    }
    if (-not $showLine) {
        $shape.CellsU("LinePattern").FormulaU = "0"
    } else {
        $shape.CellsU("LineWeight").FormulaU = "0.012 in"
    }
    Set-ShapeTextStyle -shape $shape -fontSize $fontSize -center $center -bold $bold
    return $shape
}

function Add-Line($page, [double]$pageHeight, [double]$x1, [double]$y1, [double]$x2, [double]$y2) {
    $p1 = Convert-Point -pageHeight $pageHeight -x $x1 -y $y1
    $p2 = Convert-Point -pageHeight $pageHeight -x $x2 -y $y2
    $line = $page.DrawLine($p1.X, $p1.Y, $p2.X, $p2.Y)
    $line.CellsU("LineWeight").FormulaU = "0.01 in"
    return $line
}

function Add-Label($page, [double]$pageHeight, [double]$x, [double]$y, [double]$w, [double]$h, [string]$text, [double]$fontSize = 10) {
    return Add-Box -page $page -pageHeight $pageHeight -x $x -y $y -w $w -h $h -text $text -fontSize $fontSize -showLine $false -showFill $false
}

function Convert-ToVerticalText([string]$text) {
    if ([string]::IsNullOrWhiteSpace($text)) {
        return $text
    }
    return (($text.ToCharArray()) -join "`n")
}

function Save-Diagram($doc, [string]$path) {
    $dir = Split-Path -Parent $path
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir | Out-Null
    }
    if (Test-Path $path) {
        Remove-Item $path -Force
    }
    $doc.SaveAs($path)
}

function Export-Png($page, [string]$pngPath) {
    if (Test-Path $pngPath) {
        Remove-Item $pngPath -Force
    }
    $page.Export($pngPath)
}

function New-DocWithPage($app, [string]$pageName, [double]$width, [double]$height) {
    $doc = $app.Documents.Add("")
    $page = $doc.Pages.Item(1)
    $page.Name = $pageName
    Set-PageSize -page $page -width $width -height $height
    return @{ Doc = $doc; Page = $page }
}

function Draw-PackageUi($app, [string]$vsdxPath, [string]$pngPath) {
    $pageHeight = 7.6
    $docInfo = New-DocWithPage -app $app -pageName "用户界面包图" -width 12 -height $pageHeight
    $doc = $docInfo.Doc
    $page = $docInfo.Page

    Add-Box $page $pageHeight 430 40 340 70 "用户界面包图" 22 $true | Out-Null
    Add-Box $page $pageHeight 470 140 240 80 "用户界面" 18 | Out-Null
    Add-Box $page $pageHeight 120 340 220 140 "Web管理端`nweb-admin`n`n工单管理 / 催缴待办 / 信用分明细" 12 | Out-Null
    Add-Box $page $pageHeight 470 300 240 90 "输入、输出`n发送业务请求" 13 | Out-Null
    Add-Box $page $pageHeight 860 340 220 140 "微信小程序端`nweapp-owner`n`n首页 / 报修 / 投诉 / 活动" 12 | Out-Null
    Add-Line $page $pageHeight 590 220 590 280 | Out-Null
    Add-Line $page $pageHeight 230 340 230 280 | Out-Null
    Add-Line $page $pageHeight 970 340 970 280 | Out-Null
    Add-Line $page $pageHeight 230 280 970 280 | Out-Null
    Add-Box $page $pageHeight 900 500 250 150 "职责：`n1. 接收用户输入与页面操作`n2. 展示处理结果与状态信息`n3. 向业务层发送请求" 10 | Out-Null
    Add-Label $page $pageHeight 420 680 360 24 "图 4-3  用户界面包图" 12 | Out-Null
    Add-Label $page $pageHeight 300 720 600 24 "Fig. 4-3 User Interface Package Diagram" 11 | Out-Null

    Save-Diagram $doc $vsdxPath
    Export-Png $page $pngPath
    $doc.Close()
}

function Draw-PackageBusiness($app, [string]$vsdxPath, [string]$pngPath) {
    $pageHeight = 7.6
    $docInfo = New-DocWithPage -app $app -pageName "业务逻辑包图" -width 12 -height $pageHeight
    $doc = $docInfo.Doc
    $page = $docInfo.Page

    Add-Box $page $pageHeight 430 40 340 70 "业务逻辑包图" 22 $true | Out-Null
    Add-Box $page $pageHeight 430 145 220 80 "业务逻辑" 18 | Out-Null
    Add-Box $page $pageHeight 430 330 250 160 "Service" 18 | Out-Null
    Add-Box $page $pageHeight 470 370 170 70 "ServiceImpl" 13 | Out-Null
    Add-Box $page $pageHeight 830 325 260 150 "Controller`n`nRepair / Recommend / Activity" 14 | Out-Null
    Add-Line $page $pageHeight 540 225 540 330 | Out-Null
    Add-Line $page $pageHeight 680 400 830 400 | Out-Null
    Add-Box $page $pageHeight 900 500 250 150 "职责：`n1. 实现核心业务处理`n2. 校验权限和业务规则`n3. 向数据访问层发送持久化请求`n4. 向界面层返回结果" 10 | Out-Null
    Add-Label $page $pageHeight 430 680 340 24 "图 4-4  业务逻辑包图" 12 | Out-Null
    Add-Label $page $pageHeight 290 720 620 24 "Fig. 4-4 Business Logic Package Diagram" 11 | Out-Null

    Save-Diagram $doc $vsdxPath
    Export-Png $page $pngPath
    $doc.Close()
}

function Draw-PackageData($app, [string]$vsdxPath, [string]$pngPath) {
    $pageHeight = 7.6
    $docInfo = New-DocWithPage -app $app -pageName "数据访问包图" -width 12 -height $pageHeight
    $doc = $docInfo.Doc
    $page = $docInfo.Page

    Add-Box $page $pageHeight 430 40 340 70 "数据访问包图" 22 $true | Out-Null
    Add-Box $page $pageHeight 420 145 230 80 "数据访问" 18 | Out-Null
    Add-Box $page $pageHeight 420 325 240 150 "Dao / Mapper`n`nUserMapper`nRepairOrderMapper`nPaymentReminderMapper" 12 | Out-Null
    Add-Box $page $pageHeight 845 320 250 90 "Mapper XML" 16 | Out-Null
    Add-Box $page $pageHeight 845 455 250 90 "DB Script" 16 | Out-Null
    Add-Line $page $pageHeight 535 225 535 325 | Out-Null
    Add-Line $page $pageHeight 660 390 845 365 | Out-Null
    Add-Line $page $pageHeight 970 410 970 455 | Out-Null
    Add-Box $page $pageHeight 900 560 250 100 "职责：`n1. 完成实体数据持久化`n2. 通过 Mapper/XML 实现复杂查询`n3. 支撑事务处理与分页访问" 10 | Out-Null
    Add-Label $page $pageHeight 430 680 340 24 "图 4-5  数据访问包图" 12 | Out-Null
    Add-Label $page $pageHeight 320 720 560 24 "Fig. 4-5 Data Access Package Diagram" 11 | Out-Null

    Save-Diagram $doc $vsdxPath
    Export-Png $page $pngPath
    $doc.Close()
}

function Draw-PackageConfig($app, [string]$vsdxPath, [string]$pngPath) {
    $pageHeight = 7.6
    $docInfo = New-DocWithPage -app $app -pageName "配置管理包图" -width 12 -height $pageHeight
    $doc = $docInfo.Doc
    $page = $docInfo.Page

    Add-Box $page $pageHeight 400 40 400 70 "配置管理包图" 22 $true | Out-Null
    Add-Box $page $pageHeight 450 145 300 80 "配置管理" 18 | Out-Null
    Add-Box $page $pageHeight 100 330 250 150 "基础配置`nWebMvc / Redis / MyBatis" 12 | Out-Null
    Add-Box $page $pageHeight 455 330 270 150 "安全认证配置`nJWT / 微信接入" 12 | Out-Null
    Add-Box $page $pageHeight 835 330 260 150 "扩展组件配置`nWebSocket / SeetaFace" 12 | Out-Null
    Add-Box $page $pageHeight 360 520 480 80 "application.yml / application-dev.yml / application-prod.yml" 11 | Out-Null
    Add-Line $page $pageHeight 600 225 600 295 | Out-Null
    Add-Line $page $pageHeight 225 330 225 295 | Out-Null
    Add-Line $page $pageHeight 590 330 590 295 | Out-Null
    Add-Line $page $pageHeight 965 330 965 295 | Out-Null
    Add-Line $page $pageHeight 225 295 965 295 | Out-Null
    Add-Line $page $pageHeight 600 480 600 520 | Out-Null
    Add-Box $page $pageHeight 900 610 250 80 "职责：`n1. 统一管理运行环境参数`n2. 提供认证、缓存与消息配置`n3. 为业务模块提供公共支撑" 10 | Out-Null
    Add-Label $page $pageHeight 430 680 340 24 "图 4-6  配置管理包图" 12 | Out-Null
    Add-Label $page $pageHeight 250 720 700 24 "Fig. 4-6 Configuration Management Package Diagram" 11 | Out-Null

    Save-Diagram $doc $vsdxPath
    Export-Png $page $pngPath
    $doc.Close()
}

function Draw-PackageException($app, [string]$vsdxPath, [string]$pngPath) {
    $pageHeight = 7.6
    $docInfo = New-DocWithPage -app $app -pageName "异常处理包图" -width 12 -height $pageHeight
    $doc = $docInfo.Doc
    $page = $docInfo.Page

    Add-Box $page $pageHeight 390 40 420 70 "异常处理包图" 22 $true | Out-Null
    Add-Box $page $pageHeight 180 145 220 80 "异常处理类" 18 | Out-Null
    Add-Box $page $pageHeight 760 145 240 80 "异常处理实现" 18 | Out-Null
    Add-Box $page $pageHeight 120 380 120 120 "参数校验异常" 10 | Out-Null
    Add-Box $page $pageHeight 300 380 120 120 "业务规则异常" 10 | Out-Null
    Add-Box $page $pageHeight 480 380 120 120 "状态转换异常" 10 | Out-Null
    Add-Box $page $pageHeight 700 380 120 120 "非法参数处理" 10 | Out-Null
    Add-Box $page $pageHeight 860 380 120 120 "统一结果封装" 10 | Out-Null
    Add-Box $page $pageHeight 1020 380 120 120 "系统异常记录" 10 | Out-Null
    Add-Line $page $pageHeight 400 185 760 185 | Out-Null
    Add-Line $page $pageHeight 180 500 290 225 | Out-Null
    Add-Line $page $pageHeight 360 500 290 225 | Out-Null
    Add-Line $page $pageHeight 540 500 290 225 | Out-Null
    Add-Line $page $pageHeight 760 225 760 380 | Out-Null
    Add-Line $page $pageHeight 820 500 880 225 | Out-Null
    Add-Line $page $pageHeight 980 500 880 225 | Out-Null
    Add-Line $page $pageHeight 1140 500 880 225 | Out-Null
    Add-Label $page $pageHeight 430 680 340 24 "图 4-7  异常处理包图" 12 | Out-Null
    Add-Label $page $pageHeight 280 720 640 24 "Fig. 4-7 Exception Handling Package Diagram" 11 | Out-Null

    Save-Diagram $doc $vsdxPath
    Export-Png $page $pngPath
    $doc.Close()
}

function Draw-SystemFunction($app, [string]$vsdxPath, [string]$pngPath) {
    $pageHeight = 8.0
    $docInfo = New-DocWithPage -app $app -pageName "系统功能模块图" -width 15 -height $pageHeight
    $doc = $docInfo.Doc
    $page = $docInfo.Page

    Add-Box $page $pageHeight 600 20 320 70 "智慧社区物业服务系统" 22 $true | Out-Null
    Add-Line $page $pageHeight 760 90 760 130 | Out-Null
    Add-Line $page $pageHeight 120 130 1380 130 | Out-Null

    $modules = @(
        @{ X = 65;   Y = 180; W = 110; H = 70; Title = "用户房产`n管理"; Subs = @("用户注册登录","个人资料维护","房产绑定管理") },
        @{ X = 235;  Y = 180; W = 110; H = 70; Title = "报修工单`n管理"; Subs = @("提交报修申请","查看工单进度","评价维修服务") },
        @{ X = 405;  Y = 180; W = 110; H = 70; Title = "投诉建议`n管理"; Subs = @("提交投诉建议","情感分析预警","查看处理结果") },
        @{ X = 575;  Y = 180; W = 110; H = 70; Title = "费用账单`n管理"; Subs = @("查看费用账单","在线缴费支付","逾期催缴提醒") },
        @{ X = 745;  Y = 180; W = 110; H = 70; Title = "社区活动`n与便民"; Subs = @("发布社区活动","报名活动服务","个性化推荐") },
        @{ X = 915;  Y = 180; W = 110; H = 70; Title = "访客安防`n管理"; Subs = @("访客邀约通行","二维码核验","异常访客处理") },
        @{ X = 1085; Y = 180; W = 110; H = 70; Title = "信用积分`n管理"; Subs = @("信用分变动","积分充值消费","规则查询统计") },
        @{ X = 1255; Y = 180; W = 110; H = 70; Title = "系统配置`n与统计"; Subs = @("公告与消息管理","推荐规则配置","数据统计分析") }
    )

    foreach ($module in $modules) {
        $midX = $module.X + ($module.W / 2.0)
        Add-Line $page $pageHeight $midX 130 $midX 180 | Out-Null
        Add-Box $page $pageHeight $module.X $module.Y $module.W $module.H $module.Title 14 | Out-Null
        Add-Line $page $pageHeight $midX 250 $midX 285 | Out-Null
        Add-Line $page $pageHeight ($module.X + 5) 285 ($module.X + 105) 285 | Out-Null

        for ($i = 0; $i -lt $module.Subs.Count; $i++) {
            $connectorX = $module.X + 25 + ($i * 32)
            $rectX = $module.X + 13 + ($i * 32)
            Add-Line $page $pageHeight $connectorX 285 $connectorX 320 | Out-Null
            $verticalText = Convert-ToVerticalText $module.Subs[$i]
            Add-Box $page $pageHeight $rectX 320 24 250 $verticalText 8 $false $true $false | Out-Null
        }
    }

    Add-Label $page $pageHeight 520 630 480 24 "图 2-4  智慧社区物业服务系统功能模块图" 12 | Out-Null
    Add-Label $page $pageHeight 390 675 740 24 "Fig. 2-4 Function Model of Smart Community Property Service System" 11 | Out-Null

    Save-Diagram $doc $vsdxPath
    Export-Png $page $pngPath
    $doc.Close()
}

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$app = New-VisioApp

try {
    Draw-PackageUi $app (Join-Path $root "用户界面包图.vsdx") (Join-Path $root "用户界面包图.png")
    Draw-PackageBusiness $app (Join-Path $root "业务逻辑包图.vsdx") (Join-Path $root "业务逻辑包图.png")
    Draw-PackageData $app (Join-Path $root "数据访问包图.vsdx") (Join-Path $root "数据访问包图.png")
    Draw-PackageConfig $app (Join-Path $root "配置管理包图.vsdx") (Join-Path $root "配置管理包图.png")
    Draw-PackageException $app (Join-Path $root "异常处理包图.vsdx") (Join-Path $root "异常处理包图.png")
    Draw-SystemFunction $app (Join-Path $root "系统功能模块图.vsdx") (Join-Path $root "系统功能模块图.png")
}
finally {
    $app.Quit()
    [System.Runtime.Interopservices.Marshal]::ReleaseComObject($app) | Out-Null
    [GC]::Collect()
    [GC]::WaitForPendingFinalizers()
}

