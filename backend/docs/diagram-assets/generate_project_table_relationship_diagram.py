from __future__ import annotations

from pathlib import Path
import html

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "visio-diagrams"
FONT_FILE = r"C:\Windows\Fonts\simsun.ttc"
TITLE_FONT = ImageFont.truetype(FONT_FILE, 32)
TEXT_FONT = ImageFont.truetype(FONT_FILE, 18)
SMALL_FONT = ImageFont.truetype(FONT_FILE, 15)

BLACK = "#000000"
WHITE = "#FFFFFF"


class SvgCanvas:
    def __init__(self, width: int, height: int) -> None:
        self.width = width
        self.height = height
        self.elements = [f'<rect x="0" y="0" width="{width}" height="{height}" fill="{WHITE}" />']

    def rect(self, x: int, y: int, w: int, h: int, stroke_width: int = 2) -> None:
        self.elements.append(
            f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{WHITE}" stroke="{BLACK}" stroke-width="{stroke_width}" />'
        )

    def line(self, x1: int, y1: int, x2: int, y2: int, stroke_width: int = 2) -> None:
        self.elements.append(
            f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{BLACK}" stroke-width="{stroke_width}" />'
        )

    def polyline(self, pts: list[tuple[int, int]], stroke_width: int = 2) -> None:
        points = " ".join(f"{x},{y}" for x, y in pts)
        self.elements.append(
            f'<polyline points="{points}" fill="none" stroke="{BLACK}" stroke-width="{stroke_width}" />'
        )

    def text(self, x: int, y: int, content: str, size: int, anchor: str = "middle", family: str = "SimSun") -> None:
        lines = content.split("\n")
        if len(lines) == 1:
            self.elements.append(
                f'<text x="{x}" y="{y}" text-anchor="{anchor}" font-family="{family}" font-size="{size}" fill="{BLACK}">{html.escape(content)}</text>'
            )
            return
        line_h = int(size * 1.3)
        base = y - (len(lines) - 1) * line_h / 2
        spans = []
        for i, line in enumerate(lines):
            dy = 0 if i == 0 else line_h
            spans.append(f'<tspan x="{x}" dy="{dy}">{html.escape(line)}</tspan>')
        self.elements.append(
            f'<text x="{x}" y="{base}" text-anchor="{anchor}" font-family="{family}" font-size="{size}" fill="{BLACK}">{"".join(spans)}</text>'
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


def center_text(draw: ImageDraw.ImageDraw, box: tuple[int, int, int, int], text: str, font: ImageFont.FreeTypeFont) -> None:
    w, h = text_size(draw, text, font)
    x1, y1, x2, y2 = box
    draw.multiline_text(((x1 + x2 - w) / 2, (y1 + y2 - h) / 2), text, font=font, fill=BLACK, spacing=4, align="center")


def draw_table(draw: ImageDraw.ImageDraw, svg: SvgCanvas, cx: int, cy: int, title: str, w: int = 180, h: int = 92) -> dict[str, int]:
    x1, y1, x2, y2 = cx - w // 2, cy - h // 2, cx + w // 2, cy + h // 2
    draw.rectangle((x1, y1, x2, y2), fill=WHITE, outline=BLACK, width=2)
    draw.line((x1, y1 + 34, x2, y1 + 34), fill=BLACK, width=2)
    draw.line((cx, y1 + 34, cx, y2), fill=BLACK, width=2)
    draw.line((x1, y1 + 63, x2, y1 + 63), fill=BLACK, width=2)
    center_text(draw, (x1 + 4, y1 + 4, x2 - 4, y1 + 30), title, SMALL_FONT)

    svg.rect(x1, y1, w, h)
    svg.line(x1, y1 + 34, x2, y1 + 34)
    svg.line(cx, y1 + 34, cx, y2)
    svg.line(x1, y1 + 63, x2, y1 + 63)
    svg.text(cx, y1 + 23, title, 15)
    return {"left": x1, "right": x2, "top": y1, "bottom": y2, "cx": cx, "cy": cy}


def draw_label(draw: ImageDraw.ImageDraw, svg: SvgCanvas, x: int, y: int, text: str, size: int = 14) -> None:
    w, h = text_size(draw, text, SMALL_FONT)
    draw.rectangle((x - w // 2 - 4, y - h // 2 - 2, x + w // 2 + 4, y + h // 2 + 2), fill=WHITE)
    draw.multiline_text((x - w / 2, y - h / 2), text, font=SMALL_FONT, fill=BLACK, spacing=4, align="center")
    svg.rect(x - w // 2 - 4, y - h // 2 - 2, w + 8, h + 4, 0)
    svg.text(x, y + 5, text, size)


def draw_poly(draw: ImageDraw.ImageDraw, svg: SvgCanvas, pts: list[tuple[int, int]]) -> None:
    draw.line(pts, fill=BLACK, width=2)
    svg.polyline(pts)


def draw_card(draw: ImageDraw.ImageDraw, svg: SvgCanvas, x: int, y: int, text: str) -> None:
    draw.text((x, y), text, font=SMALL_FONT, fill=BLACK)
    svg.text(x + 6, y + 14, text, 15, anchor="start")


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    width, height = 2200, 1400
    img = Image.new("RGB", (width, height), WHITE)
    draw = ImageDraw.Draw(img)
    svg = SvgCanvas(width, height)

    title = "表间逻辑关系图"
    draw.text((width // 2 - 120, 26), title, font=TITLE_FONT, fill=BLACK)
    svg.text(width // 2, 58, title, 32)

    boxes = {
        "fee_bill": draw_table(draw, svg, 180, 180, "物业费账单表"),
        "property": draw_table(draw, svg, 420, 180, "房产表"),
        "user_property": draw_table(draw, svg, 660, 180, "用户房产\n关联表"),
        "repair_order": draw_table(draw, svg, 1040, 180, "维修工单表"),
        "repair_worker": draw_table(draw, svg, 1300, 180, "维修派工表"),
        "worker_staffing": draw_table(draw, svg, 1560, 180, "维修人员\n排班表"),
        "parking_order": draw_table(draw, svg, 180, 430, "停车订单表"),
        "user_vehicle": draw_table(draw, svg, 420, 430, "用户车辆表"),
        "user": draw_table(draw, svg, 820, 430, "用户表"),
        "repair_fee_bill": draw_table(draw, svg, 1040, 430, "维修费用\n账单表"),
        "repair_objection": draw_table(draw, svg, 1300, 430, "维修费用\n异议表"),
        "repair_eval": draw_table(draw, svg, 1560, 430, "维修评价表"),
        "visitor_invite": draw_table(draw, svg, 1820, 430, "访客邀请表"),
        "owner_quota": draw_table(draw, svg, 180, 680, "业主停车\n额度表"),
        "complaint": draw_table(draw, svg, 420, 680, "投诉表"),
        "points_recharge": draw_table(draw, svg, 660, 680, "积分充值\n记录表"),
        "points_consume": draw_table(draw, svg, 900, 680, "积分消费\n记录表"),
        "visitor_notify": draw_table(draw, svg, 1820, 680, "访客通知表"),
        "second_fav": draw_table(draw, svg, 520, 1080, "二手收藏表"),
        "second_hand": draw_table(draw, svg, 780, 1080, "二手物品表"),
        "lost_found": draw_table(draw, svg, 1040, 1080, "失物招领表"),
        "lost_claim": draw_table(draw, svg, 1300, 1080, "失物认领表"),
        "forum_post": draw_table(draw, svg, 1560, 1080, "论坛帖子表"),
        "forum_like": draw_table(draw, svg, 1820, 980, "帖子点赞表"),
        "forum_comment": draw_table(draw, svg, 1820, 1180, "帖子评论表"),
    }

    # 上部关系
    draw_poly(draw, svg, [(270, 180), (330, 180)])
    draw_label(draw, svg, 300, 160, "生成物业费账单")
    draw_card(draw, svg, 274, 146, "*")
    draw_card(draw, svg, 318, 146, "1")

    draw_poly(draw, svg, [(510, 180), (570, 180)])
    draw_label(draw, svg, 540, 160, "关联房产")
    draw_card(draw, svg, 514, 146, "1")
    draw_card(draw, svg, 558, 146, "*")

    draw_poly(draw, svg, [(730, 226), (730, 320), (820, 320), (820, 384)])
    draw_label(draw, svg, 770, 302, "绑定业主")
    draw_card(draw, svg, 742, 236, "*")
    draw_card(draw, svg, 830, 366, "1")

    draw_poly(draw, svg, [(1040, 226), (1040, 320), (870, 320)])
    draw_label(draw, svg, 960, 302, "提交报修")
    draw_card(draw, svg, 1050, 236, "*")
    draw_card(draw, svg, 878, 300, "1")

    draw_poly(draw, svg, [(970, 180), (750, 180), (750, 210), (510, 210)])
    draw_label(draw, svg, 730, 160, "对应房产")
    draw_card(draw, svg, 954, 146, "*")
    draw_card(draw, svg, 520, 190, "1")

    draw_poly(draw, svg, [(1130, 180), (1210, 180)])
    draw_label(draw, svg, 1170, 160, "分配维修人员")
    draw_card(draw, svg, 1138, 146, "1")
    draw_card(draw, svg, 1202, 146, "*")

    draw_poly(draw, svg, [(1390, 180), (1470, 180)])
    draw_label(draw, svg, 1430, 160, "人员排班信息")
    draw_card(draw, svg, 1398, 146, "*")
    draw_card(draw, svg, 1462, 146, "1")

    # 中部左
    draw_poly(draw, svg, [(270, 430), (730, 430)])
    draw_label(draw, svg, 470, 408, "创建停车订单")
    draw_card(draw, svg, 280, 396, "*")
    draw_card(draw, svg, 718, 396, "1")

    draw_poly(draw, svg, [(270, 460), (330, 460), (330, 226)])
    draw_label(draw, svg, 300, 330, "停车归属房产")
    draw_card(draw, svg, 280, 440, "*")
    draw_card(draw, svg, 338, 236, "1")

    draw_poly(draw, svg, [(510, 430), (730, 430)])
    draw_label(draw, svg, 620, 462, "绑定车辆")
    draw_card(draw, svg, 520, 396, "*")
    draw_card(draw, svg, 718, 446, "1")

    draw_poly(draw, svg, [(270, 680), (730, 680)])
    draw_label(draw, svg, 470, 658, "业主停车额度")
    draw_card(draw, svg, 280, 646, "*")
    draw_card(draw, svg, 718, 646, "1")

    draw_poly(draw, svg, [(510, 680), (730, 680)])
    draw_label(draw, svg, 620, 712, "提交投诉")
    draw_card(draw, svg, 520, 646, "*")
    draw_card(draw, svg, 718, 696, "1")

    draw_poly(draw, svg, [(750, 476), (750, 634), (660, 634)])
    draw_label(draw, svg, 700, 610, "积分充值")
    draw_card(draw, svg, 760, 486, "1")
    draw_card(draw, svg, 668, 614, "*")

    draw_poly(draw, svg, [(860, 476), (860, 634), (900, 634)])
    draw_label(draw, svg, 910, 610, "积分消费")
    draw_card(draw, svg, 870, 486, "1")
    draw_card(draw, svg, 908, 614, "*")

    draw_poly(draw, svg, [(820, 476), (820, 760), (700, 760), (700, 726)])
    draw_label(draw, svg, 730, 742, "充值操作人")
    draw_card(draw, svg, 826, 486, "1")
    draw_card(draw, svg, 708, 736, "*")

    # 中部右维修
    draw_poly(draw, svg, [(1040, 226), (1040, 384)])
    draw_label(draw, svg, 1110, 304, "生成维修费用账单")
    draw_card(draw, svg, 1050, 236, "1")
    draw_card(draw, svg, 1050, 366, "1")

    draw_poly(draw, svg, [(1090, 430), (1210, 430)])
    draw_label(draw, svg, 1150, 408, "提交费用异议")
    draw_card(draw, svg, 1098, 396, "1")
    draw_card(draw, svg, 1202, 396, "*")

    draw_poly(draw, svg, [(1300, 476), (1300, 560), (1090, 560), (1090, 476)])
    draw_label(draw, svg, 1195, 542, "关联维修工单")
    draw_card(draw, svg, 1310, 486, "*")
    draw_card(draw, svg, 1098, 486, "1")

    draw_poly(draw, svg, [(1560, 384), (1560, 226), (1090, 226)])
    draw_label(draw, svg, 1390, 246, "评价工单")
    draw_card(draw, svg, 1570, 366, "1")
    draw_card(draw, svg, 1098, 206, "1")

    draw_poly(draw, svg, [(950, 430), (730, 430)])
    draw_label(draw, svg, 910, 462, "账单归属用户")
    draw_card(draw, svg, 938, 396, "*")
    draw_card(draw, svg, 718, 446, "1")

    draw_poly(draw, svg, [(1210, 460), (900, 460)])
    draw_label(draw, svg, 1060, 492, "异议提交用户")
    draw_card(draw, svg, 1198, 440, "*")
    draw_card(draw, svg, 908, 440, "1")

    # 访客
    draw_poly(draw, svg, [(870, 430), (1730, 430)])
    draw_label(draw, svg, 1450, 408, "创建访客邀请")
    draw_card(draw, svg, 880, 396, "1")
    draw_card(draw, svg, 1718, 396, "*")

    draw_poly(draw, svg, [(1820, 476), (1820, 634)])
    draw_label(draw, svg, 1910, 560, "生成访客通知")
    draw_card(draw, svg, 1830, 486, "1")
    draw_card(draw, svg, 1830, 614, "*")

    draw_poly(draw, svg, [(1730, 680), (900, 680)])
    draw_label(draw, svg, 1460, 712, "通知归属用户")
    draw_card(draw, svg, 1718, 646, "*")
    draw_card(draw, svg, 908, 696, "1")

    # 二手
    draw_poly(draw, svg, [(730, 476), (730, 930), (780, 930), (780, 1034)])
    draw_label(draw, svg, 820, 948, "发布二手物品")
    draw_card(draw, svg, 740, 486, "1")
    draw_card(draw, svg, 790, 1014, "*")

    draw_poly(draw, svg, [(590, 1080), (690, 1080)])
    draw_label(draw, svg, 640, 1060, "收藏二手物品")
    draw_card(draw, svg, 598, 1046, "*")
    draw_card(draw, svg, 678, 1046, "1")

    draw_poly(draw, svg, [(730, 460), (520, 460), (520, 1034)])
    draw_label(draw, svg, 470, 740, "收藏用户")
    draw_card(draw, svg, 718, 440, "1")
    draw_card(draw, svg, 530, 1014, "*")

    # 失物
    draw_poly(draw, svg, [(820, 476), (820, 930), (1040, 930), (1040, 1034)])
    draw_label(draw, svg, 940, 948, "发布失物信息")
    draw_card(draw, svg, 830, 486, "1")
    draw_card(draw, svg, 1050, 1014, "*")

    draw_poly(draw, svg, [(1130, 1080), (1210, 1080)])
    draw_label(draw, svg, 1170, 1060, "提交认领")
    draw_card(draw, svg, 1138, 1046, "1")
    draw_card(draw, svg, 1202, 1046, "*")

    draw_poly(draw, svg, [(900, 460), (1300, 460), (1300, 1034)])
    draw_label(draw, svg, 1330, 740, "认领用户")
    draw_card(draw, svg, 908, 440, "1")
    draw_card(draw, svg, 1310, 1014, "*")

    # 论坛
    draw_poly(draw, svg, [(870, 430), (1560, 430), (1560, 1034)])
    draw_label(draw, svg, 1510, 742, "发布论坛帖子")
    draw_card(draw, svg, 880, 440, "1")
    draw_card(draw, svg, 1570, 1014, "*")

    draw_poly(draw, svg, [(1650, 1048), (1730, 1002)])
    draw_label(draw, svg, 1700, 1016, "点赞帖子")
    draw_card(draw, svg, 1656, 1020, "1")
    draw_card(draw, svg, 1716, 986, "*")

    draw_poly(draw, svg, [(1650, 1112), (1730, 1158)])
    draw_label(draw, svg, 1700, 1146, "评论帖子")
    draw_card(draw, svg, 1656, 1098, "1")
    draw_card(draw, svg, 1716, 1148, "*")

    draw_poly(draw, svg, [(900, 430), (1820, 430), (1820, 934)])
    draw_label(draw, svg, 1870, 720, "点赞用户")
    draw_card(draw, svg, 908, 408, "1")
    draw_card(draw, svg, 1830, 914, "*")

    draw_poly(draw, svg, [(900, 450), (1900, 450), (1900, 1180), (1910, 1180)])
    draw_label(draw, svg, 1950, 820, "评论用户")
    draw_card(draw, svg, 908, 454, "1")
    draw_card(draw, svg, 1900, 1160, "*")

    out_png = OUT_DIR / "table-relationship-project.png"
    out_svg = OUT_DIR / "table-relationship-project.svg"
    img.save(out_png)
    svg.save(out_svg)
    print(out_png)
    print(out_svg)


if __name__ == "__main__":
    main()
