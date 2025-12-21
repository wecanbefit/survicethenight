# STN-Wastelands

> Village zombification and enhanced loot for Survive The Night

## Overview

STN-Wastelands transforms Minecraft villages into dangerous zombie-infested wastelands. All villagers become zombies or zombie villagers, iron golems are replaced with hostile mobs, and village loot scales with gamestage progression.

This module creates a post-apocalyptic atmosphere where villages are threats rather than safe havens, rewarding players who venture into these dangerous areas with enhanced loot.

## Features

### Village Zombification

When entities spawn in villages:
- **Villagers** → Zombie Villagers (retaining profession appearance)
- **Iron Golems** → Gamestage-appropriate zombies

Converted zombies:
- Immune to sunlight (equipped with invisible helmets)
- 30% chance to spawn 1-2 extra zombies nearby

### Zombie Tier System

Zombie types based on current gamestage:

| Gamestage | Tier | Zombie Types |
|-----------|------|--------------|
| 0-19 | Basic | Sprinters, regular zombies |
| 20-39 | Low | Sprinters, Spitters, Howlers |
| 40-59 | Mid | Bruisers, Shielded, Spitters, Howlers |
| 60-79 | High | Plague, Leech, Lumberjack, Bruiser, Shielded |
| 80+ | Elite | Electric, Plague, Leech, Lumberjack |

### Enhanced Loot System

Village chests contain gamestage-scaled loot:

**Loot Categories:**
- **Materials**: Iron, gold, copper, diamonds, netherite
- **Gear**: Weapons, armor, tools (chainmail → netherite)
- **Food**: Bread, cooked meats, golden foods
- **Books**: Enchanted books

**Tier System (0-9):**
- Tier 0: Gamestage 0-9
- Tier 1: Gamestage 10-19
- ...
- Tier 9: Gamestage 90-99

Higher tiers provide:
- More items per stack
- Better item quality
- Higher enchantment chances
- Multi-enchantment possibility

### Enchantment Scaling

| Tier | Enchant Chance | Multi-Enchant Chance |
|------|----------------|---------------------|
| 0 | 10% | 0% |
| 3 | 30% | 15% |
| 6 | 60% | 30% |
| 9 | 90% | 50% |

### Affected Loot Tables

16 village chest types enhanced:
- Armorer, Butcher, Cartographer
- Cleric, Desert House, Fisher
- Fletcher, Leatherworker, Mason
- Plains House, Savanna House
- Shepherd, Snowy House, Taiga House
- Tannery, Temple, Toolsmith, Weaponsmith

## Installation

### Requirements
- Minecraft 1.21.8
- Fabric Loader 0.16.0+
- Java 21+
- stn-core
- stn-mobai
- stn-zombies
- Cloth Config
- ModMenu

### Dependencies
```gradle
dependencies {
    implementation project(':stn-core')
    implementation project(':stn-mobai')
    implementation project(':stn-zombies')
}
```

## Configuration

Access via ModMenu → Mods → STN-Wastelands → Config

| Setting | Default | Description |
|---------|---------|-------------|
| `enableVillageZombification` | true | Enable villager conversion |
| `enableEnhancedLoot` | true | Enable loot scaling |
| `minimumGamestageForBonusLoot` | 10 | Min gamestage for tier 1+ loot |
| `zombieVillagerChance` | 0.6 | Chance to become zombie villager vs regular zombie |
| `enableEnhancedEnchantments` | true | Enable tier-based enchantments |
| `extraZombieSpawnChance` | 0.3 | Chance to spawn extra zombies |
| `minExtraZombies` | 1 | Minimum extra spawns |
| `maxExtraZombies` | 2 | Maximum extra spawns |

## Technical Details

### File Structure
```
stn-wastelands/
├── src/main/java/com/stn/wastelands/
│   ├── STNWastelands.java
│   ├── config/
│   │   ├── WastelandConfig.java
│   │   └── WastelandConfigManager.java
│   ├── village/
│   │   └── VillageZombifier.java
│   └── loot/
│       ├── WastelandLootModifier.java
│       ├── LootConfig.java
│       ├── LootConfigManager.java
│       ├── GamestageCountLootFunction.java
│       └── GamestageEnchantLootFunction.java
├── src/client/java/com/stn/wastelands/
│   ├── client/STNWastelandsClient.java
│   └── config/
│       ├── WastelandConfigScreen.java
│       └── ModMenuIntegration.java
└── src/main/resources/
    ├── fabric.mod.json
    └── stn_wastelands.mixins.json
```

### Performance Optimizations
- `IdentityHashMap` for O(1) entity tracking
- Queued conversions for batch processing
- Lazy initialization of zombie type lists

### Config Persistence
- Main config: `config/stn-wastelands.json`
- Loot config: `config/stn-wastelands/loot.json`

## Dependencies

### Required
- stn-core (gamestage provider)
- stn-mobai (zombie AI)
- stn-zombies (custom zombie types)
- Cloth Config (configuration UI)
- ModMenu (settings access)

## License

MIT License
