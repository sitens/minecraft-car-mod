# Car Mod

NeoForge mod for Minecraft Java Edition **1.21.11**. Adds 10 craftable,
rideable cars and a "Reinforced Crafting Table" (4x4 grid) required to
build them. Targeting release on Modrinth.

Mod id: `carmod` / package `com.kiancars.carmod` — confirmed final.

## Status: builds successfully, not yet run/playtested

`./gradlew build` succeeds and produces `build/libs/carmod-0.1.0.jar`
(confirmed against real NeoForge 21.11.45 / MC 1.21.11 artifacts). Known
gaps before this is genuinely playable:

- Not yet launched in a dev client (`./gradlew runClient`) or playtested —
  compiling clean doesn't guarantee correct runtime behavior.
- `CarEntity`'s drop/pick-block item is hardcoded to the Sedan regardless of
  actual variant — `AbstractBoat`'s drop-item supplier is fixed at
  construction time and can't legally read the entity's own synced data
  before `super()` returns. Placement (via `CarItem`) is already
  variant-correct; only break/pick-block is affected. See the note in
  `CarEntity.java` and the task in `tasks.md`.
- No real textures/models/sounds yet — blocks/items reuse vanilla crafting
  table and boat textures as placeholders; the car entity renderer uses the
  engine's default fallback (no custom model) since 1.21.11's entity
  rendering moved to a render-state/`submit()` architecture that this
  scaffold only stubs (`CarRenderer.createRenderState()`).
- The 4x4 crafting table screen draws a flat placeholder panel, not a real
  GUI texture.

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

Requires JDK 21.

```bash
./gradlew build
```

The first build downloads and decompiles Minecraft/NeoForge (~10-15
minutes); later builds are fast. If you're behind a TLS-intercepting
antivirus/proxy (e.g. Avast), Gradle's HTTPS downloads will fail with
"Plugin ... was not found" or silent timeouts even though a browser works
fine — import that tool's root CA into your JDK's `cacerts` keystore.

## License

MIT (placeholder — confirm before publishing).
