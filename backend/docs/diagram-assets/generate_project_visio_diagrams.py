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
TEXT_FONT = ImageFont.truetype(FONT_FILE, 20)
SMALL_FONT = ImageFont.truetype(FONT_FILE, 16)
TINY_FONT = ImageFont.truetype(FONT_FILE, 14)

BLACK = "#000000"
WHITE = "#FFFFFF"


class SvgCanvas:
    def __init__(self, width: int, height: int) -> None:
        self.width = width
        self.height = height
        self.elements: list[str] = [f'<rect x="0" y="0" width="{width}" height="{height}" fill="{WHITE}" />']

    def rect(self, x: int, y: int, w: int, h: int, fill: str = WHITE, stroke: str = BLACK, stroke_width: int = 2, rx: int = 0) -> None:
        self.elements.append(
            f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{rx}" ry="{rx}" fill="{fill}" stroke="{stroke}" stroke-width="{stroke_width}" />'
        )

    def line(self, x1: int, y1: int, x2: int, y2: int, stroke_width: int = 2, dash: str | None = None) -> None:
        dash_attr = f' stroke-dasharray="{dash}"' if dash else ""
        self.elements.append(
            f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{BLACK}" stroke-width="{stroke_width}"{dash_attr} />'
        )

    def polyline(self, points: Sequence[tuple[int, int]], stroke_width: int = 2, dash: str | None = None) -> None:
        dash_attr = f' stroke-dasharray="{dash}"' if dash else ""
        pts = " ".join(f"{x},{y}" for x, y in points)
        self.elements.append(
            f'<polyline points="{pts}" fill="none" stroke="{BLACK}" stroke-width="{stroke_width}"{dash_attr} />'
        )

    def polygon(self, points: Sequence[tuple[int, int]]) -> None:
        pts = " ".join(f"{x},{y}" for x, y in points)
        self.elements.append(f'<polygon points="{pts}" fill="{BLACK}" />')

    def text(self, x: int, y: int, text: str, font_size: int, anchor: str = "middle", family: str = "SimSun") -> None:
        lines = text.split("\n")
        if len(lines) == 1:
            self.elements.append(
                f'<text x="{x}" y="{y}" text-anchor="{anchor}" font-family="{family}" font-size="{font_size}" fill="{BLACK}">{html.escape(text)}</text>'
            )
            return
        line_h = int(font_size * 1.3)
        base = y - (len(lines) - 1) * line_h / 2
        spans = []
        for i, line in enumerate(lines):
            dy = 0 if i == 0 else line_h
            spans.append(f'<tspan x="{x}" dy="{dy}">{html.escape(line)}</tspan>')
        self.elements.append(
            f'<text x="{x}" y="{base}" text-anchor="{anchor}" font-family="{family}" font-size="{font_size}" fill="{BLACK}">{"".join(spans)}</text>'
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


def draw_arrow(draw: ImageDraw.ImageDraw, svg: SvgCanvas, start: tuple[int, int], end: tuple[int, int], dashed: bool = False) -> None:
    if dashed:
        draw.line((start, end), fill=BLACK, width=2)
        svg.line(start[0], start[1], end[0], end[1], 2, "10 8")
    else:
        draw.line((start, end), fill=BLACK, width=2)
        svg.line(start[0], start[1], end[0], end[1], 2)
    head = arrow_head(start, end)
    draw.polygon(head, fill=BLACK)
    svg.polygon(head)


def line_label(draw: ImageDraw.ImageDraw, svg: SvgCanvas, x: int, y: int, text: str) -> None:
    w, h = text_size(draw, text, SMALL_FONT)
    draw.rectangle((x - w / 2 - 4, y - h / 2 - 2, x + w / 2 + 4, y + h / 2 + 2), fill=WHITE)
    draw.multiline_text((x - w / 2, y - h / 2), text, font=SMALL_FONT, fill=BLACK, spacing=4, align="center")
    svg.rect(int(x - w / 2 - 4), int(y - h / 2 - 2), int(w + 8), int(h + 4), fill=WHITE, stroke=WHITE, stroke_width=0)
    svg.text(x, y + 5, text, 16)


def rounded_box(draw: ImageDraw.ImageDraw, svg: SvgCanvas, box: tuple[int, int, int, int], text: str, radius: int = 8) -> None:
    draw.rounded_rectangle(box, radius=radius, fill=WHITE, outline=BLACK, width=2)
    draw_center_text(draw, box, text, SMALL_FONT)
    x1, y1, x2, y2 = box
    svg.rect(x1, y1, x2 - x1, y2 - y1, fill=WHITE, stroke=BLACK, stroke_width=2, rx=radius)
    svg.text((x1 + x2) // 2, (y1 + y2) // 2 + 6, text, 17)


def plain_box(draw: ImageDraw.ImageDraw, svg: SvgCanvas, box: tuple[int, int, int, int], text: str, font: ImageFont.FreeTypeFont = SMALL_FONT) -> None:
    x1, y1, x2, y2 = box
    draw.rectangle(box, fill=WHITE, outline=BLACK, width=2)
    draw_center_text(draw, box, text, font)
    svg.rect(x1, y1, x2 - x1, y2 - y1, fill=WHITE, stroke=BLACK, stroke_width=2)
    svg.text((x1 + x2) // 2, (y1 + y2) // 2 + 6, text, 17 if font == SMALL_FONT else 20)


def vertical_bar(draw: ImageDraw.ImageDraw, svg: SvgCanvas, x: int, y: int, w: int, h: int, text: str, font: ImageFont.FreeTypeFont) -> None:
    draw.rectangle((x - w // 2, y, x + w // 2, y + h), fill=WHITE, outline=BLACK, width=2)
    svg.rect(x - w // 2, y, w, h, fill=WHITE, stroke=BLACK, stroke_width=2)
    vertical_text = "\n".join(text)
    draw_center_text(draw, (x - w // 2 + 4, y + 6, x + w // 2 - 4, y + h - 6), vertical_text, font)
    svg.text(x, y + h // 2 + 5, vertical_text, 16 if font == SMALL_FONT else 14)


@dataclass
class ClassBox:
    name: str
    attrs: list[str]
    x: int
    y: int
    w: int = 250

    @property
    def h(self) -> int:
        return 46 + max(42, len(self.attrs) * 24 + 18)


def draw_class_box(draw: ImageDraw.ImageDraw, svg: SvgCanvas, box: ClassBox) -> None:
    x1, y1, x2, y2 = box.x, box.y, box.x + box.w, box.y + box.h
    draw.rectangle((x1, y1, x2, y2), fill=WHITE, outline=BLACK, width=2)
    draw.line((x1, y1 + 46, x2, y1 + 46), fill=BLACK, width=2)
    draw_center_text(draw, (x1 + 4, y1 + 4, x2 - 4, y1 + 40), box.name, TEXT_FONT)
    y = y1 + 58
    for attr in box.attrs:
        draw.text((x1 + 14, y), attr, font=SMALL_FONT, fill=BLACK)
        y += 24
    svg.rect(x1, y1, box.w, box.h)
    svg.line(x1, y1 + 46, x2, y1 + 46)
    svg.text(x1 + box.w // 2, y1 + 30, box.name, 20)
    y = y1 + 76
    for attr in box.attrs:
        svg.text(x1 + 14, y, attr, 16, anchor="start")
        y += 24


def save_pair(img: Image.Image, svg: SvgCanvas, png_name: str, svg_name: str) -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    img.save(OUT_DIR / png_name)
    svg.save(OUT_DIR / svg_name)


def generate_repair_sequence() -> None:
    width, height = 1800, 980
    title = "报修工单提交流程顺序图"
    parts = [
        ("住户用户", 120),
        ("RepairController", 420),
        ("RepairService", 700),
        ("RepairGradingService", 980),
        ("WorkerRecommendService", 1260),
        ("RepairOrderMapper", 1580),
    ]
    msgs = [
        (0, 1, 180, "1  提交报修"),
        (1, 2, 260, "2  校验参数"),
        (2, 3, 340, "3  计算优先级"),
        (3, 2, 410, "4  返回优先级", True),
        (2, 4, 490, "5  推荐维修人员"),
        (4, 2, 560, "6  返回推荐结果", True),
        (2, 5, 640, "7  保存工单"),
        (5, 2, 710, "8  返回工单编号", True),
        (2, 1, 790, "9  封装响应", True),
        (1, 0, 860, "10  提交成功", True),
    ]
    img = Image.new("RGB", (width, height), WHITE)
    draw = ImageDraw.Draw(img)
    svg = SvgCanvas(width, height)
    draw.text((width // 2 - 170, 30), title, font=TITLE_FONT, fill=BLACK)
    svg.text(width // 2, 58, title, 34)
    header_y = 120
    for label, x in parts:
        rounded_box(draw, svg, (x - 110, header_y, x + 110, header_y + 56), label)
        draw.line((x, header_y + 56, x, height - 70), fill=BLACK, width=2)
        svg.line(x, header_y + 56, x, height - 70)
    for item in msgs:
        src, dst, y, text = item[:4]
        dashed = len(item) > 4 and item[4]
        start = (parts[src][1], y)
        end = (parts[dst][1], y)
        draw_arrow(draw, svg, start, end, dashed)
        line_label(draw, svg, (start[0] + end[0]) // 2, y - 18, text)
    save_pair(img, svg, "project-repair-sequence.png", "project-repair-sequence.svg")


def generate_access_sequence() -> None:
    width, height = 1900, 1000
    title = "系统访问处理顺序图"
    parts = [
        ("微信小程序/管理后台", 170),
        ("JwtAuthInterceptor", 500),
        ("业务Controller", 850),
        ("业务Service", 1200),
        ("Mapper/Repository", 1560),
    ]
    msgs = [
        (0, 1, 180, "1  请求业务接口"),
        (1, 1, 260, "2  解析令牌\n校验身份"),
        (1, 2, 360, "3  放行业务请求"),
        (2, 3, 460, "4  调用业务逻辑"),
        (3, 4, 560, "5  查询/写入数据"),
        (4, 3, 640, "6  返回数据结果", True),
        (3, 2, 730, "7  组装响应", True),
        (2, 0, 840, "8  返回接口结果", True),
    ]
    img = Image.new("RGB", (width, height), WHITE)
    draw = ImageDraw.Draw(img)
    svg = SvgCanvas(width, height)
    draw.text((width // 2 - 150, 30), title, font=TITLE_FONT, fill=BLACK)
    svg.text(width // 2, 58, title, 34)
    header_y = 120
    for label, x in parts:
        rounded_box(draw, svg, (x - 120, header_y, x + 120, header_y + 56), label)
        draw.line((x, header_y + 56, x, height - 70), fill=BLACK, width=2)
        svg.line(x, header_y + 56, x, height - 70, 2, "10 8")
    # self call
    for item in msgs:
        src, dst, y, text = item[:4]
        dashed = len(item) > 4 and item[4]
        if src == dst:
            x = parts[src][1]
            pts = [(x, y), (x + 90, y), (x + 90, y + 60), (x, y + 60)]
            draw.line(pts, fill=BLACK, width=2)
            draw.polygon(arrow_head((x + 90, y + 60), (x, y + 60)), fill=BLACK)
            svg.polyline(pts)
            svg.polygon(arrow_head((x + 90, y + 60), (x, y + 60)))
            line_label(draw, svg, x + 120, y + 28, text)
            continue
        start = (parts[src][1], y)
        end = (parts[dst][1], y)
        draw_arrow(draw, svg, start, end, dashed)
        line_label(draw, svg, (start[0] + end[0]) // 2, y - 18, text)
    save_pair(img, svg, "project-access-sequence.png", "project-access-sequence.svg")


def generate_system_module() -> None:
    width, height = 1700, 980
    title = "智慧社区系统功能模块图"
    modules = [
        "用户认证",
        "房产档案",
        "物业缴费",
        "停车管理",
        "报修工单",
        "维修排班",
        "访客通行",
        "公告通知",
        "投诉建议",
        "积分管理",
        "邻里互动",
        "活动服务",
    ]
    img = Image.new("RGB", (width, height), WHITE)
    draw = ImageDraw.Draw(img)
    svg = SvgCanvas(width, height)
    draw.text((width // 2 - 170, 24), title, font=TITLE_FONT, fill=BLACK)
    svg.text(width // 2, 58, title, 34)

    plain_box(draw, svg, (690, 110, 1010, 168), "智慧社区管理系统")

    bus_y = 250
    top_x1, top_x2 = 140, 1560
    draw.line((top_x1, bus_y, top_x2, bus_y), fill=BLACK, width=2)
    draw.line((850, 168, 850, bus_y), fill=BLACK, width=2)
    svg.line(top_x1, bus_y, top_x2, bus_y)
    svg.line(850, 168, 850, bus_y)

    count = len(modules)
    xs = [140 + round(i * (1420 / (count - 1))) for i in range(count)]
    bar_y = 300
    bar_w = 44
    bar_h = 200
    for label, x in zip(modules, xs):
        draw.line((x, bus_y, x, bar_y), fill=BLACK, width=2)
        svg.line(x, bus_y, x, bar_y)
        vertical_bar(draw, svg, x, bar_y, bar_w, bar_h, label, SMALL_FONT)
    save_pair(img, svg, "project-system-module.png", "project-system-module.svg")




def generate_mini_program_module() -> None:
    width, height = 1500, 980
    title = "智慧社区微信小程序功能模块图"
    img = Image.new("RGB", (width, height), WHITE)
    draw = ImageDraw.Draw(img)
    svg = SvgCanvas(width, height)
    draw.text((width // 2 - 220, 24), title, font=TITLE_FONT, fill=BLACK)
    svg.text(width // 2, 58, title, 34)
    plain_box(draw, svg, (590, 110, 910, 168), "智慧社区微信小程序")

    lvl1 = [("注册登录", 220), ("首页", 750), ("我的", 1280)]
    bus_y = 260
    lvl1_y = 300
    lvl1_w = 44
    lvl1_h = 180
    draw.line((lvl1[0][1], bus_y, lvl1[-1][1], bus_y), fill=BLACK, width=2)
    draw.line((750, 168, 750, bus_y), fill=BLACK, width=2)
    svg.line(lvl1[0][1], bus_y, lvl1[-1][1], bus_y)
    svg.line(750, 168, 750, bus_y)
    for label, x in lvl1:
        draw.line((x, bus_y, x, lvl1_y), fill=BLACK, width=2)
        svg.line(x, bus_y, x, lvl1_y)
        vertical_bar(draw, svg, x, lvl1_y, lvl1_w, lvl1_h, label, SMALL_FONT)

    home_children = [("公告通知", 610), ("我的房产", 705), ("物业缴费", 800), ("社区活动", 895)]
    home_bus_y = 610
    child_y = 650
    child_w = 42
    child_h = 150
    home_center_x = 750
    draw.line((home_center_x, lvl1_y + lvl1_h, home_center_x, home_bus_y), fill=BLACK, width=2)
    draw.line((home_children[0][1], home_bus_y, home_children[-1][1], home_bus_y), fill=BLACK, width=2)
    svg.line(home_center_x, lvl1_y + lvl1_h, home_center_x, home_bus_y)
    svg.line(home_children[0][1], home_bus_y, home_children[-1][1], home_bus_y)
    for label, x in home_children:
        draw.line((x, home_bus_y, x, child_y), fill=BLACK, width=2)
        svg.line(x, home_bus_y, x, child_y)
        vertical_bar(draw, svg, x, child_y, child_w, child_h, label, TINY_FONT)

    mine_children = [("停车管理", 1035), ("报修服务", 1120), ("访客通行", 1205), ("个人资料", 1290), ("投诉建议", 1375), ("积分记录", 1460)]
    mine_bus_y = 610
    mine_center_x = 1280
    draw.line((mine_center_x, lvl1_y + lvl1_h, mine_center_x, mine_bus_y), fill=BLACK, width=2)
    draw.line((mine_children[0][1], mine_bus_y, mine_children[-1][1], mine_bus_y), fill=BLACK, width=2)
    svg.line(mine_center_x, lvl1_y + lvl1_h, mine_center_x, mine_bus_y)
    svg.line(mine_children[0][1], mine_bus_y, mine_children[-1][1], mine_bus_y)
    for label, x in mine_children:
        draw.line((x, mine_bus_y, x, child_y), fill=BLACK, width=2)
        svg.line(x, mine_bus_y, x, child_y)
        vertical_bar(draw, svg, x, child_y, child_w, child_h, label, TINY_FONT)
    save_pair(img, svg, "project-mini-program-module.png", "project-mini-program-module.svg")




def generate_core_class() -> None:
    width, height = 1900, 1250
    title = "智慧社区核心类图"
    img = Image.new("RGB", (width, height), WHITE)
    draw = ImageDraw.Draw(img)
    svg = SvgCanvas(width, height)
    draw.text((width // 2 - 120, 28), title, font=TITLE_FONT, fill=BLACK)
    svg.text(width // 2, 58, title, 34)

    boxes = {
        "用户表": ClassBox("用户表", ["- id", "- role", "- nickname", "- phone"], 140, 170),
        "房产表": ClassBox("房产表", ["- id", "- community", "- building", "- room"], 440, 170),
        "用户房产关联表": ClassBox("用户房产关联表", ["- user_id", "- property_id", "- relation"], 740, 170),
        "维修工单表": ClassBox("维修工单表", ["- id", "- user_id", "- property_id", "- category", "- status"], 1080, 170),
        "维修派工表": ClassBox("维修派工表", ["- order_id", "- worker_id", "- role_type"], 1410, 170),
        "停车订单表": ClassBox("停车订单表", ["- id", "- user_id", "- property_id", "- vehicle_no"], 120, 650),
        "访客邀请表": ClassBox("访客邀请表", ["- id", "- host_user_id", "- visitor_name", "- expire_time"], 470, 650),
        "访客通知表": ClassBox("访客通知表", ["- id", "- host_user_id", "- invite_id", "- read_flag"], 800, 650),
        "二手物品表": ClassBox("二手物品表", ["- id", "- user_id", "- title", "- price"], 1130, 650),
        "失物招领表": ClassBox("失物招领表", ["- id", "- user_id", "- title", "- type"], 1430, 650),
        "论坛帖子表": ClassBox("论坛帖子表", ["- id", "- user_id", "- title", "- board"], 1730, 650),
        "论坛评论表": ClassBox("论坛评论表", ["- id", "- post_id", "- user_id", "- parent_id"], 1580, 980),
    }
    for box in boxes.values():
        draw_class_box(draw, svg, box)

    def anchor(box: ClassBox, side: str) -> tuple[int, int]:
        if side == "top":
            return box.x + box.w // 2, box.y
        if side == "bottom":
            return box.x + box.w // 2, box.y + box.h
        if side == "left":
            return box.x, box.y + box.h // 2
        return box.x + box.w, box.y + box.h // 2

    def connect(a: str, b: str, side_a: str, side_b: str, via: list[tuple[int, int]] | None = None, la: str = "", lb: str = "") -> None:
        sa = anchor(boxes[a], side_a)
        sb = anchor(boxes[b], side_b)
        pts = [sa] + (via or []) + [sb]
        draw.line(pts, fill=BLACK, width=2)
        svg.polyline(pts)
        if la:
            draw.text((pts[0][0] + 6, pts[0][1] - 18), la, font=TINY_FONT, fill=BLACK)
            svg.text(pts[0][0] + 10, pts[0][1] - 4, la, 14, anchor="start")
        if lb:
            draw.text((pts[-1][0] - 18, pts[-1][1] - 18), lb, font=TINY_FONT, fill=BLACK)
            svg.text(pts[-1][0] - 8, pts[-1][1] - 4, lb, 14, anchor="start")

    connect("用户表", "用户房产关联表", "right", "left", la="1", lb="n")
    connect("房产表", "用户房产关联表", "right", "left", la="1", lb="n")
    connect("用户表", "维修工单表", "right", "left", via=[(390, 270), (1080, 270)], la="1", lb="n")
    connect("房产表", "维修工单表", "right", "left", via=[(690, 320), (1080, 320)], la="1", lb="n")
    connect("维修工单表", "维修派工表", "right", "left", la="1", lb="n")
    connect("用户表", "停车订单表", "bottom", "top", via=[(265, 530), (245, 530), (245, 650)], la="1", lb="n")
    connect("用户表", "访客邀请表", "bottom", "top", via=[(285, 580), (595, 580), (595, 650)], la="1", lb="n")
    connect("访客邀请表", "访客通知表", "right", "left", la="1", lb="n")
    connect("用户表", "二手物品表", "bottom", "top", via=[(315, 630), (1255, 630), (1255, 650)], la="1", lb="n")
    connect("用户表", "失物招领表", "bottom", "top", via=[(335, 680), (1555, 680), (1555, 650)], la="1", lb="n")
    connect("用户表", "论坛帖子表", "bottom", "left", via=[(355, 730), (1730, 730)], la="1", lb="n")
    connect("论坛帖子表", "论坛评论表", "bottom", "top", la="1", lb="n")

    save_pair(img, svg, "project-core-class.png", "project-core-class.svg")


def generate_preview() -> None:
    html_text = """<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8">
  <title>Project Diagrams Preview</title>
  <style>
    body { font-family: SimSun, sans-serif; margin: 24px; background: #fff; color: #000; }
    .panel { margin-bottom: 20px; }
    img { width: 100%; height: auto; border: 1px solid #ddd; }
  </style>
</head>
<body>
  <div class="panel"><h2>报修工单提交流程顺序图</h2><img src="project-repair-sequence.svg"></div>
  <div class="panel"><h2>系统访问处理顺序图</h2><img src="project-access-sequence.svg"></div>
  <div class="panel"><h2>智慧社区系统功能模块图</h2><img src="project-system-module.svg"></div>
  <div class="panel"><h2>智慧社区微信小程序功能模块图</h2><img src="project-mini-program-module.svg"></div>
  <div class="panel"><h2>智慧社区核心类图</h2><img src="project-core-class.svg"></div>
</body>
</html>
"""
    (OUT_DIR / "project-preview.html").write_text(html_text, encoding="utf-8")


def main() -> None:
    generate_repair_sequence()
    generate_access_sequence()
    generate_system_module()
    generate_mini_program_module()
    generate_core_class()
    generate_preview()
    print(OUT_DIR)


if __name__ == "__main__":
    main()
