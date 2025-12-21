# STN-Survival

> Gamestage system, survival nights, and horde spawning for Survive The Night

## Overview

STN-Survival is the heart of the Survive The Night experience, managing the core survival mechanics that make the modpack challenging. It tracks player progression through a gamestage system, triggers survival night events with massive zombie hordes, and scales difficulty based on your achievements.

Every 6 days, survive the night or perish. As your gamestage rises from zombie kills and survival time, new enemy types unlock and horde sizes grow. The jockey system adds variety with 14 different rider/mount combinations that spawn at higher difficulties.

## Features

### Gamestage System

Dynamic difficulty scaling based on player progression.

**Calculation Formula:**
```
Time-based (protected floor):
  + 0.15 per night survived
  + 2.0 per survival night event completed

Kill Bonus (can be reduced by deaths):
  + 0.005 per zombie kill (200 kills = 1 gamestage)

Death Penalty (only affects kill bonus):
  - 2.0 per death

Final = floor(time-based) + max(0, kill-bonus - death-penalty)
```

**Gamestage Thresholds:**
| Gamestage | Unlocks |
|-----------|---------|
| 0-9 | Basic zombies only |
| 10+ | Feral zombies |
| 25+ | Sprinter zombies common |
| 50+ | Demolisher zombies |
| 75+ | Screamer zombies |
| 100+ | Giant boss possible |

**Horde Size Multipliers:**
| Gamestage | Multiplier |
|-----------|------------|
| ≤10 | 1.0x |
| ≤25 | 1.25x |
| ≤50 | 1.5x |
| ≤75 | 1.75x |
| ≤100 | 2.0x |
| >100 | 2.5x |

### Survival Night Events

Scheduled horde attacks every 6 in-game days.

**Timing:**
- **Interval**: Every 6 days (configurable)
- **Start**: 10 PM (game time 22000)
- **End**: 6 AM (game time 6000)

**Horde Calculation:**
```
Base Size = 20 mobs
Multiplier = (1.0 + days × 0.1) × (1.0 + (playerCount - 1) × 0.5)
Final = min(multiplier × baseSize, 200)
```

**Wave Spawning:**
- Waves every 30 seconds (600 ticks)
- 10-20 mobs per wave
- Spawn radius: 24-48 blocks from players

### Spawn Categories

| Category | Behavior |
|----------|----------|
| GROUND | Default, spawns on solid blocks |
| AERIAL | Spawns 20-50 blocks above player |
| AQUATIC | Searches for water blocks |
| PHASING | Ignores block collision |

### Jockey System

14 rider/mount combinations available at higher gamestages.

**Jockey Types:**
| Category | Combinations |
|----------|--------------|
| Spider Jockeys | Zombie, Skeleton, Stray on spiders |
| Chicken Jockeys | Baby zombie, husk, drowned on chickens |
| Horsemen | Skeleton, Wither Skeleton, Stray on skeleton horses |
| Zombie Horsemen | Husk, Drowned on zombie horses |
| Ravager Riders | Pillager, Vindicator, Evoker on ravagers |
| Nether Riders | Piglin Brute, Zombie on striders/hoglins |

**Spawn Conditions:**
- Gamestage ≥15 to attempt spawning
- Base 5% + (gamestage/20)% chance, capped at 20%
- Each jockey type has minimum gamestage requirement

## Installation

### Requirements
- Minecraft 1.21.8
- Fabric Loader 0.16.0+
- Java 21+
- stn-core

### Dependencies
```gradle
dependencies {
    implementation project(':stn-core')
}
```

## Usage

### Commands

All commands require operator permissions:

```
/survivalnight start        - Force start survival night
/survivalnight stop         - End current survival night
/survivalnight status       - Show event status
/survivalnight skip         - Skip to next survival night

/gamestage                  - View your gamestage
/gamestage set <value>      - Set gamestage (admin)
/gamestage add <value>      - Add to gamestage (admin)
```

### Registering Custom Mobs

```java
// In your mod's initialization
HordeMobRegistry.registerCustomMob(
    new HordeMob(
        MyModEntities.CUSTOM_ZOMBIE,
        10,     // spawn weight
        25,     // minimum gamestage
        MobCategory.GROUND
    )
);

// Register a jockey
HordeMobRegistry.registerCustomJockey(
    new JockeyType(
        "my_jockey",
        EntityType.SPIDER,      // mount
        EntityType.SKELETON,    // rider
        5,                      // weight
        30,                     // min gamestage
        false                   // is baby
    )
);
```

## Technical Details

### File Structure
```
stn-survival/
├── src/main/java/com/stn/survival/
│   ├── STNSurvival.java                 # Main entry point
│   ├── config/
│   │   └── STNSurvivalConfig.java       # Configuration
│   ├── progression/
│   │   ├── GamestageManager.java        # Core gamestage logic
│   │   ├── GamestageState.java          # Persistent world data
│   │   └── PlayerGamestage.java         # Per-player tracking
│   ├── event/
│   │   └── SurvivalNightManager.java    # Horde orchestration
│   ├── spawn/
│   │   ├── HordeMobRegistry.java        # Mob registry
│   │   ├── JockeySpawner.java           # Jockey spawning
│   │   └── MobCategory.java             # Spawn categories
│   ├── network/
│   │   ├── GamestageHudPayload.java
│   │   └── SurvivalNightSyncPayload.java
│   └── command/
│       └── SurvivalNightCommand.java
├── src/client/java/com/stn/survival/client/
│   ├── STNSurvivalClient.java
│   └── hud/
│       └── GamestageHudOverlay.java
└── src/main/resources/
    └── fabric.mod.json
```

### Persistence
- Gamestage data saved using Minecraft's PersistentState
- Survives server restarts
- Per-player progression tracking

### Events Fired
```java
STNEvents.SURVIVAL_NIGHT_START  // When horde spawning begins
STNEvents.SURVIVAL_NIGHT_END    // When night ends successfully
STNEvents.GAMESTAGE_CHANGED     // When player gamestage changes
```

## API Integration

### Provider Registration
```java
// During initialization
STNCore.registerSurvivalNightProvider(survivalNightManager);
STNCore.registerGamestageProvider(gamestageManager);
```

### Querying State
```java
// Check survival night status
boolean active = STNCore.isSurvivalNightActive();
int daysUntil = STNCore.getDaysUntilSurvivalNight();

// Get gamestage info
int worldStage = STNCore.getWorldGamestage();
int playerStage = STNCore.getPlayerGamestage(uuid);
float hordeMultiplier = STNCore.getHordeSizeMultiplier();
```

## Configuration

| Setting | Default | Description |
|---------|---------|-------------|
| `SURVIVAL_NIGHT_INTERVAL` | 6 | Days between survival nights |
| `BASE_HORDE_SIZE` | 20 | Base mobs per horde |
| `MAX_HORDE_SIZE` | 200 | Maximum horde cap |
| `SPAWN_RADIUS_MIN` | 24 | Minimum spawn distance |
| `SPAWN_RADIUS_MAX` | 48 | Maximum spawn distance |
| `SPAWN_WAVE_INTERVAL` | 600 | Ticks between waves |
| `GAMESTAGE_PER_NIGHT` | 0.15 | Gamestage per night survived |
| `GAMESTAGE_PER_KILL` | 0.005 | Gamestage per zombie kill |
| `DEATH_PENALTY` | 2.0 | Gamestage lost per death |

## Dependencies

### Required
- stn-core

### Optional Integration
- stn-zombies (custom zombies in hordes)
- stn-skeletons (skeleton variants)
- stn-spiders (spider variants)

## License

MIT License
