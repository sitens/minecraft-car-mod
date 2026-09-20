# Car Mod (working title)

NeoForge mod for Minecraft Java Edition **1.21.11**. Adds 10 craftable,
rideable cars and a "Reinforced Crafting Table" (4x4 grid) required to
build them. Targeting release on Modrinth.

Working mod id: `carmod` — the project name is undecided; rename via a
project-wide find/replace of `carmod` / `com.kiancars.carmod` / "Car Mod"
once a real name is picked (also update `settings.gradle`'s `rootProject.name`
and `gradle.properties`).

## Status: early scaffold, not yet building/tested

This was scaffolded without a live Gradle sync against real NeoForge
1.21.11 artifacts, so treat the following as "needs verification on first
build" rather than confirmed-working:

- `neo_version` / `neoform_version` in `gradle.properties` are placeholders —
  fill in the real published versions from
  https://projects.neoforged.net/neoforged/neoforge before syncing.
- `CraftingTable4x4Menu` — `stillValid`, `quickMoveStack` (shift-click), and
  the `slotChangedCraftingGrid` helper call are stubbed/unverified against
  1.21.11's actual `AbstractContainerMenu`/crafting-grid APIs.
- `CarEntity` — the abstract drop-item method inherited from `AbstractBoat`
  is not wired up (signature uncertain across versions); see the NOTE in
  that file.
- No real textures/models/sounds yet — blocks/items reuse vanilla crafting
  table and boat textures as placeholders; the car entity renderer draws
  vanilla's default fallback (no custom model).

## Design

- **Cars**: one shared `CarEntity` class (boat-style movement, extends
  `AbstractBoat`) with a `CarType` enum for the 10 variants. Each variant is
  a separate placement item (`carmod:car_<id>`), mirroring vanilla boats.
- **Reinforced Crafting Table**: crafted from 9 planks (3x3, like a vanilla
  crafting table). Opens a custom 16-slot (4x4) grid menu/screen.
- **Recipes**: plain vanilla shaped recipes — no custom recipe type needed.
  Every car recipe's pattern is 4 columns wide, so it physically cannot fit
  in a vanilla 3x3 table or the 2x2 inventory grid; the reinforced table is
  the only way to craft any of them. Difficulty/material cost climbs from
  iron (sedan) to netherite+diamond (golden luxury).

## Building

Requires JDK 21. Once real NeoForge version numbers are filled in:

```bash
./gradlew build
```

## License

MIT (placeholder — confirm before publishing).
