from __future__ import annotations

import html
import os
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, Sequence

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "visio-diagrams"
FONT_FILE = r"C:\Windows\Fonts\msyh.ttc"
TITLE_FONT = ImageFont.truetype(FONT_FILE, 34)
TEXT_FONT = ImageFont.truetype(FONT_FILE, 20)
SMALL_FONT = ImageFont.truetype(FONT_FILE, 16)
TINY_FONT = ImageFont.truetype(FONT_FILE, 14)

THEME = {
    "page": "#FFFFFF",
    "accent": "#5B7DB1",
    "accent_light": "#EAF1FB",
    "accent_mid": "#D6E1F5",
    "line": "#54657F",
    "text": "#1F2A3A",
    "muted": "#6F7F96",
    "shadow": "#D8E2F1",
}


def ensure_dir(path: Path) -> None:
    path.mkdir(parents=True, exist_ok=True)


def xml_escape(value: str) -> str:
    return html.escape(value, quote=True)


def text_bbox(draw: ImageDraw.ImageDraw, text: str, font: ImageFont.FreeTypeFont) -> tuple[int, int]:
    left, top, right, bottom = draw.multiline_textbbox((0, 0), text, font=font, spacing=4, align="center")
    return right - left, bottom - top


def draw_multiline_centered(
    draw: ImageDraw.ImageDraw,
    box: tuple[int, int, int, int],
    text: str,
    font: ImageFont.FreeTypeFont,
    fill: str,
    spacing: int = 4,
) -> None:
    x1, y1, x2, y2 = box
    width, height = text_bbox(draw, text, font)
    x = x1 + (x2 - x1 - width) / 2
    y = y1 + (y2 - y1 - height) / 2
    draw.multiline_text((x, y), text, font=font, fill=fill, spacing=spacing, align="center")


def save_png(img: Image.Image, out_path: Path) -> None:
    img.save(out_path, format="PNG")


class SvgCanvas:
    def __init__(self, width: int, height: int, bg: str) -> None:
        self.width = width
        self.height = height
        self.elements: list[str] = [
            f'<rect x="0" y="0" width="{width}" height="{height}" fill="{bg}" />'
        ]

    def rect(self, x: int, y: int, w: int, h: int, fill: str, stroke: str, stroke_width: int = 2, rx: int = 8) -> None:
        self.elements.append(
            f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{rx}" ry="{rx}" '
            f'fill="{fill}" stroke="{stroke}" stroke-width="{stroke_width}" />'
        )

    def line(
        self,
        x1: int,
        y1: int,
        x2: int,
        y2: int,
        stroke: str,
        stroke_width: int = 2,
        dash: str | None = None,
    ) -> None:
        dash_attr = f' stroke-dasharray="{dash}"' if dash else ""
        self.elements.append(
            f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" '
            f'stroke="{stroke}" stroke-width="{stroke_width}"{dash_attr} />'
        )

    def polyline(
        self,
        points: Sequence[tuple[int, int]],
        stroke: str,
        stroke_width: int = 2,
        fill: str = "none",
        dash: str | None = None,
    ) -> None:
        points_attr = " ".join(f"{x},{y}" for x, y in points)
        dash_attr = f' stroke-dasharray="{dash}"' if dash else ""
        self.elements.append(
            f'<polyline points="{points_attr}" fill="{fill}" stroke="{stroke}" '
            f'stroke-width="{stroke_width}"{dash_attr} />'
        )

    def polygon(self, points: Sequence[tuple[int, int]], fill: str) -> None:
        points_attr = " ".join(f"{x},{y}" for x, y in points)
        self.elements.append(f'<polygon points="{points_attr}" fill="{fill}" />')

    def text(
        self,
        x: int,
        y: int,
        text: str,
        font_size: int,
        fill: str,
        anchor: str = "middle",
        weight: str = "normal",
        family: str = "Microsoft YaHei",
        line_height: int | None = None,
    ) -> None:
        line_height = line_height or int(font_size * 1.3)
        lines = text.split("\n")
        if len(lines) == 1:
            self.elements.append(
                f'<text x="{x}" y="{y}" text-anchor="{anchor}" font-family="{family}" '
                f'font-size="{font_size}" font-weight="{weight}" fill="{fill}">{xml_escape(text)}</text>'
            )
            return

        base_y = y - (len(lines) - 1) * line_height / 2
        tspans = []
        for idx, line in enumerate(lines):
            dy = 0 if idx == 0 else line_height
            tspans.append(
                f'<tspan x="{x}" dy="{dy}">{xml_escape(line)}</tspan>'
            )
        self.elements.append(
            f'<text x="{x}" y="{base_y}" text-anchor="{anchor}" font-family="{family}" '
            f'font-size="{font_size}" font-weight="{weight}" fill="{fill}">{"".join(tspans)}</text>'
        )

    def to_svg(self) -> str:
        body = "\n  ".join(self.elements)
        return (
            f'<svg xmlns="http://www.w3.org/2000/svg" width="{self.width}" height="{self.height}" '
            f'viewBox="0 0 {self.width} {self.height}">\n  {body}\n</svg>\n'
        )


def rounded_box_png(
    draw: ImageDraw.ImageDraw,
    box: tuple[int, int, int, int],
    fill: str,
    stroke: str,
    radius: int = 12,
    width: int = 2,
    shadow: bool = False,
) -> None:
    if shadow:
        sx1, sy1, sx2, sy2 = box[0] + 4, box[1] + 4, box[2] + 4, box[3] + 4
        draw.rounded_rectangle((sx1, sy1, sx2, sy2), radius=radius, fill=THEME["shadow"])
    draw.rounded_rectangle(box, radius=radius, fill=fill, outline=stroke, width=width)


def arrow_head(start: tuple[int, int], end: tuple[int, int], size: int = 10) -> list[tuple[int, int]]:
    x1, y1 = start
    x2, y2 = end
    if abs(x2 - x1) >= abs(y2 - y1):
        if x2 >= x1:
            return [(x2, y2), (x2 - size, y2 - size // 2), (x2 - size, y2 + size // 2)]
        return [(x2, y2), (x2 + size, y2 - size // 2), (x2 + size, y2 + size // 2)]
    if y2 >= y1:
        return [(x2, y2), (x2 - size // 2, y2 - size), (x2 + size // 2, y2 - size)]
    return [(x2, y2), (x2 - size // 2, y2 + size), (x2 + size // 2, y2 + size)]


def draw_arrow_png(
    draw: ImageDraw.ImageDraw,
    start: tuple[int, int],
    end: tuple[int, int],
    fill: str,
    width: int = 2,
    dash: tuple[int, int] | None = None,
) -> None:
    if dash:
        sx, sy = start
        ex, ey = end
        total = ((ex - sx) ** 2 + (ey - sy) ** 2) ** 0.5
        if total == 0:
            return
        dx = (ex - sx) / total
        dy = (ey - sy) / total
        dash_on, dash_off = dash
        cursor = 0.0
        while cursor < total:
            seg_start = cursor
            seg_end = min(total, cursor + dash_on)
            draw.line(
                (
                    sx + dx * seg_start,
                    sy + dy * seg_start,
                    sx + dx * seg_end,
                    sy + dy * seg_end,
                ),
                fill=fill,
                width=width,
            )
            cursor += dash_on + dash_off
    else:
        draw.line((start, end), fill=fill, width=width)
    draw.polygon(arrow_head(start, end), fill=fill)


def draw_arrow_svg(
    svg: SvgCanvas,
    start: tuple[int, int],
    end: tuple[int, int],
    fill: str,
    width: int = 2,
    dash: str | None = None,
) -> None:
    svg.line(start[0], start[1], end[0], end[1], stroke=fill, stroke_width=width, dash=dash)
    svg.polygon(arrow_head(start, end), fill=fill)


def label_on_line_png(
    draw: ImageDraw.ImageDraw,
    pos: tuple[int, int],
    text: str,
    font: ImageFont.FreeTypeFont = SMALL_FONT,
    fill: str = THEME["text"],
    bg: str = THEME["page"],
) -> None:
    w, h = text_bbox(draw, text, font)
    x, y = pos
    pad = 4
    draw.rounded_rectangle((x - w / 2 - pad, y - h / 2 - pad, x + w / 2 + pad, y + h / 2 + pad), radius=6, fill=bg)
    draw.text((x - w / 2, y - h / 2), text, font=font, fill=fill)


def class_box_height(attrs: Sequence[str], methods: Sequence[str]) -> int:
    header = 44
    attrs_h = max(36, len(attrs) * 24 + 18)
    methods_h = max(36, len(methods) * 24 + 18)
    return header + attrs_h + methods_h


@dataclass
class ClassBox:
    name: str
    attrs: list[str]
    methods: list[str]
    x: int
    y: int
    w: int = 260

    @property
    def h(self) -> int:
        return class_box_height(self.attrs, self.methods)


def draw_class_box_png(draw: ImageDraw.ImageDraw, box: ClassBox) -> None:
    rounded_box_png(draw, (box.x, box.y, box.x + box.w, box.y + box.h), THEME["page"], THEME["line"], radius=8, shadow=True)
    draw.rounded_rectangle((box.x, box.y, box.x + box.w, box.y + 44), radius=8, fill=THEME["accent"], outline=THEME["line"], width=2)
    draw.rectangle((box.x, box.y + 36, box.x + box.w, box.y + 44), fill=THEME["accent"])
    draw.line((box.x, box.y + 44, box.x + box.w, box.y + 44), fill=THEME["line"], width=2)
    attr_sep = box.y + 44 + max(36, len(box.attrs) * 24 + 18)
    draw.line((box.x, attr_sep, box.x + box.w, attr_sep), fill=THEME["line"], width=2)
    draw_multiline_centered(draw, (box.x + 8, box.y + 4, box.x + box.w - 8, box.y + 40), box.name, TEXT_FONT, "#FFFFFF")
    y = box.y + 56
    for attr in box.attrs:
        draw.text((box.x + 14, y), attr, font=SMALL_FONT, fill=THEME["text"])
        y += 24
    y = attr_sep + 12
    for method in box.methods:
        draw.text((box.x + 14, y), method, font=SMALL_FONT, fill=THEME["text"])
        y += 24


def draw_class_box_svg(svg: SvgCanvas, box: ClassBox) -> None:
    svg.rect(box.x + 4, box.y + 4, box.w, box.h, fill=THEME["shadow"], stroke=THEME["shadow"], stroke_width=0, rx=8)
    svg.rect(box.x, box.y, box.w, box.h, fill=THEME["page"], stroke=THEME["line"], stroke_width=2, rx=8)
    svg.rect(box.x, box.y, box.w, 44, fill=THEME["accent"], stroke=THEME["line"], stroke_width=2, rx=8)
    attr_sep = box.y + 44 + max(36, len(box.attrs) * 24 + 18)
    svg.line(box.x, box.y + 44, box.x + box.w, box.y + 44, THEME["line"], 2)
    svg.line(box.x, attr_sep, box.x + box.w, attr_sep, THEME["line"], 2)
    svg.text(box.x + box.w // 2, box.y + 28, box.name, 22, "#FFFFFF", weight="bold")
    y = box.y + 74
    for attr in box.attrs:
        svg.text(box.x + 14, y, attr, 16, THEME["text"], anchor="start")
        y += 24
    y = attr_sep + 28
    for method in box.methods:
        svg.text(box.x + 14, y, method, 16, THEME["text"], anchor="start")
        y += 24


def save_svg(svg: SvgCanvas, path: Path) -> None:
    path.write_text(svg.to_svg(), encoding="utf-8")


def generate_sequence_diagram() -> None:
    width, height = 1800, 980
    title = "维修工单提交流程顺序图"
    participants = [
        ("业务小程序", 120),
        ("RepairController", 400),
        ("RepairGradingService", 700),
        ("WorkerRecommendService", 1010),
        ("RepairOrderMapper", 1320),
        ("物业管理端", 1620),
    ]
    messages = [
        (0, 1, 180, "1  提交报修", False),
        (1, 2, 270, "2  计算优先级", False),
        (2, 1, 340, "3  返回结果", True),
        (1, 3, 410, "4  推荐人员", False),
        (3, 1, 480, "5  返回建议", True),
        (1, 4, 560, "6  写入工单", False),
        (4, 1, 630, "7  返回编号", True),
        (1, 0, 710, "8  提交成功", True),
        (5, 4, 800, "9  查询/跟进", False),
        (5, 4, 880, "10  更新状态", False),
    ]

    img = Image.new("RGB", (width, height), THEME["page"])
    draw = ImageDraw.Draw(img)
    svg = SvgCanvas(width, height, THEME["page"])

    draw.text((width / 2 - 170, 40), title, font=TITLE_FONT, fill=THEME["text"])
    svg.text(width // 2, 72, title, 34, THEME["text"], weight="bold")

    header_y = 120
    for label, x in participants:
        rounded_box_png(draw, (x - 100, header_y, x + 100, header_y + 56), THEME["accent_light"], THEME["line"], shadow=True)
        draw_multiline_centered(draw, (x - 92, header_y + 6, x + 92, header_y + 50), label, SMALL_FONT, THEME["text"])
        draw.line((x, header_y + 56, x, height - 80), fill=THEME["line"], width=2)

        svg.rect(x - 100 + 4, header_y + 4, 200, 56, fill=THEME["shadow"], stroke=THEME["shadow"], stroke_width=0, rx=10)
        svg.rect(x - 100, header_y, 200, 56, fill=THEME["accent_light"], stroke=THEME["line"], stroke_width=2, rx=10)
        svg.text(x, header_y + 35, label, 17, THEME["text"], weight="bold")
        svg.line(x, header_y + 56, x, height - 80, stroke=THEME["line"], stroke_width=2)

    for src, dst, y, label, dashed in messages:
        start = (participants[src][1], y)
        end = (participants[dst][1], y)
        if dashed:
            draw_arrow_png(draw, start, end, THEME["line"], width=2, dash=(10, 8))
            draw_arrow_svg(svg, start, end, THEME["line"], width=2, dash="10 8")
        else:
            draw_arrow_png(draw, start, end, THEME["line"], width=2)
            draw_arrow_svg(svg, start, end, THEME["line"], width=2)
        label_x = (start[0] + end[0]) // 2
        label_on_line_png(draw, (label_x, y - 18), label, SMALL_FONT)
        svg.rect(label_x - 90, y - 38, 180, 26, fill=THEME["page"], stroke=THEME["page"], stroke_width=0, rx=6)
        svg.text(label_x, y - 20, label, 16, THEME["text"])

    out_png = OUT_DIR / "repair-sequence-diagram.png"
    out_svg = OUT_DIR / "repair-sequence-diagram.svg"
    save_png(img, out_png)
    save_svg(svg, out_svg)


def generate_architecture_sequence_diagram() -> None:
    width, height = 1900, 1080
    title = "系统架构类顺序图"
    participants = [
        ("用户端页面类", 160),
        ("控制器接口类", 460),
        ("权限认证类", 760),
        ("业务服务类", 1080),
        ("数据访问类", 1380),
        ("异常处理类", 1680),
    ]
    messages = [
        (0, 1, 190, "请求业务处理", False),
        (1, 2, 270, "校验登录权限", False),
        (2, 1, 340, "返回权限结果", True),
        (1, 0, 410, "权限不足", False),
        (1, 3, 500, "权限通过，请求业务处理", False),
        (3, 4, 680, "读取数据", False),
        (3, 4, 760, "写入数据", False),
        (4, 5, 840, "异常处理", False),
        (5, 3, 920, "返回处理结果", True),
        (3, 1, 980, "返回处理结果", True),
        (1, 0, 1040, "显示处理结果", True),
    ]

    img = Image.new("RGB", (width, height), THEME["page"])
    draw = ImageDraw.Draw(img)
    svg = SvgCanvas(width, height, THEME["page"])

    draw.text((width / 2 - 170, 40), title, font=TITLE_FONT, fill=THEME["text"])
    svg.text(width // 2, 72, title, 34, THEME["text"], weight="bold")

    header_y = 120
    for label, x in participants:
        rounded_box_png(draw, (x - 106, header_y, x + 106, header_y + 56), THEME["accent_light"], THEME["line"], shadow=True)
        draw_multiline_centered(draw, (x - 98, header_y + 6, x + 98, header_y + 50), label, SMALL_FONT, THEME["text"])
        draw.line((x, header_y + 56, x, height - 90), fill=THEME["line"], width=2)

        svg.rect(x - 106 + 4, header_y + 4, 212, 56, fill=THEME["shadow"], stroke=THEME["shadow"], stroke_width=0, rx=10)
        svg.rect(x - 106, header_y, 212, 56, fill=THEME["accent_light"], stroke=THEME["line"], stroke_width=2, rx=10)
        svg.text(x, header_y + 35, label, 17, THEME["text"], weight="bold")
        svg.line(x, header_y + 56, x, height - 90, stroke=THEME["line"], stroke_width=2, dash="10 8")

    for src, dst, y, label, dashed in messages:
        start = (participants[src][1], y)
        end = (participants[dst][1], y)
        if dashed:
            draw_arrow_png(draw, start, end, THEME["line"], width=2, dash=(10, 8))
            draw_arrow_svg(svg, start, end, THEME["line"], width=2, dash="10 8")
        else:
            draw_arrow_png(draw, start, end, THEME["line"], width=2)
            draw_arrow_svg(svg, start, end, THEME["line"], width=2)
        label_x = (start[0] + end[0]) // 2
        label_on_line_png(draw, (label_x, y - 18), label, SMALL_FONT)
        svg.rect(label_x - 96, y - 38, 192, 26, fill=THEME["page"], stroke=THEME["page"], stroke_width=0, rx=6)
        svg.text(label_x, y - 20, label, 16, THEME["text"])

    # 业务服务类自处理步骤
    self_x = participants[3][1]
    top_y = 560
    bottom_y = 640
    self_path = [(self_x, top_y), (self_x + 90, top_y), (self_x + 90, bottom_y), (self_x, bottom_y)]
    draw.line(self_path, fill=THEME["line"], width=2)
    draw.polygon(arrow_head((self_x + 90, bottom_y), (self_x, bottom_y)), fill=THEME["line"])
    label_on_line_png(draw, (self_x + 110, 600), "执行业务规则", SMALL_FONT)

    svg.polyline(self_path, THEME["line"], 2)
    svg.polygon(arrow_head((self_x + 90, bottom_y), (self_x, bottom_y)), fill=THEME["line"])
    svg.rect(self_x + 30, 580, 160, 26, fill=THEME["page"], stroke=THEME["page"], stroke_width=0, rx=6)
    svg.text(self_x + 110, 598, "执行业务规则", 16, THEME["text"])

    out_png = OUT_DIR / "architecture-sequence-diagram.png"
    out_svg = OUT_DIR / "architecture-sequence-diagram.svg"
    save_png(img, out_png)
    save_svg(svg, out_svg)


def generate_system_module_diagram() -> None:
    width, height = 1800, 980
    title = "校园失物招领管理系统功能模块图"
    root = ("校园失物招领管理系统", width // 2, 120, 360, 68)
    modules = [
        "注册登录",
        "个人信息管理",
        "用户信息管理",
        "管理员信息管理",
        "公告信息管理",
        "物品类别管理",
        "失物广场管理",
        "招领广场管理",
        "我的收藏",
        "聊天记录管理",
        "平台建议",
    ]
    positions = [
        ("注册登录", 120, 360),
        ("个人信息管理", 400, 360),
        ("用户信息管理", 680, 360),
        ("管理员信息管理", 960, 360),
        ("公告信息管理", 1240, 360),
        ("物品类别管理", 1520, 360),
        ("失物广场管理", 260, 580),
        ("招领广场管理", 540, 580),
        ("我的收藏", 820, 580),
        ("聊天记录管理", 1100, 580),
        ("平台建议", 1380, 580),
    ]

    img = Image.new("RGB", (width, height), THEME["page"])
    draw = ImageDraw.Draw(img)
    svg = SvgCanvas(width, height, THEME["page"])

    draw.text((width / 2 - 260, 36), title, font=TITLE_FONT, fill=THEME["text"])
    svg.text(width // 2, 72, title, 34, THEME["text"], weight="bold")

    rx, ry, rw, rh = root[1], root[2], root[3], root[4]
    rounded_box_png(draw, (rx - rw // 2, ry, rx + rw // 2, ry + rh), THEME["accent"], THEME["line"], shadow=True)
    draw_multiline_centered(draw, (rx - rw // 2 + 8, ry + 4, rx + rw // 2 - 8, ry + rh - 4), root[0], TEXT_FONT, "#FFFFFF")
    svg.rect(rx - rw // 2 + 4, ry + 4, rw, rh, fill=THEME["shadow"], stroke=THEME["shadow"], stroke_width=0, rx=12)
    svg.rect(rx - rw // 2, ry, rw, rh, fill=THEME["accent"], stroke=THEME["line"], stroke_width=2, rx=12)
    svg.text(rx, ry + 44, root[0], 24, "#FFFFFF", weight="bold")

    bus_y = 260
    draw.line((rx, ry + rh, rx, bus_y), fill=THEME["line"], width=2)
    xs = [item[1] for item in positions]
    draw.line((min(xs), bus_y, max(xs), bus_y), fill=THEME["line"], width=2)
    svg.line(rx, ry + rh, rx, bus_y, THEME["line"], 2)
    svg.line(min(xs), bus_y, max(xs), bus_y, THEME["line"], 2)

    for label, x, y in positions:
        box = (x - 96, y - 28, x + 96, y + 28)
        draw.line((x, bus_y, x, y - 28), fill=THEME["line"], width=2)
        rounded_box_png(draw, box, THEME["accent_light"], THEME["line"], shadow=True)
        draw_multiline_centered(draw, box, label, SMALL_FONT, THEME["text"])

        svg.line(x, bus_y, x, y - 28, THEME["line"], 2)
        svg.rect(x - 96 + 4, y - 28 + 4, 192, 56, fill=THEME["shadow"], stroke=THEME["shadow"], stroke_width=0, rx=10)
        svg.rect(x - 96, y - 28, 192, 56, fill=THEME["accent_light"], stroke=THEME["line"], stroke_width=2, rx=10)
        svg.text(x, y + 8, label, 18, THEME["text"], weight="bold")

    out_png = OUT_DIR / "system-module-diagram.png"
    out_svg = OUT_DIR / "system-module-diagram.svg"
    save_png(img, out_png)
    save_svg(svg, out_svg)


def generate_mini_program_module_diagram() -> None:
    width, height = 1900, 980
    title = "微信小程序功能模块图"
    img = Image.new("RGB", (width, height), THEME["page"])
    draw = ImageDraw.Draw(img)
    svg = SvgCanvas(width, height, THEME["page"])

    draw.text((width / 2 - 170, 36), title, font=TITLE_FONT, fill=THEME["text"])
    svg.text(width // 2, 72, title, 34, THEME["text"], weight="bold")

    root_x, root_y, root_w, root_h = width // 2, 120, 360, 68
    rounded_box_png(draw, (root_x - root_w // 2, root_y, root_x + root_w // 2, root_y + root_h), THEME["accent"], THEME["line"], shadow=True)
    draw_multiline_centered(draw, (root_x - root_w // 2, root_y, root_x + root_w // 2, root_y + root_h), "校园失物招领管理系统", TEXT_FONT, "#FFFFFF")
    svg.rect(root_x - root_w // 2 + 4, root_y + 4, root_w, root_h, fill=THEME["shadow"], stroke=THEME["shadow"], stroke_width=0, rx=12)
    svg.rect(root_x - root_w // 2, root_y, root_w, root_h, fill=THEME["accent"], stroke=THEME["line"], stroke_width=2, rx=12)
    svg.text(root_x, root_y + 44, "校园失物招领管理系统", 24, "#FFFFFF", weight="bold")

    lvl1 = [
        ("注册登录", 300, 320),
        ("首页", 950, 320),
        ("我的", 1550, 320),
    ]
    bus_y = 250
    draw.line((root_x, root_y + root_h, root_x, bus_y), fill=THEME["line"], width=2)
    draw.line((lvl1[0][1], bus_y, lvl1[-1][1], bus_y), fill=THEME["line"], width=2)
    svg.line(root_x, root_y + root_h, root_x, bus_y, THEME["line"], 2)
    svg.line(lvl1[0][1], bus_y, lvl1[-1][1], bus_y, THEME["line"], 2)

    for label, x, y in lvl1:
        draw.line((x, bus_y, x, y - 30), fill=THEME["line"], width=2)
        rounded_box_png(draw, (x - 104, y - 30, x + 104, y + 30), THEME["accent_light"], THEME["line"], shadow=True)
        draw_multiline_centered(draw, (x - 104, y - 30, x + 104, y + 30), label, SMALL_FONT, THEME["text"])

        svg.line(x, bus_y, x, y - 30, THEME["line"], 2)
        svg.rect(x - 104 + 4, y - 30 + 4, 208, 60, fill=THEME["shadow"], stroke=THEME["shadow"], stroke_width=0, rx=10)
        svg.rect(x - 104, y - 30, 208, 60, fill=THEME["accent_light"], stroke=THEME["line"], stroke_width=2, rx=10)
        svg.text(x, y + 8, label, 18, THEME["text"], weight="bold")

    home_children = [("失物广场", 800, 620), ("招领广场", 1060, 620)]
    mine_children = [("我的收藏", 1280, 620), ("平台建议", 1440, 620), ("个人信息管理", 1600, 620), ("聊天中心", 1760, 620)]

    for parent_x, children in [(950, home_children), (1550, mine_children)]:
        sub_bus_y = 500
        draw.line((parent_x, 350, parent_x, sub_bus_y), fill=THEME["line"], width=2)
        draw.line((children[0][1], sub_bus_y, children[-1][1], sub_bus_y), fill=THEME["line"], width=2)
        svg.line(parent_x, 350, parent_x, sub_bus_y, THEME["line"], 2)
        svg.line(children[0][1], sub_bus_y, children[-1][1], sub_bus_y, THEME["line"], 2)
        for label, x, y in children:
            draw.line((x, sub_bus_y, x, y - 30), fill=THEME["line"], width=2)
            rounded_box_png(draw, (x - 84, y - 30, x + 84, y + 30), THEME["accent_mid"], THEME["line"], shadow=True)
            draw_multiline_centered(draw, (x - 84, y - 30, x + 84, y + 30), label, SMALL_FONT, THEME["text"])

            svg.line(x, sub_bus_y, x, y - 30, THEME["line"], 2)
            svg.rect(x - 84 + 4, y - 30 + 4, 168, 60, fill=THEME["shadow"], stroke=THEME["shadow"], stroke_width=0, rx=10)
            svg.rect(x - 84, y - 30, 168, 60, fill=THEME["accent_mid"], stroke=THEME["line"], stroke_width=2, rx=10)
            svg.text(x, y + 8, label, 18, THEME["text"], weight="bold")

    out_png = OUT_DIR / "mini-program-module-diagram.png"
    out_svg = OUT_DIR / "mini-program-module-diagram.svg"
    save_png(img, out_png)
    save_svg(svg, out_svg)


def generate_class_diagram() -> None:
    width, height = 1900, 1420
    title = "校园失物招领管理系统类图"
    boxes = {
        "收藏": ClassBox("收藏", ["- 收藏编号", "- 用户编号", "- 物品编号"], ["+ 增加收藏信息()", "+ 删除收藏信息()"], 80, 120),
        "平台建议": ClassBox("平台建议", ["- 建议编号", "- 用户编号", "- 管理员编号", "- 标题", "- 回复"], ["+ 提交建议()", "+ 回复建议()"], 520, 120),
        "公告信息": ClassBox("公告信息", ["- 公告编号", "- 标题", "- 内容"], ["+ 增加公告信息()", "+ 查看公告信息()", "+ 修改公告信息()", "+ 删除公告信息()"], 1120, 140),
        "聊天记录": ClassBox("聊天记录", ["- 聊天编号", "- 用户账号", "- 好友账号"], ["+ 消息记录()", "+ 消息回复()"], 80, 510),
        "用户": ClassBox("用户", ["- 用户编号", "- 账号", "- 密码", "- 姓名"], ["+ 增加用户信息()", "+ 查看用户信息()", "+ 修改用户信息()", "+ 删除用户信息()"], 520, 450),
        "管理员": ClassBox("管理员", ["- 管理员编号", "- 用户名", "- 密码", "- 角色"], ["+ 增加管理员信息()", "+ 查看管理员信息()", "+ 修改管理员信息()", "+ 删除管理员信息()"], 1120, 450),
        "物品类别": ClassBox("物品类别", ["- 类别序号", "- 物品类别", "- 创建时间"], ["+ 增加物品类别信息()", "+ 查看物品类别信息()", "+ 修改物品类别信息()", "+ 删除物品类别信息()"], 1520, 450),
        "好友": ClassBox("好友", ["- 好友编号", "- 用户编号"], ["+ 增加好友()"], 80, 980),
        "失物广场": ClassBox("失物广场", ["- 物品编号", "- 物品名称", "- 物品类别", "- 物品图片", "- 账号", "- 姓名", "- 手机号"], ["+ 增加物品信息()", "+ 查看物品信息()", "+ 修改物品信息()", "+ 删除物品信息()"], 520, 920),
        "招领广场": ClassBox("招领广场", ["- 物品编号", "- 物品名称", "- 物品类别", "- 物品图片", "- 账号", "- 姓名", "- 手机号"], ["+ 增加物品信息()", "+ 查看物品信息()", "+ 修改物品信息()", "+ 删除物品信息()"], 1120, 920),
    }

    img = Image.new("RGB", (width, height), THEME["page"])
    draw = ImageDraw.Draw(img)
    svg = SvgCanvas(width, height, THEME["page"])

    draw.text((width / 2 - 230, 34), title, font=TITLE_FONT, fill=THEME["text"])
    svg.text(width // 2, 72, title, 34, THEME["text"], weight="bold")

    for box in boxes.values():
        draw_class_box_png(draw, box)
        draw_class_box_svg(svg, box)

    def connect(
        a: str,
        b: str,
        start_side: str,
        end_side: str,
        label_a: str = "",
        label_b: str = "",
        via: list[tuple[int, int]] | None = None,
    ) -> None:
        box_a = boxes[a]
        box_b = boxes[b]

        def anchor(box: ClassBox, side: str) -> tuple[int, int]:
            if side == "top":
                return (box.x + box.w // 2, box.y)
            if side == "bottom":
                return (box.x + box.w // 2, box.y + box.h)
            if side == "left":
                return (box.x, box.y + box.h // 2)
            return (box.x + box.w, box.y + box.h // 2)

        start = anchor(box_a, start_side)
        end = anchor(box_b, end_side)
        points = [start] + (via or []) + [end]

        draw.line(points, fill=THEME["line"], width=2)
        svg.polyline(points, THEME["line"], 2)

        if label_a:
            px, py = points[0]
            label_on_line_png(draw, (px - 18 if start_side == "left" else px + 18, py - 16), label_a, TINY_FONT)
            svg.rect((px - 36 if start_side == "left" else px), py - 28, 36, 20, fill=THEME["page"], stroke=THEME["page"], stroke_width=0, rx=4)
            svg.text(px - 18 if start_side == "left" else px + 18, py - 12, label_a, 14, THEME["text"])
        if label_b:
            px, py = points[-1]
            label_on_line_png(draw, (px - 18 if end_side == "left" else px + 18, py - 16), label_b, TINY_FONT)
            svg.rect((px - 36 if end_side == "left" else px), py - 28, 36, 20, fill=THEME["page"], stroke=THEME["page"], stroke_width=0, rx=4)
            svg.text(px - 18 if end_side == "left" else px + 18, py - 12, label_b, 14, THEME["text"])

    connect("收藏", "用户", "bottom", "top", "n", "1", [(210, 300), (650, 300)])
    connect("平台建议", "用户", "bottom", "top", "n", "1", [(650, 340)])
    connect("平台建议", "管理员", "bottom", "top", "n", "1", [(1180, 340)])
    connect("公告信息", "管理员", "bottom", "top", "n", "1", [(1250, 360), (1250, 450)])
    connect("聊天记录", "用户", "right", "left", "n", "1", [(470, 650)])
    connect("聊天记录", "好友", "bottom", "top", "n", "1", [(210, 860)])
    connect("好友", "用户", "right", "left", "n", "1", [(470, 1060), (470, 650)])
    connect("用户", "失物广场", "bottom", "top", "1", "n")
    connect("用户", "招领广场", "bottom", "top", "1", "n", [(780, 840), (1250, 840)])
    connect("管理员", "公告信息", "top", "bottom", "1", "n", [(1250, 360)])
    connect("管理员", "物品类别", "right", "left", "1", "n", [(1470, 650)])
    connect("物品类别", "失物广场", "bottom", "right", "1", "n", [(1650, 840), (950, 840), (950, 1080)])
    connect("物品类别", "招领广场", "bottom", "top", "1", "n", [(1650, 840), (1250, 840)])
    connect("管理员", "失物广场", "bottom", "top", "1", "n", [(1250, 840), (650, 840)])
    connect("管理员", "招领广场", "bottom", "top", "1", "n")
    connect("收藏", "失物广场", "bottom", "left", "n", "1", [(210, 840), (520, 840), (520, 1080)])

    out_png = OUT_DIR / "system-class-diagram.png"
    out_svg = OUT_DIR / "system-class-diagram.svg"
    save_png(img, out_png)
    save_svg(svg, out_svg)


def generate_preview_page() -> None:
    html_text = """<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8">
  <title>Visio Diagrams Preview</title>
  <style>
    body { font-family: "Microsoft YaHei", sans-serif; margin: 24px; background: #f4f7fb; color: #1f2a3a; }
    h1 { margin-bottom: 8px; }
    .grid { display: grid; grid-template-columns: 1fr; gap: 24px; }
    .panel { background: #fff; border: 1px solid #d6e1f5; border-radius: 10px; padding: 16px; box-shadow: 0 4px 16px rgba(84,101,127,0.08); }
    img { width: 100%; height: auto; display: block; }
    .files { font-size: 14px; color: #54657f; margin-top: 8px; }
  </style>
</head>
<body>
  <h1>重绘图预览</h1>
  <div class="grid">
    <div class="panel"><h2>顺序图</h2><img src="repair-sequence-diagram.svg" alt="顺序图"><div class="files">repair-sequence-diagram.svg / repair-sequence-diagram.png</div></div>
    <div class="panel"><h2>系统架构类顺序图</h2><img src="architecture-sequence-diagram.svg" alt="系统架构类顺序图"><div class="files">architecture-sequence-diagram.svg / architecture-sequence-diagram.png</div></div>
    <div class="panel"><h2>系统类图</h2><img src="system-class-diagram.svg" alt="系统类图"><div class="files">system-class-diagram.svg / system-class-diagram.png</div></div>
    <div class="panel"><h2>系统功能模块图</h2><img src="system-module-diagram.svg" alt="系统功能模块图"><div class="files">system-module-diagram.svg / system-module-diagram.png</div></div>
    <div class="panel"><h2>微信小程序功能模块图</h2><img src="mini-program-module-diagram.svg" alt="微信小程序功能模块图"><div class="files">mini-program-module-diagram.svg / mini-program-module-diagram.png</div></div>
  </div>
</body>
</html>
"""
    (OUT_DIR / "preview.html").write_text(html_text, encoding="utf-8")


def main() -> None:
    ensure_dir(OUT_DIR)
    generate_sequence_diagram()
    generate_architecture_sequence_diagram()
    generate_class_diagram()
    generate_system_module_diagram()
    generate_mini_program_module_diagram()
    generate_preview_page()
    print(f"generated: {OUT_DIR}")


if __name__ == "__main__":
    main()
