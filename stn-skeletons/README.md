# STN-Skeletons

> 6 skeleton variants with specialized combat roles for Survive The Night

## Overview

STN-Skeletons adds 6 distinct skeleton variants organized into two categories: bow-wielding ranged attackers and melee-focused black skeletons. Each variant has a specific combat role, from the precision-striking Marksman to the execute-focused Reaper.

These skeletons integrate with the survival night horde system, spawning at appropriate gamestage thresholds to challenge players who have mastered early-game zombies.

## Skeleton Variants

### Bow Skeletons (Ranged)

#### Marksman Skeleton
**Role**: Precision Ranged

| Stat | Value |
|------|-------|
| Health | 20 |
| Damage | High |
| Fire Rate | Slow (3s cooldown) |
| Arrow Spread | 0 (perfect accuracy) |

- Long-range precision attacks
- Punishes peeking and stationary players
- Uses vanilla skeleton model at 1.0x scale

#### Suppressor Skeleton
**Role**: Area Control

| Stat | Value |
|------|-------|
| Health | 20 |
| Damage | Low |
| Fire Rate | Fast (15 ticks) |
| Effect | Slowness II |

- Rapid fire suppression
- Arrows apply Slowness II
- Forces players to seek cover

#### Flame Archer Skeleton
**Role**: Damage Over Time

| Stat | Value |
|------|-------|
| Health | 20 |
| Damage | Medium |
| Fire Rate | Medium |
| Effect | 5-second burn |

- Flaming arrows
- Fire immune
- Effective against unarmored targets

### Black Skeletons (Melee)

#### Vanguard Skeleton
**Role**: Tanky Frontline

| Stat | Value |
|------|-------|
| Health | 40 |
| Damage | 8 |
| Speed | 0.22 |
| Knockback Resistance | 0.6 |

- Heavy iron sword
- 1.5-second attack cooldown
- Shield-breaking mechanics
- 1.25x scale (larger)
- Immune to sunlight

#### Duelist Skeleton
**Role**: Aggressive Melee

| Stat | Value |
|------|-------|
| Health | 25 |
| Damage | 5 |
| Speed | 0.32 (fast) |
| Dash Range | 6 blocks |

- Stone sword
- Dash ability (5-second cooldown)
- Gap-closing attacks
- 1.0x scale

#### Reaper Skeleton
**Role**: Executioner

| Stat | Value |
|------|-------|
| Health | 30 |
| Damage | 6 (12 execute) |
| Speed | 0.25 |
| Execute Threshold | <30% HP |

- Netherite hoe weapon
- 2x damage to low-health targets
- Gains Speed II on kills
- Prioritizes weakened enemies

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
stn-skeletons/
├── src/main/java/com/stn/skeletons/
│   ├── STNSkeletons.java
│   ├── config/
│   │   └── STNSkeletonsConfig.java
│   ├── entity/
│   │   ├── MarksmanSkeletonEntity.java
│   │   ├── SuppressorSkeletonEntity.java
│   │   ├── FlameArcherSkeletonEntity.java
│   │   ├── VanguardSkeletonEntity.java
│   │   ├── DuelistSkeletonEntity.java
│   │   └── ReaperSkeletonEntity.java
│   └── registry/
│       └── STNSkeletonEntities.java
├── src/client/java/com/stn/skeletons/client/
│   ├── STNSkeletonsClient.java
│   └── render/
│       ├── ScaledSkeletonRenderer.java
│       └── ScaledWitherSkeletonRenderer.java
└── src/main/resources/
    └── fabric.mod.json
```

### Horde Registration

| Skeleton | Weight | Gamestage Range |
|----------|--------|-----------------|
| Marksman | - | 10-15 |
| Suppressor | - | 10-15 |
| Flame Archer | - | 15-25 |
| Vanguard | - | 20-40 |
| Duelist | - | 25-50 |
| Reaper | - | 40-60 |

### Integration
- Melee skeletons implement `BlockBreakAnimatable` for stn-mobai integration
- All skeletons register with `HordeMobRegistry` from stn-survival

## Dependencies

### Required
- stn-core
- stn-mobai
- stn-survival

## License

MIT License
