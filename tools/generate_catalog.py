#!/usr/bin/env python3
# SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
# Copyright (c) 2026 Muha
# Commercial use only with prior permission. See LICENSE.md.
"""
Erzeugt den Item-Katalog für den Shop aus den offenen Minecraft-Daten von PrismarineJS.

  - src/main/resources/catalog/items.csv   (minecraft_id, name, category, price)
  - src/main/resources/static/icons/*.png  (16x16 Item-Icons)

Die Preise sind generierte STARTWERTE (Heuristik nach Material und Item-Art).
Die echten Preise legt der Server fest und synchronisiert sie über PUT /api/items/prices.

Aufruf:  python3 tools/generate_catalog.py [minecraft-version]
"""
import base64
import csv
import json
import sys
import urllib.request
from pathlib import Path

VERSION = sys.argv[1] if len(sys.argv) > 1 else "26.1"
DATA = f"https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc/{VERSION}"
ASSETS = f"https://cdn.jsdelivr.net/gh/PrismarineJS/minecraft-assets@master/data/{VERSION}"

ROOT = Path(__file__).resolve().parent.parent
CSV_OUT = ROOT / "src/main/resources/catalog/items.csv"
ICON_DIR = ROOT / "src/main/resources/static/icons"


def fetch_json(url):
    with urllib.request.urlopen(url) as r:
        return json.load(r)


def fetch_bytes(url):
    try:
        with urllib.request.urlopen(url) as r:
            return r.read()
    except Exception:
        return None


# ---------------------------------------------------------------- Ausschlüsse

EXCLUDED = {
    "air", "bedrock", "barrier", "light", "structure_block", "structure_void", "jigsaw",
    "command_block", "chain_command_block", "repeating_command_block", "command_block_minecart",
    "debug_stick", "knowledge_book", "spawner", "trial_spawner", "vault", "end_portal_frame",
    "reinforced_deepslate", "budding_amethyst", "petrified_oak_slab", "farmland", "dirt_path",
    "test_block", "test_instance_block", "written_book", "filled_map", "player_head",
    "dragon_egg", "frogspawn",
}


def is_excluded(name):
    return name in EXCLUDED or name.endswith("_spawn_egg") or name.startswith("infested_")


# ---------------------------------------------------------------- Kategorien

POTIONS = {"potion", "splash_potion", "lingering_potion", "ominous_bottle", "experience_bottle"}
BOOKS = {"book", "writable_book", "enchanted_book"}
COMBAT = {"bow", "crossbow", "trident", "mace", "shield", "arrow", "spectral_arrow", "tipped_arrow",
          "totem_of_undying", "wind_charge", "turtle_helmet", "wolf_armor", "end_crystal"}
COMBAT_SUFFIX = ("_sword", "_spear", "_helmet", "_chestplate", "_leggings", "_boots", "_horse_armor",
                 "_nautilus_armor")
TOOLS = {"shears", "flint_and_steel", "fishing_rod", "carrot_on_a_stick", "warped_fungus_on_a_stick",
         "brush", "spyglass", "compass", "recovery_compass", "clock", "lead", "bucket", "name_tag",
         "saddle", "elytra", "map", "bundle", "fire_charge"}
TOOLS_SUFFIX = ("_pickaxe", "_shovel", "_hoe", "_axe", "_bucket", "_bundle")
INGREDIENTS = {
    "diamond", "emerald", "lapis_lazuli", "quartz", "coal", "charcoal", "stick", "string", "feather",
    "leather", "gunpowder", "bone", "bone_meal", "slime_ball", "ender_pearl", "ender_eye", "blaze_rod",
    "blaze_powder", "nether_wart", "sugar", "paper", "ghast_tear", "magma_cream", "fermented_spider_eye",
    "glistering_melon_slice", "rabbit_foot", "rabbit_hide", "phantom_membrane", "redstone",
    "glowstone_dust", "prismarine_shard", "prismarine_crystals", "nautilus_shell", "heart_of_the_sea",
    "nether_star", "shulker_shell", "echo_shard", "amethyst_shard", "netherite_scrap", "flint",
    "clay_ball", "brick", "nether_brick", "ink_sac", "glow_ink_sac", "honeycomb", "turtle_scute",
    "armadillo_scute", "breeze_rod", "resin_clump", "resin_brick", "dragon_breath", "wheat",
    "copper_nugget",
}
INGREDIENTS_SUFFIX = ("_ingot", "_nugget", "_dye", "_smithing_template", "_pottery_sherd")
UTILITIES = {
    "crafting_table", "furnace", "blast_furnace", "smoker", "chest", "trapped_chest", "ender_chest",
    "barrel", "hopper", "dropper", "dispenser", "observer", "piston", "sticky_piston", "anvil",
    "chipped_anvil", "damaged_anvil", "enchanting_table", "brewing_stand", "cauldron", "beacon",
    "conduit", "lectern", "loom", "cartography_table", "fletching_table", "smithing_table",
    "stonecutter", "grindstone", "composter", "jukebox", "note_block", "lodestone", "respawn_anchor",
    "crafter", "shulker_box", "lever", "redstone_torch", "repeater", "comparator", "daylight_detector",
    "target", "tripwire_hook", "rail", "powered_rail", "detector_rail", "activator_rail", "minecart",
    "chest_minecart", "furnace_minecart", "hopper_minecart", "tnt_minecart", "tnt", "bell",
    "campfire", "soul_campfire", "torch", "soul_torch", "lantern", "soul_lantern", "ladder",
    "scaffolding", "sculk_sensor", "calibrated_sculk_sensor", "redstone_lamp", "decorated_pot",
    "lightning_rod", "chiseled_bookshelf", "copper_torch", "copper_lantern",
}
UTILITIES_SUFFIX = ("_shulker_box", "_bed", "_bulb", "_shelf")
MISC_BLOCK_SUFFIX = ("_door", "_trapdoor", "_sign", "_hanging_sign", "_fence_gate", "_button",
                     "_pressure_plate", "_sapling", "_boat", "_raft", "_banner", "_candle", "_head",
                     "_skull", "_golem_statue")


def categorize(name, block_names, food_names):
    if name in POTIONS:
        return "POTIONS"
    if name in BOOKS:
        return "BOOKS"
    if name in COMBAT or name.endswith(COMBAT_SUFFIX):
        return "COMBAT"
    if name in TOOLS or name.endswith(TOOLS_SUFFIX):
        return "TOOLS"
    if name in food_names:
        return "FOOD"
    if name in INGREDIENTS or name.endswith(INGREDIENTS_SUFFIX) or (
            name.startswith("raw_") and not name.endswith("_block")):
        return "INGREDIENTS"
    if name in UTILITIES or name.endswith(UTILITIES_SUFFIX):
        return "UTILITIES"
    if name in block_names and not name.endswith(MISC_BLOCK_SUFFIX):
        return "BLOCKS"
    return "MISC"


# ---------------------------------------------------------------- Preise (Heuristik)

SPECIAL = {
    "elytra": 1000, "enchanted_golden_apple": 800, "beacon": 600, "nether_star": 500, "mace": 400,
    "heavy_core": 300, "netherite_upgrade_smithing_template": 300, "trident": 250,
    "heart_of_the_sea": 200, "totem_of_undying": 150, "conduit": 150, "shulker_box": 70,
    "enchanted_book": 50, "golden_apple": 40, "shulker_shell": 30, "echo_shard": 25,
    "experience_bottle": 5, "ender_pearl": 4, "blaze_rod": 4, "breeze_rod": 6, "ender_eye": 6,
    "netherite_scrap": 100, "ancient_debris": 110, "dragon_breath": 20, "end_crystal": 60,
    "beehive": 6, "bee_nest": 8, "sponge": 15, "wet_sponge": 15,
}

# Grundwert pro Material (Wert eines Barrens/Edelsteins bzw. eines Blocks)
MATERIAL = {
    "netherite": 400, "diamond": 60, "emerald": 40, "gold": 12, "golden": 12, "iron": 6,
    "copper": 1.5, "amethyst": 3, "lapis": 3, "redstone": 1, "quartz": 2, "obsidian": 5,
    "prismarine": 2, "purpur": 1.5, "end": 1, "blackstone": 0.4, "deepslate": 0.3, "tuff": 0.25,
    "stone": 0.2, "cobblestone": 0.1, "andesite": 0.15, "diorite": 0.15, "granite": 0.15,
    "sandstone": 0.3, "terracotta": 0.4, "concrete": 0.5, "glass": 0.4, "wool": 0.6,
    "chainmail": 8, "leather": 2, "wooden": 0.5, "coal": 0.5, "mud": 0.2, "brick": 0.8,
    "bricks": 0.8, "resin": 1, "nether": 0.3, "basalt": 0.2, "dripstone": 0.3, "calcite": 0.3,
}
WOODS = {"oak", "spruce", "birch", "jungle", "acacia", "dark", "mangrove", "cherry", "pale", "bamboo",
         "crimson", "warped"}
WOOD_KIND = {"log": 1, "wood": 1, "stem": 1, "hyphae": 1, "planks": 0.3, "slab": 0.15, "stairs": 0.4,
             "fence": 0.3, "gate": 0.5, "door": 0.4, "trapdoor": 0.6, "button": 0.1, "plate": 0.2,
             "sign": 0.5, "boat": 1.5, "raft": 1.5, "sapling": 0.5, "leaves": 0.05, "shelf": 1,
             "mosaic": 0.3, "block": 1}
KIND = {"block": 9, "helmet": 5, "chestplate": 8, "leggings": 7, "boots": 4, "sword": 2, "spear": 2,
        "pickaxe": 3, "axe": 3, "shovel": 1, "hoe": 2, "armor": 7, "nugget": 0.11, "slab": 0.5,
        "stairs": 1, "wall": 1, "door": 2, "trapdoor": 3, "bars": 0.4, "plate": 2}
CATEGORY_DEFAULT = {"BLOCKS": 0.3, "MISC": 1, "FOOD": 0.8, "INGREDIENTS": 1.5, "TOOLS": 8,
                    "COMBAT": 10, "POTIONS": 6, "BOOKS": 4, "UTILITIES": 4}


def round_price(p):
    if p < 1:
        step = 0.05
    elif p < 10:
        step = 0.1
    elif p < 100:
        step = 1
    else:
        step = 5
    return max(0.05, round(round(p / step) * step, 2))


def price_for(name, category, stack_size):
    if name in SPECIAL:
        return SPECIAL[name]
    if name.startswith("music_disc_"):
        return 30
    if name.endswith("_smithing_template"):
        return 50
    tokens = name.split("_")
    last = tokens[-1]
    if tokens[0] in WOODS:
        return round_price(WOOD_KIND.get(last, 0.5) * (2 if tokens[0] in {"crimson", "warped"} else 1))
    base = next((MATERIAL[t] for t in tokens if t in MATERIAL), None)
    if base is None:
        price = CATEGORY_DEFAULT[category]
    else:
        price = base * KIND.get(last, 1)
    if stack_size == 1 and base is None:
        price *= 2
    return round_price(price)


# ---------------------------------------------------------------- main

def main():
    print(f"Lade Minecraft-Daten {VERSION} ...")
    items = fetch_json(f"{DATA}/items.json")
    block_names = {b["name"] for b in fetch_json(f"{DATA}/blocks.json")}
    food_names = {f["name"] for f in fetch_json(f"{DATA}/foods.json")}
    textures = {t["name"]: t["texture"] for t in fetch_json(f"{ASSETS}/texture_content.json")}

    ICON_DIR.mkdir(parents=True, exist_ok=True)
    CSV_OUT.parent.mkdir(parents=True, exist_ok=True)

    rows, missing_icons = [], []
    for item in items:
        name = item["name"]
        if is_excluded(name):
            continue
        category = categorize(name, block_names, food_names)
        rows.append([name, item["displayName"], category, f"{price_for(name, category, item['stackSize']):g}"])

        icon = ICON_DIR / f"{name}.png"
        if icon.exists():
            continue
        tex = textures.get(name)
        data = base64.b64decode(tex.split(",", 1)[1]) if tex else (
            fetch_bytes(f"{ASSETS}/items/{name}.png") or fetch_bytes(f"{ASSETS}/blocks/{name}.png"))
        if data:
            icon.write_bytes(data)
        else:
            missing_icons.append(name)

    rows.sort(key=lambda r: r[0])
    with CSV_OUT.open("w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["minecraft_id", "name", "category", "price"])
        w.writerows(rows)

    print(f"{len(rows)} Items -> {CSV_OUT.relative_to(ROOT)}")
    print(f"{len(missing_icons)} ohne Icon (Frontend zeigt Platzhalter)")


if __name__ == "__main__":
    main()
