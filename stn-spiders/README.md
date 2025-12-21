# STN-Spiders

> 6 spider variants with unique mechanics for Survive The Night

## Overview

STN-Spiders adds 6 distinct spider variants to challenge players with mobility, crowd control, and boss-tier threats. From the camouflaging Stalker to the minion-spawning Broodmother, each spider brings unique mechanics to survival night encounters.

These spiders integrate with the horde system, appearing at various gamestage thresholds and creating diverse combat scenarios that require different tactical approaches.

## Spider Variants

### Stalker Spider
**Role**: Ambush Predator

| Stat | Value |
|------|-------|
| Health | 16 |
| Damage | 3 (+4 ambush bonus) |
| Speed | 0.32 |
| Scale | 0.9x (smallest) |

- Semi-invisible in darkness when stationary
- Visibility drops to 20% in light level ≤7
- First hit deals +4.0 extra ambush damage
- Rewards stealth gameplay

### Webspinner Spider
**Role**: Crowd Control

| Stat | Value |
|------|-------|
| Health | 18 |
| Damage | 2 |
| Speed | 0.28 |
| Scale | 1.1x |

- Places cobwebs on hit (33% chance)
- Applies Slowness II for 3 seconds
- Connects to adjacent cobwebs
- Excellent at trapping players

### Leaper Spider
**Role**: Burst Mobility

| Stat | Value |
|------|-------|
| Health | 16 |
| Damage | 3 |
| Speed | 0.35 |
| Scale | 1.0x |

- Extended leap ability (10-block range)
- 10-second cooldown
- Prioritizes elevated targets
- Leaps when target >3 blocks away or on high ground

### Broodmother Spider
**Role**: Summoner/Mini-Boss

| Stat | Value |
|------|-------|
| Health | 60 |
| Damage | 4 |
| Speed | 0.2 (slowest) |
| Scale | 1.6x (largest) |
| Armor | 4.0 |
| Knockback Resistance | 0.5 |

- Spawns up to 6 cave spider minions
- 10-second spawn cooldown
- Death burst: 3 additional spiderlings
- Mini-boss tier threat

### Venom Spider
**Role**: Damage Over Time

| Stat | Value |
|------|-------|
| Health | 18 |
| Damage | 2 |
| Speed | 0.3 |
| Scale | 1.15x |

- Stacking poison effect
- Each hit adds +40 ticks to duration
- Base: 100 ticks (5 seconds) Poison II
- Punishes extended combat

### Burden Spider
**Role**: Debuffer/Tank

| Stat | Value |
|------|-------|
| Health | 40 |
| Damage | 4 |
| Speed | 0.22 (slow) |
| Scale | 1.4x |
| Armor | 8.0 |
| Knockback Resistance | 0.5 |

- Applies Weakness I + Mining Fatigue II
- Debuffs last 100 ticks (5 seconds)
- Heavy armor makes it tanky
- Disrupts player combat effectiveness

## Installation

### Requirements
- Minecraft 1.21.8
- Fabric Loader 0.15.0+
- Java 21+
- stn-core
- stn-mobai
- stn-survival

### Dependencies
```gradle
dependencies {
    implementation project(':stn-core')
    implementation project(':stn-mobai')
    implementation project(':stn-survival')
}
```

## Technical Details

### File Structure
```
stn-spiders/
├── src/main/java/com/stn/spiders/
│   ├── STNSpiders.java
│   ├── config/
│   │   └── STNSpidersConfig.java
│   ├── entity/
│   │   ├── StalkerSpiderEntity.java
│   │   ├── WebspinnerSpiderEntity.java
│   │   ├── LeaperSpiderEntity.java
│   │   ├── BroodmotherSpiderEntity.java
│   │   ├── VenomSpiderEntity.java
│   │   └── BurdenSpiderEntity.java
│   └── registry/
│       └── STNSpiderEntities.java
├── src/client/java/com/stn/spiders/client/
│   ├── STNSpidersClient.java
│   └── render/
│       └── ScaledSpiderRenderer.java
└── src/main/resources/
    └── fabric.mod.json
```

### Horde Registration

| Spider | Weight | Gamestage Range |
|--------|--------|-----------------|
| Stalker | 10-15 | Early |
| Leaper | 10-20 | Early |
| Webspinner | 8-25 | Early |
| Venom | 8-30 | Mid |
| Burden | 5-50 | Late |
| Broodmother | 3-75 | Mini-boss |

## Dependencies

### Required
- stn-core
- stn-mobai
- stn-survival

## License

MIT License
