#!/usr/bin/env bash
# Generates the JSON data/asset files for the vertical slabs plus the other
# batch-2 content (5x5 table, tack, wooden buckets, hOesaC), and rewrites
# en_us.json. Run from the mod folder:
#
#   VANILLA=/path/to/extracted/minecraft/client/jar bash tools/gen_assets.sh
#
# VANILLA must contain the unzipped 1.21.11 client jar (assets/ and data/),
# used to copy each vanilla slab's textures and full-block model.
set -euo pipefail

VANILLA="${VANILLA:-C:/Users/Siten/tools/mc_client}"
RES="src/main/resources"
A="$RES/assets/carmod"
D="$RES/data/carmod"
mkdir -p "$A/blockstates" "$A/models/block" "$A/models/item" "$A/items" "$A/lang" \
         "$D/recipe" "$D/loot_table/blocks" "$D/loot_table/entities" "$D/damage_type" "$D/tags/item" \
         "$RES/data/minecraft/tags/block/mineable" "$RES/data/minecraft/tags/damage_type"

WOODEN="acacia bamboo bamboo_mosaic birch cherry crimson dark_oak jungle mangrove oak pale_oak spruce warped"
BASES=$(sed -n '/VERTICAL_SLAB_BASES = List.of(/,/);/p' src/main/java/com/kiancars/carmod/registry/ModBlocks.java \
        | grep -o '"[a-z_]*"' | tr -d '"')

title() { echo "$1" | sed -E 's/(^|_)([a-z])/ \U\2/g; s/^ //'; }
# Waxed copper slabs have no model of their own, so follow the slab's
# blockstate to whichever model its bottom half actually uses.
slab_model_file() {
  local model
  model=$(tr -d ' \n' < "$VANILLA/assets/minecraft/blockstates/$1_slab.json" \
          | sed 's/.*"type=bottom":{"model":"minecraft:\([^"]*\)".*/\1/')
  echo "$VANILLA/assets/minecraft/models/$model.json"
}
tex() { sed -n "s/.*\"$2\": *\"\([^\"]*\)\".*/\1/p" "$(slab_model_file "$1")"; }

# ------------------------------------------------------------- vertical slabs
cat > "$A/models/block/vertical_slab.json" <<'EOF'
{
  "parent": "minecraft:block/block",
  "textures": { "particle": "#side" },
  "elements": [
    {
      "from": [0, 0, 0],
      "to": [16, 16, 8],
      "faces": {
        "down":  { "texture": "#bottom", "cullface": "down" },
        "up":    { "texture": "#top",    "cullface": "up" },
        "north": { "texture": "#side",   "cullface": "north" },
        "south": { "texture": "#side" },
        "west":  { "texture": "#side",   "cullface": "west" },
        "east":  { "texture": "#side",   "cullface": "east" }
      }
    }
  ]
}
EOF

AXE_LIST=""
PICK_LIST=""
LANG_SLABS=""
for base in $BASES; do
  name="${base}_vertical_slab"
  bottom=$(tex "$base" bottom); top=$(tex "$base" top); side=$(tex "$base" side)
  full=$(tr -d ' \n' < "$VANILLA/assets/minecraft/blockstates/${base}_slab.json" \
         | sed 's/.*"type=double":{"model":"\([^"]*\)".*/\1/')

  cat > "$A/models/block/$name.json" <<EOF
{
  "parent": "carmod:block/vertical_slab",
  "textures": {
    "bottom": "$bottom",
    "top": "$top",
    "side": "$side"
  }
}
EOF

  cat > "$A/blockstates/$name.json" <<EOF
{
  "variants": {
    "double=false,facing=north": { "model": "carmod:block/$name", "uvlock": true },
    "double=false,facing=east":  { "model": "carmod:block/$name", "y": 90,  "uvlock": true },
    "double=false,facing=south": { "model": "carmod:block/$name", "y": 180, "uvlock": true },
    "double=false,facing=west":  { "model": "carmod:block/$name", "y": 270, "uvlock": true },
    "double=true,facing=north":  { "model": "$full" },
    "double=true,facing=east":   { "model": "$full" },
    "double=true,facing=south":  { "model": "$full" },
    "double=true,facing=west":   { "model": "$full" }
  }
}
EOF

  cat > "$A/items/$name.json" <<EOF
{
  "model": {
    "type": "minecraft:model",
    "model": "carmod:block/$name"
  }
}
EOF

  cat > "$D/loot_table/blocks/$name.json" <<EOF
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "bonus_rolls": 0,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "carmod:$name",
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": 2,
              "add": false,
              "conditions": [
                {
                  "condition": "minecraft:block_state_property",
                  "block": "carmod:$name",
                  "properties": { "double": "true" }
                }
              ]
            },
            { "function": "minecraft:explosion_decay" }
          ]
        }
      ]
    }
  ]
}
EOF

  cat > "$D/recipe/$name.json" <<EOF
{
  "type": "minecraft:crafting_shaped",
  "category": "building",
  "pattern": ["#", "#", "#"],
  "key": { "#": "minecraft:${base}_slab" },
  "result": { "id": "carmod:$name", "count": 3 }
}
EOF

  cat > "$D/recipe/${base}_slab_from_vertical_slab.json" <<EOF
{
  "type": "minecraft:crafting_shapeless",
  "category": "building",
  "ingredients": ["carmod:$name"],
  "result": { "id": "minecraft:${base}_slab", "count": 1 }
}
EOF

  if [[ " $WOODEN " == *" $base "* ]]; then
    AXE_LIST="$AXE_LIST\"carmod:$name\","
  else
    PICK_LIST="$PICK_LIST\"carmod:$name\","
  fi
  LANG_SLABS="$LANG_SLABS  \"block.carmod.$name\": \"$(title "$base") Vertical Slab\",\n"
done

# ------------------------------------------------------------ crafting tables
for size in 4x4 5x5; do
  side="minecraft:block/crafting_table_side"
  [[ $size == 5x5 ]] && side="minecraft:block/crafting_table_front"
  cat > "$A/models/block/crafting_table_$size.json" <<EOF
{
  "parent": "minecraft:block/cube_bottom_top",
  "textures": {
    "bottom": "minecraft:block/oak_planks",
    "top": "minecraft:block/crafting_table_top",
    "side": "$side"
  }
}
EOF
  cat > "$A/blockstates/crafting_table_$size.json" <<EOF
{
  "variants": {
    "": { "model": "carmod:block/crafting_table_$size" }
  }
}
EOF
  cat > "$A/items/crafting_table_$size.json" <<EOF
{
  "model": {
    "type": "minecraft:model",
    "model": "carmod:block/crafting_table_$size"
  }
}
EOF
  cat > "$D/loot_table/blocks/crafting_table_$size.json" <<EOF
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "bonus_rolls": 0,
      "entries": [ { "type": "minecraft:item", "name": "carmod:crafting_table_$size" } ],
      "conditions": [ { "condition": "minecraft:survives_explosion" } ]
    }
  ]
}
EOF
done
rm -f "$A/models/item/crafting_table_4x4.json"

cat > "$D/recipe/crafting_table_5x5.json" <<'EOF'
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": ["PPPP", "PPPP", "PPPP", "PPPP"],
  "key": { "P": "#minecraft:planks" },
  "result": { "id": "carmod:crafting_table_5x5", "count": 1 }
}
EOF

# ---------------------------------------------------------------------- tack
cat > "$A/models/block/tack.json" <<'EOF'
{
  "textures": { "particle": "carmod:block/tack", "post": "carmod:block/tack" },
  "elements": [
    {
      "from": [7, 0, 7],
      "to": [9, 16, 9],
      "faces": {
        "down":  { "texture": "#post", "cullface": "down" },
        "up":    { "texture": "#post" },
        "north": { "texture": "#post" },
        "south": { "texture": "#post" },
        "west":  { "texture": "#post" },
        "east":  { "texture": "#post" }
      }
    }
  ]
}
EOF
cat > "$A/models/block/tack_top.json" <<'EOF'
{
  "textures": { "particle": "carmod:block/tack", "post": "carmod:block/tack" },
  "elements": [
    {
      "from": [7, 0, 7],
      "to": [9, 12, 9],
      "faces": {
        "up":    { "texture": "#post" },
        "north": { "texture": "#post" },
        "south": { "texture": "#post" },
        "west":  { "texture": "#post" },
        "east":  { "texture": "#post" }
      }
    },
    {
      "from": [7.5, 12, 7.5],
      "to": [8.5, 16, 8.5],
      "faces": {
        "up":    { "texture": "#post" },
        "north": { "texture": "#post" },
        "south": { "texture": "#post" },
        "west":  { "texture": "#post" },
        "east":  { "texture": "#post" }
      }
    }
  ]
}
EOF
cat > "$A/blockstates/tack.json" <<'EOF'
{
  "variants": {
    "half=lower": { "model": "carmod:block/tack" },
    "half=upper": { "model": "carmod:block/tack_top" }
  }
}
EOF

# ---------------------------------------------------- simple flat item icons
for item in tack wooden_bucket wooden_water_bucket wooden_lava_bucket; do
  cat > "$A/models/item/$item.json" <<EOF
{
  "parent": "minecraft:item/generated",
  "textures": { "layer0": "carmod:item/$item" }
}
EOF
done
for egg in hoesac_spawn_egg hoesac_final_boss_spawn_egg; do
cat > "$A/models/item/$egg.json" <<'EOF'
{
  "parent": "minecraft:item/generated",
  "textures": { "layer0": "minecraft:item/zombie_spawn_egg" }
}
EOF
done
for item in tack wooden_bucket wooden_water_bucket wooden_lava_bucket hoesac_spawn_egg hoesac_final_boss_spawn_egg; do
  cat > "$A/items/$item.json" <<EOF
{
  "model": {
    "type": "minecraft:model",
    "model": "carmod:item/$item"
  }
}
EOF
done

# Only the bottom half drops a tack. Breaking either half also removes the
# other one, and without this check both halves dropped (2 tacks).
cat > "$D/loot_table/blocks/tack.json" <<'EOF'
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "bonus_rolls": 0,
      "entries": [ { "type": "minecraft:item", "name": "carmod:tack" } ],
      "conditions": [
        { "condition": "minecraft:survives_explosion" },
        {
          "condition": "minecraft:block_state_property",
          "block": "carmod:tack",
          "properties": { "half": "lower" }
        }
      ]
    }
  ]
}
EOF
cat > "$D/recipe/tack.json" <<'EOF'
{
  "type": "minecraft:crafting_shaped",
  "category": "equipment",
  "pattern": ["#", "#"],
  "key": { "#": "minecraft:iron_ingot" },
  "result": { "id": "carmod:tack", "count": 5 }
}
EOF
cat > "$D/recipe/wooden_bucket.json" <<'EOF'
{
  "type": "minecraft:crafting_shaped",
  "category": "tools",
  "pattern": [" S ", "P P", " P "],
  "key": { "S": "minecraft:stick", "P": "minecraft:oak_planks" },
  "result": { "id": "carmod:wooden_bucket", "count": 1 }
}
EOF

cat > "$D/damage_type/tack.json" <<'EOF'
{
  "message_id": "carmod.tack",
  "exhaustion": 0.0,
  "scaling": "never"
}
EOF
cat > "$D/damage_type/hoesac_roll.json" <<'EOF'
{
  "message_id": "carmod.hoesac_roll",
  "exhaustion": 0.0,
  "scaling": "never"
}
EOF
cat > "$D/damage_type/hoesac_suffocate.json" <<'EOF'
{
  "message_id": "carmod.hoesac_suffocate",
  "exhaustion": 0.0,
  "scaling": "never"
}
EOF
for tag in bypasses_armor bypasses_effects bypasses_enchantments bypasses_resistance; do
  cat > "$RES/data/minecraft/tags/damage_type/$tag.json" <<'EOF'
{
  "replace": false,
  "values": ["carmod:tack", "carmod:hoesac_roll", "carmod:hoesac_suffocate"]
}
EOF
done

# ------------------------------------------------------------------- hOesaC
cat > "$D/loot_table/entities/hoesac.json" <<'EOF'
{
  "type": "minecraft:entity",
  "pools": [
    {
      "rolls": 1,
      "bonus_rolls": 0,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "minecraft:sugar",
          "functions": [
            { "function": "minecraft:set_count", "count": { "type": "minecraft:uniform", "min": 0, "max": 2 } }
          ]
        }
      ]
    }
  ]
}
EOF
# ---------------------------------------------------------------- junk food
# name|Display Name|recipe row (4 items, left to right)
JUNK_FOODS="
dorinos|Dorinos|minecraft:paper minecraft:wheat minecraft:wheat minecraft:orange_dye
fritoz|Fritoz|minecraft:paper minecraft:wheat minecraft:wheat minecraft:wheat
layz|Layz Chips|minecraft:paper minecraft:potato minecraft:potato minecraft:potato
layz_bbq|Layz BBQ Chips|minecraft:paper minecraft:potato minecraft:potato minecraft:red_dye
candy_bar|Candy Bar|minecraft:paper minecraft:cocoa_beans minecraft:sugar minecraft:cocoa_beans
soda|Soda|minecraft:glass_bottle minecraft:sugar minecraft:sugar minecraft:red_dye
gummy_worms|Gummy Worms|minecraft:slime_ball minecraft:sugar minecraft:sugar minecraft:slime_ball
donut|Donut|minecraft:wheat minecraft:egg minecraft:sugar minecraft:pink_dye
lollipop|Lollipop|minecraft:stick minecraft:sugar minecraft:sugar minecraft:red_dye
popcorn|Popcorn|minecraft:bowl minecraft:wheat minecraft:wheat minecraft:wheat
pizza_slice|Pizza Slice|minecraft:bread minecraft:cooked_porkchop minecraft:red_mushroom minecraft:bread
hot_dog|Hot Dog|minecraft:bread minecraft:cooked_porkchop minecraft:cooked_porkchop minecraft:bread
ice_cream|Ice Cream|minecraft:snowball minecraft:sugar minecraft:sugar minecraft:wheat
cotton_candy|Cotton Candy|minecraft:stick minecraft:sugar minecraft:sugar minecraft:sugar
cheezy_puffs|Cheezy Puffs|minecraft:paper minecraft:wheat minecraft:yellow_dye minecraft:yellow_dye
"
JUNK_TAG=""
LANG_JUNK=""
while IFS='|' read -r id display row; do
  [[ -z "$id" ]] && continue
  read -r i1 i2 i3 i4 <<< "$row"
  # One letter per distinct ingredient, in the order they appear.
  keys=""; pattern=""; declare -A letter=(); next=0; letters=(A B C D)
  for ing in $i1 $i2 $i3 $i4; do
    if [[ -z "${letter[$ing]:-}" ]]; then
      letter[$ing]=${letters[$next]}; next=$((next + 1))
      keys="$keys\"${letter[$ing]}\": \"$ing\", "
    fi
    pattern="$pattern${letter[$ing]}"
  done
  unset letter
  cat > "$D/recipe/$id.json" <<EOF
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": ["$pattern"],
  "key": { ${keys%, } },
  "result": { "id": "carmod:$id", "count": 1 }
}
EOF
  cat > "$A/models/item/$id.json" <<EOF
{
  "parent": "minecraft:item/generated",
  "textures": { "layer0": "carmod:item/$id" }
}
EOF
  cat > "$A/items/$id.json" <<EOF
{
  "model": {
    "type": "minecraft:model",
    "model": "carmod:item/$id"
  }
}
EOF
  JUNK_TAG="$JUNK_TAG\"carmod:$id\","
  LANG_JUNK="$LANG_JUNK  \"item.carmod.$id\": \"$display\",\n"
done <<< "$JUNK_FOODS"

cat > "$D/tags/item/junk_food.json" <<EOF
{
  "values": [${JUNK_TAG%,}]
}
EOF

# ------------------------------------------------------------ mining tags
cat > "$RES/data/minecraft/tags/block/mineable/axe.json" <<EOF
{
  "replace": false,
  "values": [${AXE_LIST}"carmod:crafting_table_4x4","carmod:crafting_table_5x5"]
}
EOF
cat > "$RES/data/minecraft/tags/block/mineable/pickaxe.json" <<EOF
{
  "replace": false,
  "values": [${PICK_LIST}"carmod:tack"]
}
EOF

# -------------------------------------------------------------------- lang
{
  cat <<'EOF'
{
  "itemGroup.carmod.car_tab": "Car Mod",
  "block.carmod.crafting_table_4x4": "4x4 Crafting Table",
  "container.carmod.crafting_table_4x4": "4x4 Crafting Table",
  "block.carmod.crafting_table_5x5": "5x5 Crafting Table",
  "container.carmod.crafting_table_5x5": "5x5 Crafting Table",
  "item.carmod.car_sedan": "Sedan",
  "item.carmod.car_pickup": "Pickup Truck",
  "item.carmod.car_offroader": "Off-Roader",
  "item.carmod.car_sports_car": "Sports Car",
  "item.carmod.car_limousine": "Limousine",
  "item.carmod.car_racer": "Racer",
  "item.carmod.car_monster_truck": "Monster Truck",
  "item.carmod.car_armored_car": "Armored Car",
  "item.carmod.car_hover_prototype": "Hover Prototype",
  "item.carmod.car_golden_luxury": "Golden Luxury Car",
  "block.carmod.tack": "Tack",
  "item.carmod.wooden_bucket": "Wooden Bucket",
  "item.carmod.wooden_water_bucket": "Wooden Water Bucket",
  "item.carmod.wooden_lava_bucket": "Wooden Lava Bucket",
  "entity.carmod.hoesac": "hOesaC",
  "item.carmod.hoesac_spawn_egg": "hOesaC Spawn Egg",
  "entity.carmod.hoesac_final_boss": "hOesaC Final Boss",
  "item.carmod.hoesac_final_boss_spawn_egg": "hOesaC Final Boss Spawn Egg",
  "death.attack.carmod.hoesac_suffocate": "%1$s got suffocated",
  "death.attack.carmod.hoesac_suffocate.player": "%1$s got suffocated by %2$s",
  "death.attack.carmod.hoesac_roll": "%1$s was flattened",
  "death.attack.carmod.hoesac_roll.player": "%1$s was flattened by %2$s",
  "death.attack.carmod.tack": "%1$s stepped on a tack",
  "death.attack.carmod.tack.player": "%1$s stepped on a tack while fighting %2$s",
EOF
  printf "%b" "$LANG_JUNK"
  printf "%b" "$LANG_SLABS" | sed '$ s/,$//'
  echo "}"
} > "$A/lang/en_us.json"

echo "generated assets for $(echo $BASES | wc -w) vertical slabs"

# ------------------------------------------------- batch 5: beans, altar, loot
mkdir -p "$D/loot_modifiers" "$RES/data/neoforge/loot_modifiers" "$A/textures/mob_effect"

# Can of beans
cat > "$D/recipe/can_of_beans.json" <<'JSON'
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": ["ABBA"],
  "key": { "A": "minecraft:iron_ingot", "B": "minecraft:cocoa_beans" },
  "result": { "id": "carmod:can_of_beans", "count": 1 }
}
JSON
cat > "$A/models/item/can_of_beans.json" <<'JSON'
{
  "parent": "minecraft:item/generated",
  "textures": { "layer0": "carmod:item/can_of_beans" }
}
JSON
cat > "$A/items/can_of_beans.json" <<'JSON'
{
  "model": {
    "type": "minecraft:model",
    "model": "carmod:item/can_of_beans"
  }
}
JSON

# Altar: 4 from the 4x4 table. Rows top to bottom: empty / diamond, crafting table, crafting table, diamond / empty, mangrove log, mangrove log, empty / 4 obsidian.
cat > "$D/recipe/altar.json" <<'JSON'
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "    ",
    "DTTD",
    " LL ",
    "OOOO"
  ],
  "key": {
    "D": "minecraft:diamond",
    "T": "minecraft:crafting_table",
    "L": "minecraft:mangrove_log",
    "O": "minecraft:obsidian"
  },
  "result": { "id": "carmod:altar", "count": 4 }
}
JSON
cat > "$A/models/block/altar.json" <<'JSON'
{
  "parent": "minecraft:block/block",
  "textures": {
    "particle": "minecraft:block/obsidian",
    "base": "minecraft:block/polished_blackstone",
    "top": "minecraft:block/obsidian"
  },
  "elements": [
    { "from": [1, 0, 1], "to": [15, 3, 15],
      "faces": { "down": {"texture": "#base", "cullface": "down"}, "up": {"texture": "#base"}, "north": {"texture": "#base"}, "south": {"texture": "#base"}, "west": {"texture": "#base"}, "east": {"texture": "#base"} } },
    { "from": [5, 3, 5], "to": [11, 9, 11],
      "faces": { "north": {"texture": "#base"}, "south": {"texture": "#base"}, "west": {"texture": "#base"}, "east": {"texture": "#base"} } },
    { "from": [2, 9, 2], "to": [14, 12, 14],
      "faces": { "down": {"texture": "#base"}, "up": {"texture": "#top"}, "north": {"texture": "#base"}, "south": {"texture": "#base"}, "west": {"texture": "#base"}, "east": {"texture": "#base"} } }
  ]
}
JSON
cat > "$A/models/block/altar_filled.json" <<'JSON'
{
  "parent": "carmod:block/altar",
  "textures": {
    "top": "minecraft:block/crying_obsidian"
  }
}
JSON
{
  echo '{'
  echo '  "variants": {'
  echo '    "food=0": { "model": "carmod:block/altar" },'
  for i in $(seq 1 15); do
    sep=","; [[ $i == 15 ]] && sep=""
    echo "    \"food=$i\": { \"model\": \"carmod:block/altar_filled\" }$sep"
  done
  echo '  }'
  echo '}'
} > "$A/blockstates/altar.json"
cat > "$A/items/altar.json" <<'JSON'
{
  "model": {
    "type": "minecraft:model",
    "model": "carmod:block/altar"
  }
}
JSON
cat > "$D/loot_table/blocks/altar.json" <<'JSON'
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "bonus_rolls": 0,
      "entries": [ { "type": "minecraft:item", "name": "carmod:altar" } ],
      "conditions": [ { "condition": "minecraft:survives_explosion" } ]
    }
  ]
}
JSON

# Junk food in every chest loot table
cat > "$D/loot_modifiers/junk_food_in_chests.json" <<'JSON'
{
  "type": "carmod:junk_food_in_chests",
  "conditions": []
}
JSON
cat > "$RES/data/neoforge/loot_modifiers/global_loot_modifiers.json" <<'JSON'
{
  "replace": false,
  "entries": ["carmod:junk_food_in_chests"]
}
JSON

# Mining tags: the altar needs a pickaxe (added on top of the earlier tag files)
perl -0pi -e 's|"carmod:tack"\]|"carmod:tack","carmod:altar"]|' "$RES/data/minecraft/tags/block/mineable/pickaxe.json"
mkdir -p "$RES/data/minecraft/tags/block/needs_iron_tool" 2>/dev/null
cat > "$RES/data/minecraft/tags/block/needs_iron_tool.json" <<'JSON'
{
  "replace": false,
  "values": ["carmod:altar"]
}
JSON
rmdir "$RES/data/minecraft/tags/block/needs_iron_tool" 2>/dev/null || true

# Extra names (appended to en_us.json)
EXTRA_LANG='  "block.carmod.altar": "Altar",
  "item.carmod.can_of_beans": "Can of Beans",
  "effect.carmod.farting": "Farting",
  "message.carmod.altar_took": "Took back: %s",
  "message.carmod.boss_summoned": "The ground shakes... the hOesaC Final Boss has arrived!",'
perl -0pi -e "s|(  \"itemGroup.carmod.car_tab\": \"Car Mod\",\n)|\$1$EXTRA_LANG\n|" "$A/lang/en_us.json"
echo "batch 5 assets done"
node tools/gen_hero_assets.js
