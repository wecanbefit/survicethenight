# Survive The Night

> A modular zombie survival modpack for Minecraft 1.21 built on Fabric

## Overview

Survive The Night is a comprehensive zombie survival experience for Minecraft, designed as a modular collection of interconnected mods. Each module focuses on a specific aspect of gameplay—from intelligent mob AI to defensive fortifications—allowing for flexible configuration and easy maintenance.

The modpack transforms Minecraft into an intense survival horror experience where zombies are smarter, nights are deadlier, and every dawn is an achievement. With a sophisticated gamestage system, the difficulty scales with your progress, ensuring veterans face new challenges while newcomers can learn the ropes.

## Modules

| Module | Description | Status |
|--------|-------------|--------|
| [stn-core](stn-core/README.md) | Foundation API with shared interfaces and events | Stable |
| [stn-survival](stn-survival/README.md) | Gamestage system, survival nights, horde spawning | Stable |
| [stn-mobai](stn-mobai/README.md) | Advanced mob AI with sensory detection | Stable |
| [stn-fortifications](stn-fortifications/README.md) | Defensive blocks and durability system | Stable |
| [stn-zombies](stn-zombies/README.md) | 10 unique zombie variants with special abilities | Stable |
| [stn-skeletons](stn-skeletons/README.md) | 6 skeleton variants (ranged and melee) | Stable |
| [stn-spiders](stn-spiders/README.md) | 6 spider variants with unique mechanics | Stable |
| [stn-hud](stn-hud/README.md) | RPG-style HUD replacement system | Stable |
| [stn-repair](stn-repair/README.md) | Hammer-based repair tools for fortifications | Stable |
| [stn-wastelands](stn-wastelands/README.md) | Village zombification and enhanced loot | Stable |
| [stn-traders](stn-traders/README.md) | Trading mechanics and merchant NPCs | Planned |

## Architecture

```
survivethenight/
├── stn-core           # Foundation (no dependencies)
├── stn-survival       # Depends on: core
├── stn-fortifications # Depends on: core
├── stn-mobai          # Depends on: core, fortifications
├── stn-zombies        # Depends on: core, mobai, survival
├── stn-skeletons      # Depends on: core, mobai, survival
├── stn-spiders        # Depends on: core, mobai, survival
├── stn-hud            # Depends on: core
├── stn-repair         # Depends on: core, fortifications
├── stn-wastelands     # Depends on: core, mobai, zombies
└── stn-traders        # Depends on: core (planned)
```

## Features

### Intelligent Mob AI (stn-mobai)
- **Sound Detection**: Mobs hear block breaking, combat, and movement
- **Smell Detection**: Track players through walls during survival nights
- **Light/Heat Detection**: Attracted to torches and fire sources
- **Village Detection**: Navigate toward villages using POI system
- **Block Breaking**: Pathfind through obstacles to reach targets

### Survival Night Events (stn-survival)
- **Scheduled Hordes**: Every 6 days, massive zombie waves attack
- **Gamestage Progression**: Difficulty scales with survival time and kills
- **Jockey System**: 14 different rider/mount combinations
- **Wave Spawning**: Strategic spawn timing and positioning

### Custom Mobs
- **10 Zombie Variants**: Bruiser, Sprinter, Spitter, Howler, Plague, Leech, Electric, Shielded, Lumberjack, Zombabie
- **6 Skeleton Variants**: Marksman, Suppressor, Flame Archer, Vanguard, Duelist, Reaper
- **6 Spider Variants**: Stalker, Webspinner, Leaper, Broodmother, Venom, Burden

### Defensive Systems (stn-fortifications)
- **Spike Blocks**: 4 tiers of damage traps
- **Reinforced Walls**: 4 tiers of zombie-resistant barriers
- **Electric Fence**: Powered defense with stun effects
- **Motion Sensors**: Redstone-based mob detection
- **Durability Tracking**: All blocks degrade and require repair

### HUD System (stn-hud)
- **6 Visual Styles**: Vanilla, Simple, Default, Extended, Modern, Full Texture
- **16 Element Types**: Customizable positioning and colors
- **In-Game Configuration**: Full Cloth Config integration

## Installation

### Requirements
- Minecraft 1.21.8
- Fabric Loader 0.18.3+
- Java 21+

### Dependencies
| Dependency | Version | Purpose |
|------------|---------|---------|
| Fabric API | 0.136.1+ | Core modding API |
| GeckoLib | 5.2.1 | Entity models & animations |
| Cloth Config | 19.0.147 | Configuration GUI |
| ModMenu | 15.0.0 | Settings menu integration |

### Building from Source

```bash
# Clone the repository
git clone https://github.com/wecanbefit/survivethenight.git
cd survivethenight

# Build all modules
./gradlew build

# Find JARs in each module's build/libs directory
```

### Module Selection
You can install individual modules based on your needs:

**Minimum Installation** (AI enhancements only):
- stn-core
- stn-mobai

**Recommended Installation** (full survival experience):
- All modules except stn-traders

**Full Installation**:
- All modules

## Configuration

Each module that supports configuration uses Cloth Config for in-game settings. Access via ModMenu → Mods → [Module Name] → Config.

Key configuration areas:
- Gamestage scaling factors
- Spawn rates and wave sizes
- Detection ranges for mob AI
- Block durability values
- HUD element positions and styles

## Development

### Project Structure
```
survivethenight/
├── build.gradle          # Root build configuration
├── settings.gradle       # Module definitions
├── gradle.properties     # Version management
└── stn-*/                # Individual modules
    ├── src/main/         # Server-side code
    ├── src/client/       # Client-side code
    └── build.gradle      # Module dependencies
```

### Adding a New Module
1. Create directory `stn-newmodule/`
2. Add to `settings.gradle`: `include 'stn-newmodule'`
3. Create `build.gradle` with dependencies
4. Implement `ModInitializer` and `ClientModInitializer`

### API Integration
Other mods can integrate via stn-core interfaces:
- `ISurvivalNightProvider` - Query survival night state
- `IGamestageProvider` - Access gamestage system
- `ISoundEmitter` - Register sounds for mob detection
- `IDurabilityProvider` - Hook into block durability

## License

MIT License - See individual module LICENSE files for details.

## Credits

- **Author**: wecanbefit
- **Repository**: https://github.com/wecanbefit/survivethenight
