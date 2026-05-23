from __future__ import annotations

import json
import xml.etree.ElementTree as ET
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SRC_DIR = ROOT / "visio-diagrams"
OUT_DIR = SRC_DIR / "visio-import"
MANIFEST = OUT_DIR / "manifest.json"

FILES = [
    "repair-sequence-diagram.svg",
    "architecture-sequence-diagram.svg",
    "system-class-diagram.svg",
    "system-module-diagram.svg",
    "mini-program-module-diagram.svg",
]

SVG_NS = {"svg": "http://www.w3.org/2000/svg"}
ET.register_namespace("", "http://www.w3.org/2000/svg")


def local_name(tag: str) -> str:
    return tag.rsplit("}", 1)[-1]


def normalize_svg(src: Path, dst: Path) -> dict[str, str]:
    tree = ET.parse(src)
    root = tree.getroot()

    title = None

    for elem in root.iter():
        name = local_name(elem.tag)

        if name == "text":
            if title is None and (elem.text or "").strip():
                title = (elem.text or "").strip()
            elem.set("fill", "#000000")
            elem.set("font-family", "Times New Roman, SimSun")
            if elem.attrib.get("font-weight") == "bold":
                elem.set("font-weight", "normal")

        elif name == "rect":
            elem.set("fill", "#FFFFFF")
            if elem.attrib.get("stroke-width", "1") != "0":
                elem.set("stroke", "#000000")
            else:
                elem.set("stroke", "#FFFFFF")

        elif name in {"line", "polyline"}:
            elem.set("stroke", "#000000")

        elif name == "polygon":
            elem.set("fill", "#000000")

    if title is None:
        raise ValueError(f"Unable to infer title from {src}")

    svg_text = ET.tostring(root, encoding="unicode")
    svg_text = svg_text.replace("ns0:", "").replace(":ns0", "")
    dst.write_text(svg_text, encoding="utf-8")
    return {"title": title, "src": str(dst)}


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    items: list[dict[str, str]] = []
    for name in FILES:
        src = SRC_DIR / name
        dst = OUT_DIR / name
        items.append(normalize_svg(src, dst))

    MANIFEST.write_text(
        json.dumps(items, ensure_ascii=True, indent=2),
        encoding="utf-8",
    )
    print(MANIFEST)


if __name__ == "__main__":
    main()
