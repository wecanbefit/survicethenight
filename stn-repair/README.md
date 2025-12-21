# STN-Repair

> Hammer-based repair tools for fortifications in Survive The Night

## Overview

STN-Repair provides a tiered hammer system for repairing fortification blocks from stn-fortifications. As defensive structures take damage from zombie attacks, players use hammers to restore their durability and maintain their defenses.

Five hammer tiers offer increasing repair efficiency, from basic wooden hammers for early game to netherite hammers for maximum restoration. Special blocks like spikes require specific repair materials in addition to hammer durability.

## Features

### Hammer Tiers

| Hammer | Durability | Repair Amount | Material |
|--------|------------|---------------|----------|
| Wooden | 59 | 10 | Wood planks |
| Stone | 131 | 15 | Cobblestone |
| Iron | 250 | 25 | Iron ingot |
| Diamond | 1561 | 40 | Diamond |
| Netherite | 2031 | 60 | Netherite (smithing) |

### Repair Systems

#### Tracked Block Repair
Standard fortification blocks tracked by the durability system:
- Right-click with hammer to repair
- Uses hammer durability only (1 per repair)
- Repair amount based on hammer tier

#### Special Block Repair
Spike blocks and wire defenses require materials:

| Block | Repair Item | Quantity | Repair Amount |
|-------|-------------|----------|---------------|
| Wooden Spikes | Stick | 1 | 3 |
| Bamboo Spikes | Stick | 1 | 3 |
| Iron Spikes | Iron Nugget | 1 | 3 |
| Reinforced Spikes | Iron Nugget | 2 | 3 |
| Barbed Wire | Iron Nugget | 1 | 3 |
| Electric Fence | Iron Nugget | 1 | 3 |

### HUD Overlay
When holding a hammer and looking at a fortification:
- Shows current and maximum durability
- Color-coded health indicator:
  - Green: >66% durability
  - Yellow: 33-66% durability
  - Red: <33% durability
- Shows required repair materials for special blocks

### Repair Effects
- Anvil sound effect
- Critical hit particles around block
- Visual feedback for successful repairs

## Crafting Recipes

### Wooden Hammer
```
[P]
[S]
[S]

P = Any Plank
S = Stick
```

### Stone Hammer
```
[C]
[S]
[S]

C = Cobblestone
S = Stick
```

### Iron Hammer
```
[I]
[S]
[S]

I = Iron Ingot
S = Stick
```

### Diamond Hammer
```
[D]
[S]
[S]

D = Diamond
S = Stick
```

### Netherite Hammer
Upgrade Diamond Hammer via Smithing Table:
- Diamond Hammer + Netherite Ingot

## Installation

### Requirements
- Minecraft 1.21.8
- Fabric Loader 0.16.0+
- Java 21+
- stn-core
- stn-fortifications

### Dependencies
```gradle
dependencies {
    implementation project(':stn-core')
    implementation project(':stn-fortifications')
}
```

## Technical Details

### File Structure
```
stn-repair/
├── src/main/java/com/stn/repair/
│   ├── STNRepair.java               # Entry point
│   ├── config/
│   │   └── STNRepairConfig.java     # HUD position config
│   ├── item/
│   │   └── HammerItem.java          # Core repair logic
│   └── registry/
│       └── STNItems.java            # Item registration
├── src/client/java/com/stn/repair/client/
│   ├── STNRepairClient.java
│   └── hud/
│       └── HammerHudOverlay.java    # Durability HUD
└── src/main/resources/
    ├── fabric.mod.json
    ├── assets/stn_repair/
    │   ├── lang/en_us.json
    │   ├── models/item/             # 5 hammer models
    │   └── textures/item/           # 5 hammer textures
    └── data/stn_repair/recipe/      # 5 crafting recipes
```

### Integration Points
- **stn-core**: Uses `IDurabilityProvider` for durability queries
- **stn-fortifications**: Accesses `BlockDurabilityManager` for repairs

## Configuration

| Setting | Default | Description |
|---------|---------|-------------|
| `hudPosition` | `br` | HUD position (tl, tm, tr, ml, mr, bl, bm, br) |

Position codes:
- `t` = top, `m` = middle, `b` = bottom
- `l` = left, `m` = middle, `r` = right

## Dependencies

### Required
- stn-core
- stn-fortifications

## License

MIT License
