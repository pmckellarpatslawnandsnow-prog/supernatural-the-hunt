"""Writes item/block models, blockstates, recipes and loot tables. Run: python3 tools/make_json.py"""
import json
import os

RES = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources")
A = os.path.join(RES, "assets", "snhunt")
D = os.path.join(RES, "data", "snhunt")


def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


# ---------------------------------------------------------------- item models
GENERATED = ["salt", "emf_meter", "hunters_journal", "holy_water", "exorcism_rite", "dead_mans_blood", "colt_round"]
HANDHELD = ["salt_shotgun", "flare_gun", "silver_knife", "machete", "the_colt"]
for name in GENERATED:
    write(f"{A}/models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"snhunt:item/{name}"}})
for name in HANDHELD:
    write(f"{A}/models/item/{name}.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": f"snhunt:item/{name}"}})
write(f"{A}/models/item/devils_trap.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "snhunt:block/devils_trap"}})
write(f"{A}/models/item/grave.json", {"parent": "snhunt:block/grave"})
write(f"{A}/models/item/cursed_object.json", {"parent": "snhunt:block/cursed_object"})
for mob in ["spirit", "wendigo", "shapeshifter", "demon", "vampire", "shtriga", "daeva", "scarecrow", "greater_demon"]:
    write(f"{A}/models/item/{mob}_spawn_egg.json", {"parent": "minecraft:item/template_spawn_egg"})

# ---------------------------------------------------------------- block models + states
write(f"{A}/models/block/salt_line.json", {"parent": "minecraft:block/carpet", "textures": {"wool": "snhunt:block/salt_line", "particle": "snhunt:block/salt_line"}})
write(f"{A}/models/block/devils_trap.json", {"parent": "minecraft:block/carpet", "textures": {"wool": "snhunt:block/devils_trap", "particle": "snhunt:block/devils_trap"}})
write(f"{A}/models/block/grave.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
    "top": "snhunt:block/grave_top", "side": "snhunt:block/grave_side", "bottom": "minecraft:block/dirt"}})
write(f"{A}/models/block/grave_salted.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
    "top": "snhunt:block/grave_top_salted", "side": "snhunt:block/grave_side", "bottom": "minecraft:block/dirt"}})
write(f"{A}/models/block/cursed_object.json", {"parent": "minecraft:block/cube_all", "textures": {"all": "snhunt:block/cursed_object"}})

write(f"{A}/blockstates/salt_line.json", {"variants": {"": {"model": "snhunt:block/salt_line"}}})
write(f"{A}/blockstates/devils_trap.json", {"variants": {"": {"model": "snhunt:block/devils_trap"}}})
write(f"{A}/blockstates/cursed_object.json", {"variants": {"": {"model": "snhunt:block/cursed_object"}}})
write(f"{A}/blockstates/grave.json", {"variants": {
    "salted=false": {"model": "snhunt:block/grave"},
    "salted=true": {"model": "snhunt:block/grave_salted"}}})

# ---------------------------------------------------------------- loot tables (1.21 uses singular folder names)
def drops(block, item):
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": item}],
                                                   "conditions": [{"condition": "minecraft:survives_explosion"}]}]}


write(f"{D}/loot_table/blocks/salt_line.json", drops("salt_line", "snhunt:salt"))
write(f"{D}/loot_table/blocks/devils_trap.json", drops("devils_trap", "snhunt:devils_trap"))
# graves and cursed objects drop nothing on purpose

# ---------------------------------------------------------------- recipes
def item(i):
    return {"item": i}


def shaped(name, pattern, key, result, count=1):
    write(f"{D}/recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": "equipment", "pattern": pattern,
                                      "key": {k: (v if isinstance(v, dict) else item(v)) for k, v in key.items()},
                                      "result": {"id": result, "count": count}})


def shapeless(name, ingredients, result, count=1):
    write(f"{D}/recipe/{name}.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
                                      "ingredients": [item(i) for i in ingredients],
                                      "result": {"id": result, "count": count}})


shapeless("salt", ["minecraft:sugar", "minecraft:bone_meal"], "snhunt:salt", 4)
shaped("salt_shotgun", ["III", "GTP"], {"I": "minecraft:iron_ingot", "G": "minecraft:gunpowder",
                                        "T": "minecraft:tripwire_hook", "P": {"tag": "minecraft:planks"}}, "snhunt:salt_shotgun")
shaped("flare_gun", ["IO", "G "], {"I": "minecraft:iron_ingot", "O": "minecraft:orange_dye", "G": "minecraft:gunpowder"}, "snhunt:flare_gun")
shaped("emf_meter", ["T", "C", "I"], {"T": "minecraft:redstone_torch", "C": "minecraft:compass", "I": "minecraft:iron_ingot"}, "snhunt:emf_meter")
shapeless("hunters_journal", ["minecraft:book", "minecraft:leather", "minecraft:ink_sac"], "snhunt:hunters_journal")
shapeless("holy_water", ["minecraft:glass_bottle", "minecraft:glass_bottle", "minecraft:glass_bottle",
                         "minecraft:water_bucket", "snhunt:salt", "minecraft:gold_nugget"], "snhunt:holy_water", 3)
shapeless("exorcism_rite", ["minecraft:paper", "minecraft:ink_sac", "snhunt:salt", "minecraft:gold_nugget"], "snhunt:exorcism_rite")
shapeless("devils_trap", ["minecraft:red_dye", "minecraft:red_dye", "snhunt:salt"], "snhunt:devils_trap", 4)
shaped("silver_knife", ["I", "S"], {"I": "minecraft:iron_ingot", "S": "minecraft:stick"}, "snhunt:silver_knife")
shaped("machete", ["  I", " I ", "S  "], {"I": "minecraft:iron_ingot", "S": "minecraft:stick"}, "snhunt:machete")
shapeless("dead_mans_blood", ["minecraft:glass_bottle", "minecraft:rotten_flesh", "minecraft:red_dye"], "snhunt:dead_mans_blood")
shapeless("colt_round", ["minecraft:diamond", "minecraft:gold_ingot", "minecraft:gunpowder"], "snhunt:colt_round", 2)

print("json written")
