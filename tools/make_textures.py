"""Generates every texture in the mod as original pixel art.

Run:  python3 tools/make_textures.py
Writes PNGs into src/main/resources/assets/snhunt/textures/ (and the mod icon).
Edit the colors/designs here, or replace any PNG with your own art.
"""
import math
import os
import random

from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "snhunt")
TEX = os.path.join(ROOT, "textures")
random.seed(1967)


def save(img, *path):
    full = os.path.join(TEX, *path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    img.save(full)


def jitter(c, amt=10):
    r, g, b = c[:3]
    a = c[3] if len(c) > 3 else 255
    d = random.randint(-amt, amt)
    return (max(0, min(255, r + d)), max(0, min(255, g + d)), max(0, min(255, b + d)), a)


def fill(img, x0, y0, x1, y1, color, noise=10):
    for y in range(y0, y1):
        for x in range(x0, x1):
            img.putpixel((x, y), jitter(color, noise))


def px(img, x, y, color):
    img.putpixel((x, y), color if len(color) == 4 else (*color, 255))


# --------------------------------------------------------------------------------------------- entities
# Zombie-model layout (64x64 texture, only the top half is used by the biped model):
#   head 0..32 x 0..16 (face front at 8..16, 8..16), body 16..40 x 16..32 (front 20..28),
#   right arm 40..56 x 16..32, right leg 0..16 x 16..32. Left limbs mirror the right ones.

def biped(spec):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    a = spec.get("alpha", 255)

    def c(col):
        return (*col[:3], a)

    skin, hair = c(spec["skin"]), c(spec["hair"])
    shirt, pants = c(spec["shirt"]), c(spec["pants"])
    shoes = c(spec.get("shoes", spec["pants"]))
    n = spec.get("noise", 10)

    # head
    fill(img, 0, 0, 32, 16, skin, n)
    fill(img, 8, 0, 16, 8, hair, n)                       # top
    hr = spec.get("hair_rows", 2)
    fill(img, 0, 8, 8, 8 + hr + 2, hair, n)               # right side
    fill(img, 16, 8, 24, 8 + hr + 2, hair, n)             # left side
    fill(img, 24, 8, 32, 16 if spec.get("long_hair") else 8 + hr + 3, hair, n)  # back
    fill(img, 8, 8, 16, 8 + hr, hair, n)                  # fringe
    if spec.get("long_hair"):
        fill(img, 0, 8, 8, 16, hair, n)
        fill(img, 16, 8, 24, 16, hair, n)
        fill(img, 8, 8, 9, 16, hair, n)
        fill(img, 15, 8, 16, 16, hair, n)

    # body / arms / legs
    fill(img, 16, 16, 40, 32, shirt, n)
    sleeve = c(spec.get("sleeve", spec["shirt"]))
    fill(img, 40, 16, 56, 32, sleeve, n)
    hand_rows = spec.get("hand_rows", 3)
    fill(img, 40, 32 - hand_rows, 56, 32, c(spec.get("hand", spec["skin"])), n)
    fill(img, 44, 16, 52, 20, sleeve, n)  # arm top/bottom caps
    fill(img, 48, 16, 52, 20, c(spec.get("hand", spec["skin"])), n)
    fill(img, 0, 16, 16, 32, pants, n)
    fill(img, 0, 29, 16, 32, shoes, n)
    fill(img, 8, 16, 12, 20, shoes, n)  # sole

    # face
    face = spec.get("face", {})
    ew = c(face.get("white", (240, 240, 240)))
    ep = c(face.get("pupil", (40, 30, 30)))
    row = 12
    for x in (9, 14):
        px(img, x, row, ew)
    for x in (10, 13):
        px(img, x, row, ep)
    if face.get("full_eyes"):
        for x in (9, 10, 13, 14):
            px(img, x, row, ep)
    if face.get("hollow"):
        for x in (9, 10, 13, 14):
            px(img, x, row, ep)
            px(img, x, row + 1, ep)
    mouth = face.get("mouth")
    if mouth:
        for x in range(11, 13):
            px(img, x, 14, c(mouth))
    if face.get("fangs"):
        px(img, 11, 15, c((250, 250, 250)))
        px(img, 12, 15, c((250, 250, 250)))
    if face.get("brow"):
        for x in (9, 10, 13, 14):
            px(img, x, row - 1, c(face["brow"]))

    for fn in spec.get("extra", []):
        fn(img, c)
    return img


def eyes_layer(color, points=((9, 12), (10, 12), (13, 12), (14, 12))):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for (x, y) in points:
        px(img, x, y, (*color, 255))
    return img


def gown_front(img, c):
    # lace trim on a white gown
    for x in range(20, 28, 2):
        px(img, x, 20, c((255, 255, 255)))


def wendigo_ribs(img, c):
    for y in range(21, 31, 2):
        for x in range(21, 27):
            px(img, x, y, c((70, 60, 52)))
    # claws
    for x in range(40, 56, 2):
        px(img, x, 31, c((235, 230, 215)))


def peeling(img, c):
    for _ in range(25):
        x, y = random.randint(0, 39), random.randint(0, 31)
        if img.getpixel((x, y))[3]:
            px(img, x, y, c((150, 40, 45)))


def flannel(img, c):
    for y in range(16, 32):
        for x in range(16, 56):
            if (x % 4 == 0 or y % 4 == 0) and img.getpixel((x, y))[3]:
                if x >= 40 and y >= 29:
                    continue
                px(img, x, y, c((40, 30, 30)))


def stitched_mouth(img, c):
    for x in range(10, 14):
        px(img, x, 14, c((60, 40, 25)))
    for x in (10, 12):
        px(img, x, 13, c((60, 40, 25)))
        px(img, x, 15, c((60, 40, 25)))
    # straw sticking out
    for (x, y) in ((8, 15), (15, 15), (40, 31), (44, 31), (47, 31), (52, 31), (55, 31)):
        px(img, x, y, c((220, 190, 90)))


def jacket_seam(img, c):
    for y in range(20, 32):
        px(img, 24, y, c((15, 15, 15)))


def ember_cracks(img, c):
    for _ in range(30):
        x, y = random.randint(0, 55), random.randint(0, 31)
        if img.getpixel((x, y))[3]:
            px(img, x, y, c((200, 70, 20)))
    jacket_seam(img, c)
    for y in range(20, 26):  # white shirt and dark tie
        px(img, 23, y, c((220, 220, 220)))
        px(img, 25, y, c((220, 220, 220)))
        px(img, 24, y, c((90, 10, 10)))


def shadowy(img, c):
    for y in range(64):
        for x in range(64):
            p = img.getpixel((x, y))
            if p[3] and random.random() < 0.2:
                img.putpixel((x, y), (p[0], p[1], p[2], max(60, p[3] - 90)))


ENTITIES = {
    "spirit": dict(skin=(214, 224, 236), hair=(30, 30, 38), shirt=(238, 240, 246), pants=(238, 240, 246),
                   shoes=(220, 225, 235), alpha=165, long_hair=True, hair_rows=2,
                   face=dict(hollow=True, pupil=(25, 25, 40), mouth=(60, 60, 80)), extra=[gown_front]),
    "wendigo": dict(skin=(120, 108, 96), hair=(70, 62, 55), shirt=(110, 98, 86), pants=(96, 86, 76),
                    shoes=(70, 62, 55), hand=(90, 80, 70), hand_rows=4, hair_rows=1,
                    face=dict(full_eyes=True, pupil=(200, 230, 240), mouth=(40, 20, 20), fangs=True), extra=[wendigo_ribs]),
    "shapeshifter": dict(skin=(196, 150, 124), hair=(78, 52, 32), shirt=(60, 70, 52), pants=(52, 70, 110), shoes=(40, 32, 26),
                         sleeve=(60, 70, 52), face=dict(pupil=(90, 60, 30), mouth=(140, 70, 70)), extra=[peeling]),
    "demon": dict(skin=(205, 168, 140), hair=(40, 28, 20), shirt=(34, 34, 38), pants=(44, 44, 54), shoes=(20, 20, 20),
                  face=dict(full_eyes=True, pupil=(5, 5, 5), mouth=(130, 70, 70)), extra=[jacket_seam]),
    "vampire": dict(skin=(226, 214, 204), hair=(22, 18, 18), shirt=(40, 26, 22), pants=(30, 30, 34), shoes=(15, 15, 15),
                    hair_rows=3, face=dict(pupil=(120, 10, 10), mouth=(120, 10, 15), fangs=True), extra=[jacket_seam]),
    "shtriga": dict(skin=(150, 160, 130), hair=(225, 225, 220), shirt=(70, 72, 66), pants=(64, 66, 60), shoes=(40, 40, 36),
                    long_hair=True, hand=(130, 140, 110),
                    face=dict(full_eyes=True, pupil=(200, 214, 70), mouth=(50, 40, 40)), extra=[]),
    "daeva": dict(skin=(14, 14, 18), hair=(8, 8, 10), shirt=(10, 10, 14), pants=(10, 10, 14), shoes=(6, 6, 8),
                  alpha=220, noise=6, face=dict(full_eyes=True, pupil=(200, 30, 30)), extra=[shadowy]),
    "scarecrow": dict(skin=(196, 164, 104), hair=(222, 192, 92), shirt=(160, 40, 40), pants=(70, 80, 110), shoes=(90, 70, 40),
                      hand=(222, 192, 92), hand_rows=2, hair_rows=1,
                      face=dict(full_eyes=True, pupil=(25, 15, 10)), extra=[flannel, stitched_mouth]),
    "greater_demon": dict(skin=(60, 50, 46), hair=(18, 16, 16), shirt=(22, 22, 24), pants=(22, 22, 24), shoes=(10, 10, 10),
                          face=dict(full_eyes=True, pupil=(240, 140, 30), mouth=(30, 10, 10)), extra=[ember_cracks]),
}

EYES = {
    "wendigo_eyes": (200, 235, 245),
    "shtriga_eyes": (210, 230, 80),
    "daeva_eyes": (255, 40, 40),
    "scarecrow_eyes": (255, 150, 30),
    "greater_demon_eyes": (255, 140, 20),
}


def make_entities():
    for name, spec in ENTITIES.items():
        save(biped(spec), "entity", name + ".png")
    for name, col in EYES.items():
        save(eyes_layer(col), "entity", name + ".png")


# --------------------------------------------------------------------------------------------- items

def canvas():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def draw_pixels(img, rows, palette):
    """rows: list of 16 strings; palette maps chars to RGB(A)."""
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in palette:
                col = palette[ch]
                px(img, x, y, col if len(col) == 4 else (*col, 255))
    return img


def line(img, x0, y0, x1, y1, col):
    steps = max(abs(x1 - x0), abs(y1 - y0)) or 1
    for i in range(steps + 1):
        x = round(x0 + (x1 - x0) * i / steps)
        y = round(y0 + (y1 - y0) * i / steps)
        if 0 <= x < 16 and 0 <= y < 16:
            px(img, x, y, col)


ITEMS = {
    "salt": ([
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        ".......w........",
        "......wWw.......",
        ".....wWWWw..w...",
        "....wWWgWWw.ww..",
        "...wWWWWWgWwWWw.",
        "..wWgWWWWWWWWgWw",
        "..ggggggggggggg.",
        "................",
        "................",
        "................",
    ], {"w": (235, 235, 240), "W": (250, 250, 255), "g": (190, 190, 200)}),
    "salt_shotgun": ([
        "................",
        "...............g",
        "..............gG",
        ".............gGg",
        "............gGg.",
        "...........gGg..",
        "..........gGg...",
        ".........gGg....",
        "........bBg.....",
        ".......bBb......",
        "......bBbt......",
        ".....bBb........",
        "....bBb.........",
        "...bBb..........",
        "..bBb...........",
        "..bb............",
    ], {"g": (80, 84, 90), "G": (150, 155, 165), "b": (100, 62, 34), "B": (140, 90, 50), "t": (60, 60, 60)}),
    "flare_gun": ([
        "................",
        "................",
        "................",
        "................",
        "....oooooooOO...",
        "...oOOOOOOOOOO..",
        "...oOOOOOOOOOo..",
        "...ooooOOoooo...",
        "......oOOo......",
        "......oOOot.....",
        ".....oOOo.t.....",
        ".....oOOott.....",
        "....oOOo........",
        "....oOOo........",
        "....oooo........",
        "................",
    ], {"o": (200, 90, 20), "O": (245, 140, 40), "t": (60, 60, 60)}),
    "emf_meter": ([
        "..........a.....",
        "..........a.....",
        "..........a.....",
        "....kkkkkkakk...",
        "....kGGYYRRkk...",
        "....kkkkkkkkk...",
        "....kssssssskk..",
        "....kssssssskk..",
        "....kssbbbssk...",
        "....kssbbbssk...",
        "....kssssssskk..",
        "....kssssssskk..",
        "....kssssssskk..",
        "....kkkkkkkkk...",
        "................",
        "................",
    ], {"k": (30, 30, 34), "s": (60, 62, 70), "a": (160, 160, 170), "G": (60, 220, 60), "Y": (240, 220, 40),
        "R": (230, 50, 40), "b": (110, 110, 120)}),
    "hunters_journal": ([
        "................",
        "...LLLLLLLLLL...",
        "...LllllllllLp..",
        "...LllllllllLp..",
        "...LlllyylllLp..",
        "...LllyllyllLp..",
        "...LllyllyllLp..",
        "...LlllyylllLp..",
        "...SSSSSSSSSSSb.",
        "...LllllllllLpb.",
        "...LllllllllLp..",
        "...LllllllllLp..",
        "...LllllllllLp..",
        "...LllllllllLp..",
        "...LLLLLLLLLLp..",
        "................",
    ], {"L": (70, 40, 22), "l": (110, 66, 36), "p": (225, 215, 185), "S": (40, 24, 14), "b": (180, 150, 60),
        "y": (190, 160, 70)}),
    "holy_water": ([
        "................",
        ".......cc.......",
        ".......cc.......",
        "......gccg......",
        "......g..g......",
        ".....g....g.....",
        "....g......g....",
        "....gbbbbbbg....",
        "....gbbYbbbg....",
        "....gbYYYbbg....",
        "....gbbYbbbg....",
        "....gbbYbbbg....",
        "....gbbbbbbg....",
        ".....gggggg.....",
        "................",
        "................",
    ], {"c": (140, 100, 60), "g": (200, 225, 240), "b": (90, 150, 230), "Y": (240, 210, 90)}),
    "exorcism_rite": ([
        "................",
        "..bbbbbbbbbbbb..",
        ".bpppppppppppb..",
        "..pprrrprrrpp...",
        "..ppppppppppp...",
        "..pprrprrrrpp...",
        "..ppppppppppp...",
        "..pprrrrrprpp...",
        "..ppppppppppp...",
        "..pprrprrrrpp...",
        "..ppppppppppp...",
        "..pprrrrprrpp...",
        "..ppppppppppp...",
        ".bpppppppppppb..",
        "..bbbbbbbbbbbb..",
        "................",
    ], {"b": (150, 110, 60), "p": (232, 220, 190), "r": (150, 25, 25)}),
    "silver_knife": ([
        "................",
        "..............W.",
        ".............WS.",
        "............WS..",
        "...........WS...",
        "..........WS....",
        ".........WS.....",
        "........WS......",
        ".......WS.......",
        "......gg........",
        ".....hgg........",
        "....hh..........",
        "...hh...........",
        "..hh............",
        "................",
        "................",
    ], {"W": (245, 248, 255), "S": (175, 182, 195), "g": (120, 120, 130), "h": (50, 34, 24)}),
    "machete": ([
        "..............WW",
        ".............WSS",
        "............WSS.",
        "...........WSS..",
        "..........WSS...",
        ".........WSS....",
        "........WSS.....",
        ".......WSS......",
        "......WSS.......",
        ".....WSS........",
        "....gSS.........",
        "...hg...........",
        "..hh............",
        ".hh.............",
        "hh..............",
        "................",
    ], {"W": (220, 225, 230), "S": (140, 145, 150), "g": (90, 90, 95), "h": (30, 30, 30)}),
    "dead_mans_blood": ([
        "................",
        "................",
        ".......kk.......",
        "......gkkg......",
        "......g..g......",
        "......grrg......",
        "......grrg......",
        "......gRrg......",
        "......grrg......",
        "......grRg......",
        "......grrg......",
        "......grrg......",
        "......gggg......",
        "................",
        "................",
        "................",
    ], {"k": (30, 30, 30), "g": (200, 210, 220), "r": (90, 8, 12), "R": (150, 20, 25)}),
    "the_colt": ([
        "................",
        "................",
        "................",
        "...SSSSSSSSSSSS.",
        "..SsssssssssssS.",
        "..SSSSSSSSSSSS..",
        "...GGGGGsSS.....",
        "...GgggGs.S.....",
        "...GGGGGsS......",
        "....iiiS........",
        "...iIIi.........",
        "...iIIi.........",
        "..iIIi..........",
        "..iIIi..........",
        "..iiii..........",
        "................",
    ], {"S": (70, 72, 78), "s": (150, 155, 165), "G": (90, 92, 98), "g": (200, 175, 90), "i": (190, 180, 150),
        "I": (235, 228, 200)}),
    "colt_round": ([
        "................",
        "................",
        "................",
        ".......cc.......",
        "......cCCc......",
        "......cCCc......",
        "......bbbb......",
        "......bBBb......",
        "......bBBb......",
        "......bBBb......",
        "......bBBb......",
        "......bbbb......",
        "......dddd......",
        "................",
        "................",
        "................",
    ], {"c": (150, 100, 50), "C": (200, 140, 70), "b": (180, 140, 50), "B": (230, 195, 90), "d": (140, 100, 40)}),
}


def make_items():
    for name, (rows, palette) in ITEMS.items():
        save(draw_pixels(canvas(), rows, palette), "item", name + ".png")


# --------------------------------------------------------------------------------------------- blocks

def devils_trap():
    img = canvas()
    red = (150, 18, 18, 255)
    cx = cy = 7.5
    for i in range(200):
        a = i / 200 * math.tau
        px(img, int(round(cx + math.cos(a) * 7)), int(round(cy + math.sin(a) * 7)), red)
    pts = []
    for k in range(5):
        a = -math.pi / 2 + k * math.tau / 5
        pts.append((cx + math.cos(a) * 6.5, cy + math.sin(a) * 6.5))
    for k in range(5):
        x0, y0 = pts[k]
        x1, y1 = pts[(k + 2) % 5]
        line(img, int(round(x0)), int(round(y0)), int(round(x1)), int(round(y1)), red)
    for (x, y) in ((1, 1), (14, 1), (1, 14), (14, 14)):
        px(img, x, y, red)
    return img


def salt_line():
    img = canvas()
    for _ in range(110):
        x, y = random.randint(0, 15), random.randint(0, 15)
        v = random.randint(225, 255)
        px(img, x, y, (v, v, v, 255))
    return img


def dirt(base=(110, 80, 55)):
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), jitter((*base, 255), 14))
    return img


def grave_top(salted):
    img = dirt((92, 66, 44))
    bone = (225, 218, 196, 255)
    line(img, 3, 4, 10, 4, bone)
    line(img, 4, 8, 12, 11, bone)
    for (x, y) in ((2, 3), (2, 5), (11, 3), (11, 5), (3, 7), (13, 12), (12, 12)):
        px(img, x, y, bone)
    draw_pixels(img, ["", "", "", "", "", "", "", "", "", "", "", "",
                      "...ss...........", "..sSSs..........", "..sSSs..........", "...ss..........."],
                {"s": (200, 194, 172), "S": (232, 226, 205)})  # skull
    if salted:
        for _ in range(70):
            v = random.randint(230, 255)
            px(img, random.randint(0, 15), random.randint(0, 15), (v, v, v, 255))
    return img


def grave_side():
    img = dirt((96, 70, 48))
    for x in range(16):
        for y in range(0, 3):
            img.putpixel((x, y), jitter((70, 110, 50, 255), 12))
    return img


def cursed_object():
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), jitter((34, 18, 44, 255), 10))
    glow = (200, 60, 220, 255)
    for i in range(1, 15):
        px(img, i, i, glow)
        px(img, 15 - i, i, glow)
    for i in range(3, 13):
        px(img, i, 2, glow)
        px(img, i, 13, glow)
        px(img, 2, i, glow)
        px(img, 13, i, glow)
    return img


def make_blocks():
    save(devils_trap(), "block", "devils_trap.png")
    save(salt_line(), "block", "salt_line.png")
    save(grave_top(False), "block", "grave_top.png")
    save(grave_top(True), "block", "grave_top_salted.png")
    save(grave_side(), "block", "grave_side.png")
    save(cursed_object(), "block", "cursed_object.png")


def make_icon():
    img = Image.new("RGBA", (128, 128), (18, 14, 14, 255))
    d = ImageDraw.Draw(img)
    d.ellipse((10, 10, 118, 118), outline=(170, 20, 20, 255), width=6)
    pts = []
    for k in range(5):
        a = -math.pi / 2 + k * math.tau / 5
        pts.append((64 + math.cos(a) * 50, 64 + math.sin(a) * 50))
    for k in range(5):
        d.line([pts[k], pts[(k + 2) % 5]], fill=(170, 20, 20, 255), width=5)
    d.ellipse((50, 50, 78, 78), fill=(240, 240, 245, 255))  # a pile of salt at the centre
    img.save(os.path.join(ROOT, "icon.png"))


if __name__ == "__main__":
    make_entities()
    make_items()
    make_blocks()
    make_icon()
    print("textures written to", os.path.abspath(TEX))
