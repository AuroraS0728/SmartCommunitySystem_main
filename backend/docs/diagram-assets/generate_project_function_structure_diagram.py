from __future__ import annotations

import html
from dataclasses import dataclass
from pathlib import Path
from typing import Sequence

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "visio-diagrams"
FONT_FILE = r"C:\Windows\Fonts\simsun.ttc"
TITLE_FONT = ImageFont.truetype(FONT_FILE, 34)
BOX_FONT = ImageFont.truetype(FONT_FILE, 18)
SMALL_FONT = ImageFont.truetype(FONT_FILE, 16)
TINY_FONT = ImageFont.truetype(FONT_FILE, 14)

BLACK = "#000000"
WHITE = "#FFFFFF"


class SvgCanvas:
    def __init__(self, width: int, height: int) -> None:
        self.width = width
        self.height = height
        self.elements: list[str] = [f'<rect x="0" y="0" width="{width}" height="{height}" fill="{WHITE}" />']

    def rect(
        self,
        x: int,
        y: int,
        w: int,
        h: int,
        fill: str = WHITE,
        stroke: str = BLACK,
        stroke_width: int = 2,
        rx: int = 0,
    ) -> None:
        self.elements.append(
            f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{rx}" ry="{rx}" fill="{fill}" stroke="{stroke}" stroke-width="{stroke_width}" />'
        )

    def line(self, x1: int, y1: int, x2: int, y2: int, stroke_width: int = 2) -> None:
        self.elements.append(
            f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{BLACK}" stroke-width="{stroke_width}" />'
        )

    def text(self, x: int, y: int, text: str, font_size: int, family: str = "SimSun") -> None:
        lines = text.split("\n")
        if len(lines) == 1:
            self.elements.append(
                f'<text x="{x}" y="{y}" text-anchor="middle" font-family="{family}" font-size="{font_size}" fill="{BLACK}">{html.escape(text)}</text>'
            )
            return
        line_h = int(font_size * 1.35)
        base = y - (len(lines) - 1) * line_h / 2
        spans = []
        for i, line in enumerate(lines):
            dy = 0 if i == 0 else line_h
            spans.append(f'<tspan x="{x}" dy="{dy}">{html.escape(line)}</tspan>')
        self.elements.append(
            f'<text x="{x}" y="{base}" text-anchor="middle" font-family="{family}" font-size="{font_size}" fill="{BLACK}">{"".join(spans)}</text>'
        )

    def save(self, path: Path) -> None:
        path.write_text(
            f'<svg xmlns="http://www.w3.org/2000/svg" width="{self.width}" height="{self.height}" viewBox="0 0 {self.width} {self.height}">\n  '
            + "\n  ".join(self.elements)
            + "\n</svg>\n",
            encoding="utf-8",
        )


def text_size(draw: ImageDraw.ImageDraw, text: str, font: ImageFont.FreeTypeFont) -> tuple[int, int]:
    l, t, r, b = draw.multiline_textbbox((0, 0), text, font=font, spacing=4, align="center")
    return r - l, b - t


def draw_center_text(draw: ImageDraw.ImageDraw, box: tuple[int, int, int, int], text: str, font: ImageFont.FreeTypeFont) -> None:
    w, h = text_size(draw, text, font)
    x1, y1, x2, y2 = box
    draw.multiline_text(((x1 + x2 - w) / 2, (y1 + y2 - h) / 2), text, font=font, fill=BLACK, spacing=4, align="center")


def draw_box(
    draw: ImageDraw.ImageDraw,
    svg: SvgCanvas,
    box: tuple[int, int, int, int],
    text: str,
    font: ImageFont.FreeTypeFont,
    svg_font_size: int,
) -> None:
    x1, y1, x2, y2 = box
    draw.rectangle(box, fill=WHITE, outline=BLACK, width=2)
    draw_center_text(draw, box, text, font)
    svg.rect(x1, y1, x2 - x1, y2 - y1, fill=WHITE, stroke=BLACK, stroke_width=2)
    svg.text((x1 + x2) // 2, (y1 + y2) // 2 + 6, text, svg_font_size)


def verticalize(text: str) -> str:
    return "\n".join([c for c in text.replace("\n", "") if c.strip()])


def draw_vertical_bar(
    draw: ImageDraw.ImageDraw,
    svg: SvgCanvas,
    center_x: int,
    top_y: int,
    width: int,
    height: int,
    text: str,
    font: ImageFont.FreeTypeFont,
    svg_font_size: int,
) -> None:
    x1 = center_x - width // 2
    x2 = center_x + width // 2
    box = (x1, top_y, x2, top_y + height)
    draw.rectangle(box, fill=WHITE, outline=BLACK, width=2)
    svg.rect(x1, top_y, width, height, fill=WHITE, stroke=BLACK, stroke_width=2)
    vertical_text = verticalize(text)
    draw_center_text(draw, (x1 + 4, top_y + 6, x2 - 4, top_y + height - 6), vertical_text, font)
    svg.text(center_x, top_y + height // 2 + 5, vertical_text, svg_font_size)


def draw_title(draw: ImageDraw.ImageDraw, svg: SvgCanvas, width: int, title: str) -> None:
    draw.text((width // 2 - 300, 24), title, font=TITLE_FONT, fill=BLACK)
    svg.text(width // 2, 60, title, 34)


@dataclass
class Domain:
    label: str
    leaves: list[str]


@dataclass
class Branch:
    label: str
    x1: int
    x2: int
    domains: list[Domain]


def centers(x1: int, x2: int, count: int) -> list[int]:
    if count == 1:
        return [(x1 + x2) // 2]
    usable = x2 - x1
    return [x1 + round(i * usable / (count - 1)) for i in range(count)]


def render_branch(draw: ImageDraw.ImageDraw, svg: SvgCanvas, branch: Branch, header_center_y: int) -> None:
    header_w = 210
    header_h = 62
    header_center_x = (branch.x1 + branch.x2) // 2
    header_box = (
        header_center_x - header_w // 2,
        header_center_y - header_h // 2,
        header_center_x + header_w // 2,
        header_center_y + header_h // 2,
    )
    draw_box(draw, svg, header_box, branch.label, BOX_FONT, 18)

    domain_centers = centers(branch.x1, branch.x2, len(branch.domains))
    bus_y = header_box[3] + 68
    domain_top = bus_y + 44
    domain_h = 230
    domain_w = 40
    leaf_w = 34
    leaf_h = 156
    leaf_gap = 14

    svg.line(domain_centers[0], bus_y, domain_centers[-1], bus_y)
    draw.line((domain_centers[0], bus_y, domain_centers[-1], bus_y), fill=BLACK, width=2)
    svg.line(header_center_x, header_box[3], header_center_x, bus_y)
    draw.line((header_center_x, header_box[3], header_center_x, bus_y), fill=BLACK, width=2)

    for domain, cx in zip(branch.domains, domain_centers):
        svg.line(cx, bus_y, cx, domain_top)
        draw.line((cx, bus_y, cx, domain_top), fill=BLACK, width=2)
        draw_vertical_bar(draw, svg, cx, domain_top, domain_w, domain_h, domain.label, SMALL_FONT, 16)

        leaves_top = domain_top + domain_h + 54
        svg.line(cx, domain_top + domain_h, cx, leaves_top - 12)
        draw.line((cx, domain_top + domain_h, cx, leaves_top - 12), fill=BLACK, width=2)
        count = len(domain.leaves)
        if count == 1:
            leaf_centers = [cx]
        else:
            start = cx - ((count - 1) * (leaf_w + 16)) // 2
            leaf_centers = [start + i * (leaf_w + 16) for i in range(count)]
            svg.line(leaf_centers[0], leaves_top - 12, leaf_centers[-1], leaves_top - 12)
            draw.line((leaf_centers[0], leaves_top - 12, leaf_centers[-1], leaves_top - 12), fill=BLACK, width=2)
        for leaf_cx, leaf in zip(leaf_centers, domain.leaves):
            svg.line(leaf_cx, leaves_top - 12, leaf_cx, leaves_top)
            draw.line((leaf_cx, leaves_top - 12, leaf_cx, leaves_top), fill=BLACK, width=2)
            draw_vertical_bar(draw, svg, leaf_cx, leaves_top, leaf_w, leaf_h, leaf, TINY_FONT, 14)


def generate() -> tuple[Path, Path]:
    width, height = 3400, 1280
    title = "智慧社区物业服务管理系统功能结构图"
    out_png = OUT_DIR / "project-system-function-structure.png"
    out_svg = OUT_DIR / "project-system-function-structure.svg"

    branches = [
        Branch(
            label="业主侧\n小程序",
            x1=140,
            x2=1060,
            domains=[
                Domain("实现住户身份接入\n与账户维护", ["账号登录", "业主注册", "改密绑手机"]),
                Domain("实现报修申请\n与进度反馈", ["在线报修", "工单查询", "上门评价"]),
                Domain("实现附加服务预约\n与结果通知", ["预约下单", "积分支付", "消息反馈"]),
                Domain("实现费用账单查询\n与积分支付", ["物业缴费", "停车缴费", "积分明细"]),
                Domain("实现访客通行授权\n与门禁交互", ["邀请码管理", "动态二维码", "入场通知"]),
                Domain("实现社区信息获取\n与互动参与", ["公告活动", "二手失物", "投诉建议"]),
                Domain("实现个人资料维护\n与便民入口聚合", ["个人中心", "收藏消息", "停车月卡"]),
            ],
        ),
        Branch(
            label="安防侧\n小程序",
            x1=1180,
            x2=1740,
            domains=[
                Domain("实现门禁扫码核验\n与通行确认", ["动态码核验", "邀请码核验"]),
                Domain("实现费用异议审核\n与结果回传", ["异议列表", "驳回处理", "通过处理"]),
                Domain("实现投诉建议查看\n与在线回复", ["投诉查看", "反馈回复"]),
            ],
        ),
        Branch(
            label="维修侧\n小程序",
            x1=1860,
            x2=2420,
            domains=[
                Domain("实现任务接收\n与工单查看", ["任务列表", "工单详情"]),
                Domain("实现上门核验\n与身份确认", ["验证码核验", "人脸核验"]),
                Domain("实现作业反馈\n与状态流转", ["开工处理", "完工回传", "图片上传"]),
                Domain("实现个人信息查看\n与消息协同", ["绩效查看", "消息通知"]),
            ],
        ),
        Branch(
            label="物业管理\n后台",
            x1=2520,
            x2=3260,
            domains=[
                Domain("实现资产档案归集\n与住户维护", ["房产档案", "住户画像"]),
                Domain("实现工单调度分派\n与 SLA 督办", ["智能派单", "优先级调整", "超时扫描"]),
                Domain("实现费用账单生成\n与催缴运营", ["账单管理", "积分充值", "催缴任务"]),
                Domain("实现访客记录核验\n与黑名单处置", ["访客记录", "黑名单管理", "核验记录"]),
                Domain("实现维修人员排班\n与人脸配置", ["人员档案", "排班状态", "人脸注册"]),
                Domain("实现公告活动发布\n与规则维护", ["公告管理", "活动管理", "推荐规则"]),
                Domain("实现投诉信用任务\n与统计分析", ["投诉分析", "信用记录", "数据看板", "系统日志"]),
            ],
        ),
    ]

    img = Image.new("RGB", (width, height), WHITE)
    draw = ImageDraw.Draw(img)
    svg = SvgCanvas(width, height)

    draw_title(draw, svg, width, title)

    root_box = (1350, 110, 2050, 182)
    draw_box(draw, svg, root_box, "智慧社区物业服务管理系统", BOX_FONT, 20)

    global_bus_y = 270
    branch_centers = [((branch.x1 + branch.x2) // 2) for branch in branches]
    draw.line((branch_centers[0], global_bus_y, branch_centers[-1], global_bus_y), fill=BLACK, width=2)
    svg.line(branch_centers[0], global_bus_y, branch_centers[-1], global_bus_y)
    root_center_x = (root_box[0] + root_box[2]) // 2
    draw.line((root_center_x, root_box[3], root_center_x, global_bus_y), fill=BLACK, width=2)
    svg.line(root_center_x, root_box[3], root_center_x, global_bus_y)

    branch_center_y = 350
    for center_x in branch_centers:
        draw.line((center_x, global_bus_y, center_x, branch_center_y - 31), fill=BLACK, width=2)
        svg.line(center_x, global_bus_y, center_x, branch_center_y - 31)

    for branch in branches:
        render_branch(draw, svg, branch, branch_center_y)

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    img.save(out_png)
    svg.save(out_svg)
    return out_png, out_svg


if __name__ == "__main__":
    png, svg = generate()
    print(png)
    print(svg)
