# Car Mod

NeoForge mod for Minecraft Java Edition **1.21.11**. Targeting release on Modrinth.

Mod id: `carmod` / package `com.kiancars.carmod`.

**[Download the latest build (carmod-0.5.0.jar)](https://github.com/sitens/minecraft-car-mod/releases/tag/v0.5.0)**

## Changelog

Newest first. Each version says what's new, what got fixed, and what changed.

### v0.5.0 — 2026-09-30
- **Changed:** The hOesaC Final Boss now has **100 hearts** (was 25) and is **7 blocks wide, tall, and deep** (was 16).
- **Changed:** The regular hOesaC now has **20 hearts** (was 7) and walks a little faster than a zombie.
- **New:** **Altars** and a way to summon the Final Boss for real: build the ring, fill it with 12 different junk foods, and blow up TNT in the middle. (Recipe and ring layout are below in "What's in it".)
- **New:** Junk food now shows up in **chests in villages, abandoned villages, pillager outposts, trial chambers, and every other structure with loot.**
- **New:** **5 more junk foods** (Pizza Slice, Hot Dog, Ice Cream, Cotton Candy, Cheezy Puffs), so there are 15 now.
- **New:** **Can of Beans**: fills 3 hunger bars and gives you **Farting** for 1 minute. Every half a second you get boosted about 1 block into the air and let out a toot.
- **Changed:** Junk food now fills **5 hunger bars** (was 2½). Two minutes after you eat any, you get **Hunger for 20 seconds**.
- **Heads up:** The fart sound is the pufferfish "pffft" pitched down, because regular Minecraft has no real fart sound.

### v0.4.0 — 2026-09-30
- **New:** The **hOesaC Final Boss**: 25 hearts, a purple boss bar, and a giant round body (16 blocks wide, tall, and deep) with tiny arms and legs and a medium-size head. Get him with his spawn egg for now.
- **New:** His roll attack (charges at you, 2 hearts through armor and effects) and his jump-and-land attack (7 hearts, "[name] got suffocated by hOesaC Final Boss").
- **New:** Tacks stop his roll. He bounces off them, loses 2 hearts each time, then tries again.
- **Heads up:** His attacks are brand new and haven't been tested against a real player yet. Try him in a big, flat, open area.

### v0.3.0 — 2026-09-29
- **New:** 10 junk foods (Dorinos, Fritoz, Layz Chips, Layz BBQ Chips, Candy Bar, Soda, Gummy Worms, Donut, Lollipop, Popcorn). Each fills 2½ hunger bars.
- **New:** hOesaC now shows up by himself when you carry junk food in Survival.
- **Fixed:** Tacks dropped 2 when broken. Now they drop 1.
- **Changed:** Cars are fast on land and slow on water (it was the other way around).
- **Changed:** Cars can drive up 1-block bumps without getting stuck.

### v0.2.0 — 2026-09-27
- **Fixed:** The game crashed on startup. That's fixed now.
- **Fixed:** None of the car recipes worked. They all work now.
- **New:** 5x5 Crafting Table (16 planks in the 4x4 table).
- **New:** Standing-up slabs for all 61 kinds of slabs.
- **New:** Tacks (3 hearts of damage, even through armor).
- **New:** Wooden Bucket (holds water or lava, wears out fast).
- **New:** hOesaC, a mob with 7 hearts who doesn't burn in the sun (spawn egg only until junk food came in v0.3.0).

### v0.1.1 — 2026-09-20
- **Fixed:** The game crashed on launch because of missing picture files for the items. (It still crashed on startup for a different reason, which got fixed in v0.2.0.)

### v0.1.0 — 2026-09-19
- **New:** First test version: 10 cars you can ride, and the 4x4 Crafting Table you need to make them.

## What's in it

- **10 cars** you can ride — fast on land, slow on water, and they can drive up 1-block bumps
- **4x4 Crafting Table** — 9 planks in a normal crafting table
- **5x5 Crafting Table** — 16 planks filling the 4x4 table
- **Standing-up slabs** for every slab in the game (61 of them) — 3 normal slabs stacked in a column make 3 standing slabs. Put one back in the crafting grid to turn it back into a normal slab.
- **Tacks** — 2 iron ingots stacked make 5 tacks. They're thin and 2 blocks tall. Walking into one does 3 hearts of damage, straight through armor. Breaking one gives back 1 tack.
- **15 junk foods** — each fills 5 hunger bars. Two minutes after you eat one, you get Hunger for 20 seconds. They also turn up in chests everywhere (villages, outposts, trial chambers...). Made in the 4x4 or 5x5 table, one row of 4 (left → right):

  | Junk food | Recipe |
  |---|---|
  | Dorinos | Paper, Wheat, Wheat, Orange Dye |
  | Fritoz | Paper, Wheat, Wheat, Wheat |
  | Layz Chips | Paper, Potato, Potato, Potato |
  | Layz BBQ Chips | Paper, Potato, Potato, Red Dye |
  | Candy Bar | Paper, Cocoa Beans, Sugar, Cocoa Beans |
  | Soda | Glass Bottle, Sugar, Sugar, Red Dye (you get the bottle back) |
  | Gummy Worms | Slime Ball, Sugar, Sugar, Slime Ball |
  | Donut | Wheat, Egg, Sugar, Pink Dye |
  | Lollipop | Stick, Sugar, Sugar, Red Dye |
  | Popcorn | Bowl, Wheat, Wheat, Wheat (you get the bowl back) |
  | Pizza Slice | Bread, Cooked Porkchop, Red Mushroom, Bread |
  | Hot Dog | Bread, Cooked Porkchop, Cooked Porkchop, Bread |
  | Ice Cream | Snowball, Sugar, Sugar, Wheat |
  | Cotton Candy | Stick, Sugar, Sugar, Sugar |
  | Cheezy Puffs | Paper, Wheat, Yellow Dye, Yellow Dye |
- **Can of Beans** — Iron Ingot, Cocoa Beans, Cocoa Beans, Iron Ingot (one row, in the 4x4 table). Fills 3 hunger bars and gives you **Farting** for 1 minute: you get boosted about 1 block up every half a second, with a toot each time, and no fall damage while it lasts. (Beans aren't junk food, so they don't attract hOesaC.)
- **Altar** — a pedestal that holds one junk food. Crafted in the 4x4 table (makes 4). Rows, top to bottom:
  1. (empty)
  2. Diamond, Crafting Table, Crafting Table, Diamond
  3. (empty), Mangrove Log, Mangrove Log, (empty)
  4. Obsidian, Obsidian, Obsidian, Obsidian

  Right-click an altar holding a junk food to put it on. Right-click with an empty hand to take it back. A full altar glows.
- **Summoning the hOesaC Final Boss** — build this on flat ground (**C** = cobblestone, **A** = an altar holding a junk food, **.** = open ground):

  ```
  C A A A C
  A . . . A
  A . T . A      T = TNT (any of the 9 middle squares)
  A . . . A
  C A A A C
  ```

  That's 12 altars and 4 cobblestone corners, with a 3x3 patch of ground in the middle. Each of the 12 altars must hold a **different** junk food (you have 15 to choose from). Then blow up a TNT in the middle. The altars' junk food gets used up (the altars themselves survive), lightning strikes, and the boss arrives and goes after everyone nearby. If you get the ring wrong, nothing happens.
- **Wooden Bucket** — a stick on top, then 3 oak planks in a V under it. Holds water or lava, but it wears out fast, and lava burns it a little every second.
- **hOesaC Final Boss** — a giant round boss (7 blocks wide, tall, and deep) with **100 hearts** and a purple health bar at the top of your screen. He rolls at you (2 hearts through armor) and jumps up to land on you (suffocation, 7 hearts). Tacks stop his roll: he bounces off them and loses 2 hearts each time. Summon him with the altar ritual above (or use his spawn egg in the Car Mod tab).
- **hOesaC** — an orange mob with 20 hearts who walks a little faster than a zombie and doesn't burn in the sun. He only shows up (and only attacks) when you're carrying junk food. If he beats you, he keeps your junk food, and everything else drops like normal. Beat him to get it back.

## How to test this on your computer

**1. Install Minecraft's mod loader (NeoForge)**
- Go to https://neoforged.net/
- Download the installer for version **1.21.11**
- Run it, pick "Install client", click OK

**2. Put the mod file in the right folder**
- Press `Windows key + R`
- Type `%appdata%\.minecraft\mods` and hit Enter
- If a "mods" folder doesn't exist, make one
- **Delete any older `carmod` file** in there
- Drop `carmod-0.5.0.jar` (link above) into that folder

**3. Launch it**
- Open the Minecraft Launcher
- In the dropdown at the bottom left, pick the "neoforge" version
- Click Play

**4. What to check**
- Make a new world (Creative mode is easiest)
- Open the creative inventory and find the "Car Mod" tab — everything is there
- Place the 4x4 and 5x5 tables and right-click them → bigger crafting grids
- Try crafting a car in the 4x4 table (the recipes are in `src/main/resources/data/carmod/recipe`)
- Place standing-up slabs; place a second one of the same kind into the empty half to make a full block
- Place a tack and walk into it (in Survival) → ouch
- Scoop water or lava with the wooden bucket and watch it wear out
- Drive a car on land (fast) and on water (slow)
- In Survival, carry some junk food and wait — hOesaC should show up within a minute or two (not in Peaceful)
- Or use the hOesaC spawn egg to meet him right away
- For the Final Boss: go to a big, flat, open area, build the altar ring (see above), fill it with 12 different junk foods, and set off TNT in the middle. Or use the hOesaC Final Boss Spawn Egg. Try tacks around yourself!
- Eat a Can of Beans and enjoy the ride
- Eat junk food, then wait 2 minutes for the Hunger

**Heads up:** cars are invisible when placed (no car art yet), but you can still ride them.

## Coming next

The AI Crafting Table (suits, weapons, and upgrades), the Electronic Bed, Redstone Remote, and Door Inspector (recipes are approved), and car textures.

## Known issues

- Breaking a car always gives back a Sedan, no matter which car it was.
- Placeholder art: the big tables reuse crafting-table textures, the table screens are plain gray, and hOesaC's spawn egg reuses the zombie egg picture.

## Building

Requires JDK 21.

```bash
./gradlew build
```

The first build downloads and decompiles Minecraft/NeoForge (~10-15
minutes); later builds are fast. If you're behind a TLS-intercepting
antivirus/proxy (e.g. Avast), Gradle's HTTPS downloads will fail with
"Plugin ... was not found" or silent timeouts even though a browser works
fine — import that tool's root CA into your JDK's `cacerts` keystore.

Generated files:

- `java tools/GenTextures.java` redraws the mod's own textures.
- `bash tools/gen_assets.sh` regenerates the standing-slab, tack, bucket,
  table and hOesaC JSON files and `en_us.json` (needs the unzipped vanilla
  client jar; see the top of the script).

Testing: `./gradlew runServer` starts a local test server;
`./gradlew runClientJoinLocal` opens the game and joins it.

## License

MIT (placeholder — confirm before publishing).
