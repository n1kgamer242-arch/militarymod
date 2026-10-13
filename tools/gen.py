import os, json, struct, zlib, gzip, random

ROOT = '/home/claude/militarymod/src/main/resources'
DATA = ROOT + '/data/militarymod'
ASSETS = ROOT + '/assets/militarymod'
def mk(p): os.makedirs(os.path.dirname(p), exist_ok=True)
def wjson(p, o):
    mk(p)
    with open(p, 'w') as f: json.dump(o, f, indent=1)

# ============================ PNG / textures ============================
def png(path, w, h, px):
    mk(path)
    raw = b''.join(b'\x00' + bytes(c for p in row for c in p) for row in px)
    def ch(t, d):
        c = struct.pack('>I', len(d)) + t + d
        return c + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
    with open(path, 'wb') as f:
        f.write(b'\x89PNG\r\n\x1a\n' + ch(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0))
                + ch(b'IDAT', zlib.compress(raw)) + ch(b'IEND', b''))

def skin(path, base, dark, light, hostile):
    rnd = random.Random(7)
    img = [[(0, 0, 0, 0)] * 64 for _ in range(64)]
    SK = (224, 172, 138, 255); BLK = (20, 20, 22, 255)
    camo = lambda: rnd.choice([base, base, dark, light, base])
    def rect(x, y, w, h, f):
        for j in range(h):
            for i in range(w): img[y + j][x + i] = f()
    def part(u, v, w, h, d, f):
        rect(u + d, v, 2 * w, d, f); rect(u, v + d, 2 * d + 2 * w, h, f)
    # testa
    part(0, 0, 8, 8, 8, lambda: SK)
    rect(8, 0, 16, 8, camo)               # sommita' casco (top+bottom)
    rect(0, 8, 32, 3, camo)               # casco su tutti i lati
    if hostile:
        rect(8, 13, 8, 3, lambda: BLK)    # passamontagna
        rect(0, 13, 8, 3, lambda: BLK); rect(16, 13, 8, 3, lambda: BLK); rect(24, 13, 8, 3, lambda: BLK)
        img[12][10] = (180, 30, 30, 255); img[12][13] = (180, 30, 30, 255)
    else:
        img[12][10] = (30, 30, 30, 255); img[12][13] = (30, 30, 30, 255)
    # corpo + gilet
    part(16, 16, 8, 12, 4, camo)
    rect(21, 21, 6, 6, lambda: dark)
    # braccia
    for (u, v) in ((40, 16), (32, 48)):
        part(u, v, 4, 12, 4, camo)
        for j in range(9, 12):
            for i in range(0, 16):
                if img[v + 4 + j][u + i][3]: img[v + 4 + j][u + i] = SK
    if hostile:
        rect(40, 21, 16, 2, lambda: (170, 25, 25, 255))   # fascia rossa
    # gambe + stivali
    for (u, v) in ((0, 16), (16, 48)):
        part(u, v, 4, 12, 4, camo)
        for j in range(9, 12):
            for i in range(0, 16):
                if img[v + 4 + j][u + i][3]: img[v + 4 + j][u + i] = BLK
    png(path, 64, 64, img)

skin(ASSETS + '/textures/entity/ally_soldier.png', (75, 92, 45, 255), (44, 58, 30, 255), (112, 124, 72, 255), False)
skin(ASSETS + '/textures/entity/hostile_soldier.png', (52, 52, 58, 255), (26, 26, 30, 255), (84, 84, 90, 255), True)

def icon(path, painter):
    img = [[(0, 0, 0, 0)] * 16 for _ in range(16)]
    painter(lambda x, y, c: img.__getitem__(y).__setitem__(x, c))
    png(path, 16, 16, img)
def rifle(s):
    for i in range(11):
        s(3 + i, 12 - i, (60, 60, 66, 255)); s(4 + i, 12 - i, (90, 90, 98, 255))
    for i in range(4): s(2 + i, 13 - i // 2 + 0, (110, 70, 35, 255)); s(1 + i, 14 - i, (110, 70, 35, 255))
    s(7, 10, (30, 30, 30, 255)); s(8, 9, (30, 30, 30, 255))
def ammo(s):
    for k in range(3):
        for y in range(5, 13):
            s(3 + k * 4, y, (214, 170, 60, 255)); s(4 + k * 4, y, (186, 140, 40, 255))
        s(3 + k * 4, 4, (150, 100, 40, 255)); s(4 + k * 4, 4, (150, 100, 40, 255))
def radio(s):
    for x in range(4, 13):
        for y in range(7, 14): s(x, y, (60, 70, 55, 255))
    for x in range(5, 9):
        for y in range(8, 10): s(x, y, (120, 200, 120, 255))
    for y in range(1, 7): s(11, y, (160, 160, 165, 255))
icon(ASSETS + '/textures/item/rifle.png', rifle)
icon(ASSETS + '/textures/item/ammo.png', ammo)
icon(ASSETS + '/textures/item/command_radio.png', radio)

for n in ('rifle', 'ammo', 'command_radio'):
    wjson(ASSETS + f'/models/item/{n}.json', {"parent": "item/handheld" if n == 'rifle' else "item/generated",
                                              "textures": {"layer0": f"militarymod:item/{n}"}})
for n in ('ally_soldier_spawn_egg', 'hostile_soldier_spawn_egg'):
    wjson(ASSETS + f'/models/item/{n}.json', {"parent": "minecraft:item/template_spawn_egg"})

wjson(ASSETS + '/lang/en_us.json', {
    "itemGroup.militarymod": "Military Mod",
    "item.militarymod.rifle": "Assault Rifle", "item.militarymod.ammo": "Ammo",
    "item.militarymod.command_radio": "Command Radio",
    "item.militarymod.ally_soldier_spawn_egg": "Allied Soldier Spawn Egg",
    "item.militarymod.hostile_soldier_spawn_egg": "Hostile Soldier Spawn Egg",
    "entity.militarymod.ally_soldier": "Allied Soldier", "entity.militarymod.hostile_soldier": "Hostile Soldier",
    "entity.militarymod.bullet": "Bullet",
    "msg.militarymod.wave": "WAVE %s! Enemy soldiers are closing in!",
    "msg.militarymod.recruited": "Soldier recruited!", "msg.militarymod.need_emeralds": "Recruiting costs %s emeralds.",
    "msg.militarymod.mode_follow": "Soldier: FOLLOW", "msg.militarymod.mode_guard": "Soldier: GUARD",
    "msg.militarymod.radio_rally": "%s soldiers moving to position", "msg.militarymod.radio_follow": "%s soldiers following you"})
wjson(ASSETS + '/lang/it_it.json', {
    "itemGroup.militarymod": "Military Mod",
    "item.militarymod.rifle": "Fucile d'assalto", "item.militarymod.ammo": "Munizioni",
    "item.militarymod.command_radio": "Radio di comando",
    "item.militarymod.ally_soldier_spawn_egg": "Uovo di spawn Soldato alleato",
    "item.militarymod.hostile_soldier_spawn_egg": "Uovo di spawn Soldato nemico",
    "entity.militarymod.ally_soldier": "Soldato alleato", "entity.militarymod.hostile_soldier": "Soldato nemico",
    "entity.militarymod.bullet": "Proiettile",
    "msg.militarymod.wave": "ONDATA %s! I soldati nemici stanno arrivando!",
    "msg.militarymod.recruited": "Soldato reclutato!", "msg.militarymod.need_emeralds": "Reclutare costa %s smeraldi.",
    "msg.militarymod.mode_follow": "Soldato: SEGUI", "msg.militarymod.mode_guard": "Soldato: GUARDIA",
    "msg.militarymod.radio_rally": "%s soldati in movimento verso la posizione", "msg.militarymod.radio_follow": "%s soldati ti seguono"})

# ============================ recipes / loot ============================
wjson(DATA + '/recipes/rifle.json', {"type": "minecraft:crafting_shaped", "pattern": ["III", "  S", "  S"],
      "key": {"I": {"item": "minecraft:iron_ingot"}, "S": {"item": "minecraft:stick"}},
      "result": {"item": "militarymod:rifle"}})
wjson(DATA + '/recipes/ammo.json', {"type": "minecraft:crafting_shapeless",
      "ingredients": [{"item": "minecraft:gunpowder"}, {"item": "minecraft:iron_ingot"}],
      "result": {"item": "militarymod:ammo", "count": 8}})
wjson(DATA + '/recipes/command_radio.json', {"type": "minecraft:crafting_shapeless",
      "ingredients": [{"item": "minecraft:iron_ingot"}, {"item": "minecraft:redstone"}, {"item": "minecraft:compass"}],
      "result": {"item": "militarymod:command_radio"}})

def entry(name, w, lo, hi):
    e = {"type": "minecraft:item", "name": name, "weight": w}
    if hi > 1 or lo != 1:
        e["functions"] = [{"function": "minecraft:set_count", "count": {"min": lo, "max": hi}}]
    return e
wjson(DATA + '/loot_tables/chests/military.json', {"type": "minecraft:chest", "pools": [
    {"rolls": {"min": 3, "max": 6}, "entries": [
        entry("militarymod:ammo", 10, 8, 24), entry("minecraft:iron_ingot", 8, 2, 6),
        entry("minecraft:gold_ingot", 4, 1, 4), entry("minecraft:emerald", 6, 1, 4),
        entry("minecraft:cooked_beef", 6, 3, 8), entry("minecraft:golden_apple", 1, 1, 1),
        entry("minecraft:iron_chestplate", 2, 1, 1), entry("militarymod:rifle", 2, 1, 1),
        entry("minecraft:gunpowder", 5, 2, 8)]}]})
wjson(DATA + '/loot_tables/entities/hostile_soldier.json', {"type": "minecraft:entity", "pools": [
    {"rolls": 1, "entries": [entry("militarymod:ammo", 1, 0, 4)]},
    {"rolls": 1, "conditions": [{"condition": "minecraft:random_chance", "chance": 0.15}],
     "entries": [{"type": "minecraft:item", "name": "minecraft:emerald"}]}]})

# ============================ NBT writer / builder ============================
def _s(s):
    b = s.encode(); return struct.pack('>H', len(b)) + b
def wval(t, v):
    if t == 1: return struct.pack('>b', v)
    if t == 3: return struct.pack('>i', v)
    if t == 6: return struct.pack('>d', v)
    if t == 8: return _s(v)
    if t == 9:
        et, items = v
        return struct.pack('>bi', et, len(items)) + b''.join(wval(et, i) for i in items)
    if t == 10:
        out = b''
        for k, (tt, vv) in v.items(): out += struct.pack('>b', tt) + _s(k) + wval(tt, vv)
        return out + b'\x00'

AIR = 'minecraft:air'
DIRS = {'north': (0, -1), 'south': (0, 1), 'east': (1, 0), 'west': (-1, 0)}
SB = 'minecraft:stone_bricks'; CB = 'minecraft:cobblestone'; PL = 'minecraft:spruce_planks'; LOG = 'minecraft:spruce_log'
CON = 'minecraft:gray_concrete'; BARS = 'minecraft:iron_bars'; SAND = 'minecraft:sandstone'; WOOLG = 'minecraft:green_wool'

class B:
    def __init__(s): s.b = {}; s.e = []
    def set(s, x, y, z, n, nbt=None, **p): s.b[(x, y, z)] = (n, tuple(sorted(p.items())), nbt)
    def box(s, x1, y1, z1, x2, y2, z2, n, **p):
        for x in range(min(x1, x2), max(x1, x2) + 1):
            for y in range(min(y1, y2), max(y1, y2) + 1):
                for z in range(min(z1, z2), max(z1, z2) + 1): s.set(x, y, z, n, **p)
    def shell(s, x1, y1, z1, x2, y2, z2, wall):
        s.box(x1, y1, z1, x2, y2, z2, wall); s.box(x1 + 1, y1 + 1, z1 + 1, x2 - 1, y2 - 1, z2 - 1, AIR)
    def door(s, x, y, z, f):
        for half, dy in (('lower', 0), ('upper', 1)):
            s.set(x, y + dy, z, 'minecraft:iron_door', facing=f, half=half, hinge='left', open='false', powered='false')
    def ladder(s, x, y1, y2, z, f):
        for y in range(y1, y2 + 1): s.set(x, y, z, 'minecraft:ladder', facing=f, waterlogged='false')
    def chest(s, x, y, z, f='south'):
        s.set(x, y, z, 'minecraft:chest', nbt={'id': (8, 'minecraft:chest'), 'LootTable': (8, 'militarymod:chests/military')},
              facing=f, type='single', waterlogged='false')
    def lantern(s, x, y, z, hang='false'): s.set(x, y, z, 'minecraft:lantern', hanging=hang, waterlogged='false')
    def bed(s, x, y, z, f):
        dx, dz = DIRS[f]
        s.set(x, y, z, 'minecraft:red_bed', facing=f, part='foot', occupied='false')
        s.set(x + dx, y, z + dz, 'minecraft:red_bed', facing=f, part='head', occupied='false')
    def ally(s, x, y, z): s.e.append(('ally', x + .5, y, z + .5))
    def foe(s, x, y, z): s.e.append(('foe', x + .5, y, z + .5))
    def save(s, name):
        pal = {}; blocks = []
        for (x, y, z), (n, props, nbt) in s.b.items():
            key = (n, props)
            if key not in pal: pal[key] = len(pal)
            e = {'pos': (9, (3, [x, y, z])), 'state': (3, pal[key])}
            if nbt: e['nbt'] = (10, nbt)
            blocks.append(e)
        palette = []
        for (n, props), i in sorted(pal.items(), key=lambda kv: kv[1]):
            d = {'Name': (8, n)}
            if props: d['Properties'] = (10, {k: (8, v) for k, v in props})
            palette.append(d)
        size = [max(c[i] for c in s.b) + 1 for i in range(3)]
        ents = []
        for kind, x, y, z in s.e:
            nbt = {'id': (8, 'militarymod:ally_soldier' if kind == 'ally' else 'militarymod:hostile_soldier'),
                   'PersistenceRequired': (1, 1)}
            if kind == 'ally': nbt['Mode'] = (3, 1)
            ents.append({'pos': (9, (6, [x, float(y), z])), 'blockPos': (9, (3, [int(x), int(y), int(z)])), 'nbt': (10, nbt)})
        root = {'DataVersion': (3, 3465), 'size': (9, (3, size)), 'palette': (9, (10, palette)),
                'blocks': (9, (10, blocks)), 'entities': (9, (10, ents))}
        p = DATA + f'/structures/{name}.nbt'; mk(p)
        with gzip.open(p, 'wb') as f: f.write(struct.pack('>b', 10) + _s('') + wval(10, root))
        return len(blocks), len(ents)

def found(b, x1, z1, x2, z2, h=8):
    b.box(x1, 0, z1, x2, 2, z2, CB); b.box(x1, 3, z1, x2, 3, z2, SB); b.box(x1, 4, z1, x2, 3 + h, z2, AIR)

# ============================ 10 STRUTTURE DIFENSIVE (in superficie) ============================
def watchtower():
    b = B(); found(b, 0, 0, 6, 6, 14)
    b.shell(1, 3, 1, 5, 9, 5, SB); b.box(0, 9, 0, 6, 9, 6, PL); b.set(2, 9, 3, AIR)
    b.ladder(2, 4, 9, 3, 'east'); b.door(3, 4, 5, 'south')
    for x in range(7):
        for z in range(7):
            if x in (0, 6) or z in (0, 6): b.set(x, 10, z, BARS)
    for x, z in ((0, 0), (0, 6), (6, 0), (6, 6)): b.box(x, 10, z, x, 12, z, LOG)
    b.box(0, 13, 0, 6, 13, 6, 'minecraft:spruce_slab', type='bottom', waterlogged='false')
    b.lantern(3, 12, 3, 'true'); b.chest(4, 4, 2)
    b.ally(1, 10, 1); b.ally(5, 10, 5); b.ally(3, 4, 3); return b
def sandbag_nest():
    b = B(); found(b, 0, 0, 8, 8, 4)
    for x in range(9):
        for z in range(9):
            if x in (0, 8) or z in (0, 8): b.box(x, 4, z, x, 5, z, SAND)
    b.box(3, 4, 8, 5, 5, 8, AIR); b.chest(1, 4, 1)
    b.set(4, 4, 4, 'minecraft:campfire', facing='north', lit='true', signal_fire='false', waterlogged='false')
    b.ally(2, 4, 2); b.ally(6, 4, 2); b.ally(4, 4, 6); return b
def checkpoint():
    b = B(); found(b, 0, 0, 12, 6, 6); b.box(0, 3, 2, 12, 3, 4, CON)
    for z in range(7):
        if z != 3: b.box(6, 4, z, 6, 5, z, BARS)
    b.shell(8, 3, 0, 12, 7, 4, CB); b.door(8, 4, 2, 'west')
    for p in ((12, 5, 2), (10, 5, 0), (10, 5, 4)): b.set(*p, BARS)
    b.chest(11, 4, 1); b.lantern(10, 6, 2, 'true')
    b.ally(7, 4, 1); b.ally(7, 4, 5); b.ally(10, 4, 2); return b
def concrete_bunker():
    b = B(); found(b, 0, 0, 10, 10, 6); b.shell(1, 3, 1, 9, 8, 9, CON)
    for i in range(2, 9, 2):
        for p in ((i, 6, 1), (i, 6, 9), (1, 6, i), (9, 6, i)): b.set(*p, BARS)
    b.door(5, 4, 9, 'south'); b.chest(2, 4, 2); b.chest(8, 4, 2); b.set(8, 4, 8, 'minecraft:crafting_table')
    b.lantern(5, 7, 5, 'true'); b.ally(3, 4, 5); b.ally(7, 4, 5); b.ally(5, 4, 3); return b
def sniper_nest():
    b = B(); found(b, 0, 0, 6, 6, 14)
    for x, z in ((1, 1), (1, 5), (5, 1), (5, 5)): b.box(x, 4, z, x, 11, z, LOG)
    b.box(0, 12, 0, 6, 12, 6, PL); b.set(2, 12, 1, AIR); b.ladder(2, 4, 12, 1, 'east')
    for x in range(7):
        for z in range(7):
            if x in (0, 6) or z in (0, 6): b.set(x, 13, z, BARS)
    for x, z in ((0, 0), (0, 6), (6, 0), (6, 6)): b.box(x, 13, z, x, 15, z, LOG)
    b.box(0, 16, 0, 6, 16, 6, 'minecraft:spruce_slab', type='bottom', waterlogged='false')
    b.lantern(3, 15, 3, 'true'); b.chest(5, 13, 3, 'west'); b.ally(4, 13, 5); b.ally(4, 13, 1); return b
def tent(b, x, z):
    b.shell(x, 3, z, x + 4, 6, z + 4, WOOLG); b.box(x + 2, 4, z + 4, x + 2, 5, z + 4, AIR)
    b.bed(x + 1, 4, z + 1, 'south'); b.bed(x + 3, 4, z + 1, 'south')
def field_camp():
    b = B(); found(b, 0, 0, 16, 10, 6)
    for x in (0, 6, 12): tent(b, x, 0)
    b.set(8, 4, 8, 'minecraft:campfire', facing='north', lit='true', signal_fire='false', waterlogged='false')
    b.chest(6, 4, 8); b.ally(3, 4, 8); b.ally(8, 4, 6); b.ally(13, 4, 8); return b
def barracks():
    b = B(); found(b, 0, 0, 14, 8, 6); b.shell(1, 3, 1, 13, 8, 7, PL)
    for x in (2, 4, 6, 10, 12): b.bed(x, 4, 3, 'north')
    b.door(7, 4, 7, 'south'); b.chest(12, 4, 6); b.chest(2, 4, 6)
    for p in ((4, 5, 7), (10, 5, 7), (4, 5, 1), (10, 5, 1)): b.set(*p, BARS)
    b.lantern(7, 7, 4, 'true'); b.ally(5, 4, 5); b.ally(9, 4, 5); b.ally(7, 4, 3); return b
def trench_line():
    b = B(); found(b, 0, 0, 16, 4, 4); b.box(0, 3, 1, 16, 3, 3, PL)
    for x in range(17):
        b.box(x, 4, 0, x, 5 if x % 2 == 0 else 4, 0, SAND); b.box(x, 4, 4, x, 5 if x % 2 == 0 else 4, 4, SAND)
    b.box(0, 4, 1, 0, 4, 3, SAND); b.box(16, 4, 1, 16, 4, 3, SAND)
    b.chest(2, 4, 2); b.ally(3, 4, 2); b.ally(8, 4, 2); b.ally(13, 4, 2); b.ally(8, 4, 1); return b
def radio_outpost():
    b = B(); found(b, 0, 0, 8, 8, 18); b.shell(1, 3, 1, 7, 7, 7, CB); b.door(4, 4, 7, 'south')
    b.box(4, 8, 4, 4, 16, 4, BARS); b.set(4, 17, 4, 'minecraft:lightning_rod', facing='up', powered='false', waterlogged='false')
    b.chest(2, 4, 2); b.set(6, 4, 2, 'minecraft:note_block'); b.lantern(4, 6, 4, 'true')
    b.ally(4, 4, 5); b.ally(2, 4, 5); return b
def armory():
    b = B(); found(b, 0, 0, 10, 8, 6); b.shell(1, 3, 1, 9, 7, 7, SB); b.door(5, 4, 7, 'south')
    for x in (2, 4, 6, 8): b.chest(x, 4, 2)
    for p in ((3, 5, 1), (7, 5, 1), (1, 5, 4), (9, 5, 4)): b.set(*p, BARS)
    b.lantern(5, 6, 4, 'true'); b.ally(5, 4, 4); b.ally(3, 4, 5); return b

DEFENSES = dict(watchtower=watchtower, sandbag_nest=sandbag_nest, checkpoint=checkpoint, concrete_bunker=concrete_bunker,
                sniper_nest=sniper_nest, field_camp=field_camp, barracks=barracks, trench_line=trench_line,
                radio_outpost=radio_outpost, armory=armory)

# ============================ BASE DI RECLUTAMENTO ============================
def recruitment_base():
    b = B(); found(b, 0, 0, 30, 30, 16)
    for i in range(31):
        for (x, z) in ((i, 0), (i, 30), (0, i), (30, i)): b.box(x, 4, z, x, 6, z, SB)
    b.box(13, 4, 30, 17, 6, 30, AIR); b.box(13, 6, 30, 17, 6, 30, BARS)
    def tower(x, z, df):
        b.shell(x, 3, z, x + 4, 9, z + 4, SB); b.set(x + 2, 9, z + 1, AIR); b.ladder(x + 2, 4, 9, z + 1, 'south')
        dx = x + 4 if df == 'east' else x
        b.door(dx, 4, z + 2, df)
        for i in range(5):
            for p in ((x + i, 10, z), (x + i, 10, z + 4), (x, 10, z + i), (x + 4, 10, z + i)): b.set(*p, BARS)
        b.lantern(x + 2, 5, z + 3); b.ally(x + 3, 10, z + 3)
    tower(0, 0, 'east'); tower(0, 26, 'east'); tower(26, 0, 'west'); tower(26, 26, 'west')
    # quartier generale
    b.shell(10, 3, 8, 20, 9, 18, SB); b.door(15, 4, 18, 'south')
    b.chest(11, 4, 9); b.chest(15, 4, 9); b.chest(19, 4, 9)
    b.set(13, 4, 9, 'minecraft:crafting_table'); b.set(17, 4, 9, 'minecraft:crafting_table')
    for p in ((12, 6, 18), (18, 6, 18), (10, 6, 13), (20, 6, 13)): b.set(*p, BARS)
    b.lantern(15, 8, 13, 'true'); b.ally(13, 4, 14); b.ally(17, 4, 14)
    # tende + bandiera
    tent(b, 3, 10); tent(b, 22, 10)
    b.box(6, 4, 27, 6, 11, 27, LOG); b.box(7, 8, 27, 9, 10, 27, 'minecraft:red_wool')
    # reclute (tasto destro + 3 smeraldi)
    for x in range(8, 24, 2): b.ally(x, 4, 22)
    for x in range(9, 23, 4): b.ally(x, 4, 25)
    return b

# ============================ 10 DUNGEON MILITARI (sotterranei) ============================
def dungeon(wall, floor, seed, nrooms, soldiers, nchests, feature, hubh):
    r = random.Random(seed); b = B(); H = hubh + 8; cx = cz = 16
    org = [1, 12, 23]
    chosen = [(1, 1)]; edges = []
    while len(chosen) < nrooms:
        c = r.choice(chosen); d = r.choice([(1, 0), (-1, 0), (0, 1), (0, -1)])
        n = (c[0] + d[0], c[1] + d[1])
        if 0 <= n[0] < 3 and 0 <= n[1] < 3 and n not in chosen: chosen.append(n); edges.append((c, n))
    def rect(c): return (org[c[0]], org[c[1]], org[c[0]] + 7, org[c[1]] + 7)
    def hh(c): return hubh if c == (1, 1) else 4
    def corr(a, c):
        (x1, z1, x2, z2), (u1, v1, u2, v2) = rect(a), rect(c)
        if a[1] == c[1]:
            lo, hi = sorted((x2, u2)); lo2 = min(x2, u2) + 1; hi2 = max(x1, u1) - 1
            return (lo2, z1 + 3, hi2, z1 + 4)
        lo2 = min(z2, v2) + 1; hi2 = max(z1, v1) - 1
        return (x1 + 3, lo2, x1 + 4, hi2)
    # pass 1: muri, pass 2: scavo
    for c in chosen:
        x1, z1, x2, z2 = rect(c); b.box(x1 - 1, 0, z1 - 1, x2 + 1, hh(c) + 1, z2 + 1, wall)
    cors = [corr(a, c) for a, c in edges]
    for (x1, z1, x2, z2) in cors: b.box(x1 - 1, 0, z1 - 1, x2 + 1, 4, z2 + 1, wall)
    b.box(cx - 1, hubh + 1, cz - 1, cx + 1, H - 1, cz + 1, wall)
    for c in chosen:
        x1, z1, x2, z2 = rect(c); b.box(x1, 1, z1, x2, hh(c), z2, AIR); b.box(x1, 0, z1, x2, 0, z2, floor)
    for (x1, z1, x2, z2) in cors:
        b.box(x1, 1, z1, x2, 3, z2, AIR); b.box(x1, 0, z1, x2, 0, z2, floor)
    # pozzo + scala + pilastro d'appoggio
    b.box(cx, hubh + 1, cz, cx, H, cz, AIR); b.box(cx, 1, cz - 1, cx, hubh, cz - 1, wall)
    b.ladder(cx, 1, H, cz, 'south')
    # capanna d'ingresso
    b.shell(cx - 2, H, cz - 2, cx + 2, H + 4, cz + 2, CB); b.set(cx, H, cz, AIR); b.ladder(cx, H, H, cz, 'south')
    b.door(cx, H + 1, cz + 2, 'south'); b.lantern(cx + 1, H + 1, cz + 1); b.foe(cx + 1, H + 1, cz - 1)
    # arredo
    chest_rooms = r.sample(chosen, min(nchests, len(chosen))) if nchests <= len(chosen) else chosen
    for c in chosen:
        x1, z1, x2, z2 = rect(c); hub = c == (1, 1); h = hh(c)
        b.lantern((x1 + x2) // 2, h, (z1 + z2) // 2 + (2 if hub else 0), 'true')
        n = soldiers + (2 if hub else 0)
        for _ in range(r.randint(max(1, n - 1), n)):
            for _try in range(20):
                x, z = r.randint(x1, x2), r.randint(z1, z2)
                if abs(x - cx) > 2 or abs(z - cz) > 2: b.foe(x, 1, z); break
        if c in chest_rooms: b.chest(x1, 1, z1)
        if c in chest_rooms and nchests > len(chosen): b.chest(x2, 1, z2, 'north')
        if feature == 'tables': b.set(x2, 1, z1, 'minecraft:crafting_table'); b.set(x2, 1, z1 + 1, 'minecraft:furnace', facing='west', lit='false')
        elif feature == 'crates':
            for i in range(3): b.set(x2, 1, z2 - i, 'minecraft:barrel', facing='up', open='false')
        elif feature == 'cells' and not hub:
            b.box(x1, 1, z1 + 3, x2 - 2, 3, z1 + 3, BARS)
        elif feature == 'lab' and not hub:
            b.box(x1 + 1, 1, z1 + 1, x2 - 1, 1, z1 + 1, 'minecraft:iron_block')
            b.box(x1 + 1, 2, z1 + 1, x2 - 1, 2, z1 + 1, 'minecraft:white_stained_glass')
        elif feature == 'beds' and not hub:
            b.bed(x1 + 1, 1, z1 + 2, 'north'); b.bed(x1 + 3, 1, z1 + 2, 'north'); b.bed(x1 + 5, 1, z1 + 2, 'north')
        elif feature == 'silo' and hub:
            b.box(18, 1, 18, 19, hubh - 1, 19, 'minecraft:iron_block'); b.box(18, hubh, 18, 19, hubh, 19, 'minecraft:red_concrete')
    return b, H + 1

DUNGEONS = {
 'underground_bunker': (SB, 'minecraft:stone', 11, 5, 2, 2, 'tables', 4),
 'command_center': (CON, 'minecraft:polished_andesite', 21, 7, 2, 3, 'tables', 5),
 'supply_depot': ('minecraft:deepslate_bricks', 'minecraft:deepslate_tiles', 31, 6, 2, 5, 'crates', 4),
 'prison_camp': (CB, 'minecraft:stone', 41, 6, 2, 2, 'cells', 4),
 'laboratory': ('minecraft:white_concrete', 'minecraft:smooth_quartz', 51, 6, 2, 3, 'lab', 4),
 'armory_vault': ('minecraft:polished_blackstone_bricks', 'minecraft:blackstone', 61, 4, 3, 7, 'crates', 4),
 'missile_silo': ('minecraft:light_gray_concrete', 'minecraft:gray_concrete', 71, 5, 2, 3, 'silo', 9),
 'tunnel_complex': ('minecraft:deepslate_tiles', 'minecraft:deepslate', 81, 9, 1, 3, 'tables', 4),
 'hq_barracks': (SB, 'minecraft:spruce_planks', 91, 6, 2, 2, 'beds', 4),
 'black_site': ('minecraft:polished_blackstone', 'minecraft:blackstone', 101, 9, 3, 5, 'lab', 5),
}

# ============================ salvataggio + JSON worldgen ============================
def structure_json(name, offset, step, hproj='WORLD_SURFACE_WG', adapt='beard_thin'):
    wjson(DATA + f'/worldgen/structure/{name}.json', {
        "type": "minecraft:jigsaw", "biomes": "#militarymod:has_military_structures", "step": step,
        "spawn_overrides": {}, "terrain_adaptation": adapt, "start_pool": f"militarymod:{name}", "size": 1,
        "start_height": {"absolute": offset}, "project_start_to_heightmap": hproj,
        "max_distance_from_center": 80, "use_expansion_hack": False})
    wjson(DATA + f'/worldgen/template_pool/{name}.json', {"name": f"militarymod:{name}", "fallback": "minecraft:empty",
        "elements": [{"weight": 1, "element": {"element_type": "minecraft:single_pool_element",
                      "location": f"militarymod:{name}", "projection": "rigid", "processors": "minecraft:empty"}}]})
def structure_set(name, names, spacing, sep, salt):
    wjson(DATA + f'/worldgen/structure_set/{name}.json', {
        "structures": [{"structure": f"militarymod:{n}", "weight": 1} for n in names],
        "placement": {"type": "minecraft:random_spread", "spacing": spacing, "separation": sep, "salt": salt}})

report = []
for n, fn in DEFENSES.items():
    report.append((n, *fn().save(n))) if False else None
    bl = fn(); report.append((n, *bl.save(n))); structure_json(n, -4, 'surface_structures')
bl = recruitment_base(); report.append(('recruitment_base', *bl.save('recruitment_base')))
structure_json('recruitment_base', -4, 'surface_structures')
for n, a in DUNGEONS.items():
    bl, top = dungeon(*a); report.append((n, *bl.save(n))); structure_json(n, -top, 'underground_structures', adapt='none')

structure_set('military_bases', ['recruitment_base'], 48, 20, 91827364)
structure_set('military_defenses', list(DEFENSES), 20, 8, 91827365)
structure_set('military_dungeons', list(DUNGEONS), 22, 9, 91827366)
wjson(DATA + '/tags/worldgen/biome/has_military_structures.json', {"values": [
    "#minecraft:is_forest", "#minecraft:is_taiga", "#minecraft:is_hill", "#minecraft:is_savanna",
    "#minecraft:is_badlands", "#minecraft:is_jungle", "minecraft:plains", "minecraft:sunflower_plains",
    "minecraft:desert", "minecraft:snowy_plains", "minecraft:meadow"]})
for r_ in report: print(r_)
