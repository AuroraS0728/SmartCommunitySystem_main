from __future__ import annotations

import html
from dataclasses import dataclass
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "visio-diagrams"
FONT_FILE = r"C:\Windows\Fonts\simsun.ttc"
TITLE_FONT = ImageFont.truetype(FONT_FILE, 34)
NAME_FONT = ImageFont.truetype(FONT_FILE, 18)
ATTR_FONT = ImageFont.truetype(FONT_FILE, 16)
MULT_FONT = ImageFont.truetype(FONT_FILE, 18)

BLACK = "#000000"
WHITE = "#FFFFFF"


class SvgCanvas:
    def __init__(self, width: int, height: int) -> None:
        self.width = width
        self.height = height
        self.elements: list[str] = [f'<rect x="0" y="0" width="{width}" height="{height}" fill="{WHITE}" />']

    def rect(self, x: int, y: int, w: int, h: int, stroke_width: int = 2) -> None:
        self.elements.append(
            f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{WHITE}" stroke="{BLACK}" stroke-width="{stroke_width}" />'
        )

    def line(self, x1: int, y1: int, x2: int, y2: int, stroke_width: int = 2) -> None:
        self.elements.append(
            f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{BLACK}" stroke-width="{stroke_width}" />'
        )

    def polyline(self, points: list[tuple[int, int]], stroke_width: int = 2) -> None:
        pts = " ".join(f"{x},{y}" for x, y in points)
        self.elements.append(
            f'<polyline points="{pts}" fill="none" stroke="{BLACK}" stroke-width="{stroke_width}" />'
        )

    def text(self, x: int, y: int, text: str, font_size: int, anchor: str = "middle") -> None:
        lines = text.split("\n")
        if len(lines) == 1:
            self.elements.append(
                f'<text x="{x}" y="{y}" text-anchor="{anchor}" font-family="SimSun" font-size="{font_size}" fill="{BLACK}">{html.escape(text)}</text>'
            )
            return
        line_h = int(font_size * 1.35)
        base = y - (len(lines) - 1) * line_h / 2
        spans: list[str] = []
        for i, line in enumerate(lines):
            dy = 0 if i == 0 else line_h
            spans.append(f'<tspan x="{x}" dy="{dy}">{html.escape(line)}</tspan>')
        self.elements.append(
            f'<text x="{x}" y="{base}" text-anchor="{anchor}" font-family="SimSun" font-size="{font_size}" fill="{BLACK}">{"".join(spans)}</text>'
        )

    def save(self, path: Path) -> None:
        path.write_text(
            f'<svg xmlns="http://www.w3.org/2000/svg" width="{self.width}" height="{self.height}" viewBox="0 0 {self.width} {self.height}">\n  '
            + "\n  ".join(self.elements)
            + "\n</svg>\n",
            encoding="utf-8",
        )


@dataclass
class ClassBox:
    name: str
    attrs: list[str]
    x: int
    y: int
    w: int = 260
    title_h: int = 44

    @property
    def h(self) -> int:
        return self.title_h + 20 + len(self.attrs) * 28

    @property
    def left(self) -> int:
        return self.x

    @property
    def right(self) -> int:
        return self.x + self.w

    @property
    def top(self) -> int:
        return self.y

    @property
    def bottom(self) -> int:
        return self.y + self.h

    @property
    def cx(self) -> int:
        return self.x + self.w // 2

    @property
    def cy(self) -> int:
        return self.y + self.h // 2


def multiline_size(draw: ImageDraw.ImageDraw, text: str, font: ImageFont.FreeTypeFont) -> tuple[int, int]:
    l, t, r, b = draw.multiline_textbbox((0, 0), text, font=font, spacing=4, align="center")
    return r - l, b - t


def draw_center_text(
    draw: ImageDraw.ImageDraw,
    box: tuple[int, int, int, int],
    text: str,
    font: ImageFont.FreeTypeFont,
) -> None:
    w, h = multiline_size(draw, text, font)
    x1, y1, x2, y2 = box
    draw.multiline_text(((x1 + x2 - w) / 2, (y1 + y2 - h) / 2), text, font=font, fill=BLACK, spacing=4, align="center")


def draw_class_box(draw: ImageDraw.ImageDraw, svg: SvgCanvas, box: ClassBox) -> None:
    draw.rectangle((box.left, box.top, box.right, box.bottom), fill=WHITE, outline=BLACK, width=2)
    draw.line((box.left, box.top + box.title_h, box.right, box.top + box.title_h), fill=BLACK, width=2)
    draw_center_text(draw, (box.left + 8, box.top + 4, box.right - 8, box.top + box.title_h - 4), box.name, NAME_FONT)
    svg.rect(box.left, box.top, box.w, box.h)
    svg.line(box.left, box.top + box.title_h, box.right, box.top + box.title_h)
    svg.text(box.cx, box.top + 28, box.name, 18)

    y = box.top + box.title_h + 14
    for attr in box.attrs:
        draw.text((box.left + 14, y), attr, font=ATTR_FONT, fill=BLACK)
        svg.text(box.left + 14, y + 15, attr, 16, anchor="start")
        y += 28


def draw_polyline(draw: ImageDraw.ImageDraw, svg: SvgCanvas, points: list[tuple[int, int]]) -> None:
    draw.line(points, fill=BLACK, width=2)
    svg.polyline(points)


def relation_label(draw: ImageDraw.ImageDraw, svg: SvgCanvas, x: int, y: int, text: str) -> None:
    draw.text((x, y), text, font=MULT_FONT, fill=BLACK)
    svg.text(x + 6, y + 16, text, 18, anchor="start")


def main() -> None:
    width, height = 1760, 1080
    img = Image.new("RGB", (width, height), WHITE)
    draw = ImageDraw.Draw(img)
    svg = SvgCanvas(width, height)

    title = "智慧社区领域对象类图"
    draw.text((width // 2 - 170, 24), title, font=TITLE_FONT, fill=BLACK)
    svg.text(width // 2, 58, title, 34)

    boxes = {
        "user": ClassBox("用户", ["- 编号", "- 角色", "- 昵称", "- 手机号"], 80, 140),
        "property": ClassBox("房产", ["- 编号", "- 小区", "- 楼栋", "- 房号"], 430, 140),
        "link": ClassBox("用户房产关联", ["- 编号", "- 用户编号", "- 房产编号", "- 关系"], 780, 140),
        "repair": ClassBox("报修工单", ["- 编号", "- 用户编号", "- 房产编号", "- 类别", "- 状态"], 1130, 140),
        "worker": ClassBox("维修派工", ["- 编号", "- 工单编号", "- 维修人员编号", "- 角色类型"], 1430, 140),
        "parking": ClassBox("停车订单", ["- 编号", "- 用户编号", "- 房产编号", "- 车牌号", "- 金额"], 80, 680),
        "bill": ClassBox("物业账单", ["- 编号", "- 房产编号", "- 账期", "- 金额", "- 状态"], 500, 680),
        "invite": ClassBox("访客邀约", ["- 编号", "- 业主编号", "- 访客姓名", "- 失效时间"], 920, 680),
        "complaint": ClassBox("投诉建议", ["- 编号", "- 用户编号", "- 类型", "- 内容", "- 状态"], 1340, 680),
    }

    for box in boxes.values():
        draw_class_box(draw, svg, box)

    user = boxes["user"]
    prop = boxes["property"]
    link = boxes["link"]
    repair = boxes["repair"]
    worker = boxes["worker"]
    parking = boxes["parking"]
    bill = boxes["bill"]
    invite = boxes["invite"]
    complaint = boxes["complaint"]

    # 用户 1 --- * 用户房产关联
    draw_polyline(draw, svg, [(user.right, user.cy), (link.left, user.cy)])
    relation_label(draw, svg, user.right + 6, user.cy - 28, "1")
    relation_label(draw, svg, link.left - 18, user.cy - 28, "*")

    # 房产 1 --- * 用户房产关联
    draw_polyline(draw, svg, [(prop.right, prop.cy), (link.left, prop.cy)])
    relation_label(draw, svg, prop.right + 6, prop.cy - 28, "1")
    relation_label(draw, svg, link.left - 18, prop.cy - 28, "*")

    # 报修工单 1 --- * 维修派工
    draw_polyline(draw, svg, [(repair.right, repair.cy), (worker.left, repair.cy)])
    relation_label(draw, svg, repair.right + 6, repair.cy - 28, "1")
    relation_label(draw, svg, worker.left - 18, repair.cy - 28, "*")

    # 房产 1 --- * 物业账单
    draw_polyline(draw, svg, [(prop.cx, prop.bottom), (prop.cx, 620), (bill.cx, 620), (bill.cx, bill.top)])
    relation_label(draw, svg, prop.cx + 8, prop.bottom + 8, "1")
    relation_label(draw, svg, bill.cx + 8, bill.top - 30, "*")

    # 用户 1 --- * 停车订单
    draw_polyline(draw, svg, [(user.cx, user.bottom), (user.cx, parking.top)])
    relation_label(draw, svg, user.cx + 8, user.bottom + 8, "1")
    relation_label(draw, svg, parking.cx + 8, parking.top - 30, "*")

    # 用户 1 --- * 访客邀约
    draw_polyline(draw, svg, [(user.cx + 28, user.bottom), (user.cx + 28, 560), (invite.cx, 560), (invite.cx, invite.top)])
    relation_label(draw, svg, user.cx + 38, user.bottom + 44, "1")
    relation_label(draw, svg, invite.cx + 8, invite.top - 30, "*")

    # 用户 1 --- * 投诉建议
    draw_polyline(draw, svg, [(user.cx - 28, user.bottom), (user.cx - 28, 520), (complaint.cx, 520), (complaint.cx, complaint.top)])
    relation_label(draw, svg, user.cx - 18, user.bottom + 8, "1")
    relation_label(draw, svg, complaint.cx + 8, complaint.top - 30, "*")

    # 用户 1 --- * 报修工单
    draw_polyline(draw, svg, [(user.cx + 56, user.bottom), (user.cx + 56, 430), (repair.cx, 430), (repair.cx, repair.bottom)])
    relation_label(draw, svg, user.cx + 66, user.bottom + 8, "1")
    relation_label(draw, svg, repair.cx + 8, repair.bottom + 8, "*")

    # 房产 1 --- * 报修工单
    draw_polyline(draw, svg, [(prop.cx + 46, prop.bottom), (prop.cx + 46, 500), (repair.left - 40, 500), (repair.left - 40, repair.cy), (repair.left, repair.cy)])
    relation_label(draw, svg, prop.cx + 56, prop.bottom + 8, "1")
    relation_label(draw, svg, repair.left - 34, repair.cy - 28, "*")

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    png_path = OUT_DIR / "project-domain-class.png"
    svg_path = OUT_DIR / "project-domain-class.svg"
    img.save(png_path)
    svg.save(svg_path)
    print(png_path)
    print(svg_path)


if __name__ == "__main__":
    main()
