# Car Mod

NeoForge mod for Minecraft Java Edition **1.21.11**. Targeting release on Modrinth.

Mod id: `carmod` / package `com.kiancars.carmod`.

**[Download the latest build (carmod-0.3.0.jar)](https://github.com/sitens/minecraft-car-mod/releases/tag/v0.3.0)**

## Changelog

Newest first. Each version says what's new, what got fixed, and what changed.

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
- **10 junk foods** — each fills 2½ hunger bars. Made in the 4x4 or 5x5 table, one row of 4 (left → right):

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
- **Wooden Bucket** — a stick on top, then 3 oak planks in a V under it. Holds water or lava, but it wears out fast, and lava burns it a little every second.
- **hOesaC** — an orange mob with 7 hearts who doesn't burn in the sun. He only shows up (and only attacks) when you're carrying junk food. If he beats you, he keeps your junk food, and everything else drops like normal. Beat him to get it back.

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
- Drop `carmod-0.3.0.jar` (link above) into that folder

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

**Heads up:** cars are invisible when placed (no car art yet), but you can still ride them.

## Coming next

Car textures, the hOesaC Final Boss, the Electronic Bed, Redstone Remote, and Door Inspector.

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
