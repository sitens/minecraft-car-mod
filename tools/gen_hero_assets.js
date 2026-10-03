// Writes the JSON assets for the AI Crafting Table and the hero gear. Run: node tools/gen_hero_assets.js
const fs = require("fs");
const RES = "src/main/resources";
const A = RES + "/assets/carmod";
const D = RES + "/data/carmod";
const w = (p, o) => { fs.mkdirSync(require("path").dirname(p), { recursive: true }); fs.writeFileSync(p, typeof o === "string" ? o : JSON.stringify(o, null, 2) + "\n"); };

const tint = [{ type: "minecraft:dye", default: -1 }];
const gear = { hero_blade: "item/hero_blade", hero_axe: "item/hero_axe", hero_pickaxe: "item/hero_pickaxe", hero_shovel: "item/hero_shovel", hero_suit: "item/hero_suit" };
for (const [id, tex] of Object.entries(gear)) {
  w(`${A}/models/item/${id}.json`, { parent: "minecraft:item/handheld", textures: { layer0: "carmod:" + tex } });
  w(`${A}/items/${id}.json`, { model: { type: "minecraft:model", model: "carmod:item/" + id, tints: tint } });
}
w(`${A}/equipment/hero_suit.json`, { layers: { humanoid: [{ texture: "carmod:hero_suit", dyeable: { color_when_undyed: -1 } }] } });

w(`${A}/models/block/ai_crafting_table.json`, {
  parent: "minecraft:block/cube_bottom_top",
  textures: { bottom: "carmod:block/ai_crafting_table_bottom", top: "carmod:block/ai_crafting_table_top", side: "carmod:block/ai_crafting_table_side", particle: "carmod:block/ai_crafting_table_side" },
});
w(`${A}/blockstates/ai_crafting_table.json`, { variants: { "": { model: "carmod:block/ai_crafting_table" } } });
w(`${A}/items/ai_crafting_table.json`, { model: { type: "minecraft:model", model: "carmod:block/ai_crafting_table" } });
w(`${D}/loot_table/blocks/ai_crafting_table.json`, {
  type: "minecraft:block",
  pools: [{ rolls: 1, bonus_rolls: 0, entries: [{ type: "minecraft:item", name: "carmod:ai_crafting_table" }], conditions: [{ condition: "minecraft:survives_explosion" }] }],
});
w(`${D}/recipe/ai_crafting_table.json`, {
  type: "minecraft:crafting_shaped", category: "misc",
  pattern: ["DRRD", "LCOL", "IIII", "    "],
  key: { D: "minecraft:diamond", R: "minecraft:redstone_block", L: "minecraft:lapis_block", C: "carmod:crafting_table_4x4", O: "minecraft:observer", I: "minecraft:iron_block" },
  result: { id: "carmod:ai_crafting_table", count: 1 },
});

const pick = RES + "/data/minecraft/tags/block/mineable/pickaxe.json";
const tags = JSON.parse(fs.readFileSync(pick, "utf8"));
if (!tags.values.includes("carmod:ai_crafting_table")) tags.values.push("carmod:ai_crafting_table");
w(pick, tags);
const iron = RES + "/data/minecraft/tags/block/needs_iron_tool.json";
const needs = JSON.parse(fs.readFileSync(iron, "utf8"));
if (!needs.values.includes("carmod:ai_crafting_table")) needs.values.push("carmod:ai_crafting_table");
w(iron, needs);

const langPath = A + "/lang/en_us.json";
const lang = JSON.parse(fs.readFileSync(langPath, "utf8"));
Object.assign(lang, {
  "block.carmod.ai_crafting_table": "AI Crafting Table",
  "container.carmod.ai_workshop": "AI Crafting Table",
  "key.carmod.ability": "Use suit ability",
  "key.carmod.cycle_ability": "Switch ability",
  "key.category.carmod.hero": "Car Mod Heroes",
  "item.carmod.hero_suit": "Hero Suit",
  "item.carmod.hero_blade": "Hero Blade",
  "item.carmod.hero_axe": "Hero Axe",
  "item.carmod.hero_pickaxe": "Hero Pickaxe",
  "item.carmod.hero_shovel": "Hero Shovel",
});
w(langPath, lang);
console.log("hero assets written");
