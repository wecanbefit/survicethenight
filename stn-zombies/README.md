# STN-Zombies

> 10 unique zombie variants with special abilities for Survive The Night

## Overview

STN-Zombies adds 10 distinct zombie variants to the game, each with unique roles, abilities, and AI behaviors. From the leap-attacking Zombabie to the lightning-summoning Electric Zombie, these variants create diverse and challenging combat encounters.

Each zombie type unlocks at specific gamestage thresholds, ensuring difficulty scales with player progression. The variants are designed to work together—Howlers buff nearby zombies while Plague Zombies weaken players with stacking debuffs.

## Zombie Variants

### Tier 1 - Early Game (Gamestage 5-20)

#### Zombabie
**Role**: Swarm/Harassment

| Stat | Value |
|------|-------|
| Health | 8 |
| Damage | 2 |
| Speed | 0.3 |

- Small size (fits through 1-block gaps)
- Leap attack mechanic (3-6 block range)
- Always renders as baby zombie

#### Sprinter Zombie
**Role**: Chaser

| Stat | Value |
|------|-------|
| Health | 15 |
| Damage | 4 |
| Speed | 0.26 (0.45 sprinting) |

- Burst sprint when within 0-8 blocks of target
- Burns in daylight
- Very fast pursuit speed

#### Lumberjack Zombie
**Role**: Block Destruction

| Stat | Value |
|------|-------|
| Health | 35 |
| Damage | 5 |
| Speed | 0.22 |

- Spawns with iron axe
- 3x wood break speed multiplier
- Applies bleeding (Wither) on hit
- Immune to sunlight

### Tier 2 - Mid Game (Gamestage 25-45)

#### Bruiser Zombie
**Role**: Tank

| Stat | Value |
|------|-------|
| Health | 60 |
| Damage | 8 |
| Speed | 0.2 |
| Knockback Resistance | 0.8 |

- Windup attack (1-second telegraph)
- Heavy footstep particles
- 1.5x wood break multiplier
- Immune to sunlight

#### Spitter Zombie
**Role**: Ranged Harassment

| Stat | Value |
|------|-------|
| Health | 20 |
| Damage | 2 |
| Speed | 0.22 |

- Shoots acid projectiles
- Maintains 6-10 block range
- 3-second attack cooldown
- Burns in daylight

#### Plague Zombie
**Role**: Attrition

| Stat | Value |
|------|-------|
| Health | 25 |
| Damage | 3 |
| Speed | 0.2 |

- Stacking sickness effect (max 5 stacks)
- Progressive debuffs:
  - 1+ stacks: Slowness
  - 2+ stacks: Hunger
  - 3+ stacks: Weakness
  - 4+ stacks: Nausea
  - 5 stacks: Wither
- Burns in daylight

#### Howler Zombie
**Role**: Support/Threat Escalator

| Stat | Value |
|------|-------|
| Health | 25 |
| Damage | 3 |
| Speed | 0.23 |

- Howls to buff nearby zombies (20-second cooldown)
- Grants Speed + Strength II for 10 seconds
- Very loud (attracts more zombies)
- Burns in daylight

#### Leech Zombie
**Role**: Sustain Threat

| Stat | Value |
|------|-------|
| Health | 30 |
| Damage | 4 |
| Speed | 0.23 |

- Heals 50% of damage dealt
- Dripping blood particle effects
- Burns in daylight

### Tier 3 - Late Game (Gamestage 50+)

#### Shielded Zombie
**Role**: Formation Breaker

| Stat | Value |
|------|-------|
| Health | 30 |
| Damage | 5 |
| Speed | 0.2 |

- Spawns with shield (offhand)
- Blocks frontal attacks (90° arc)
- Reduces blocked damage to 20%
- 15% chance to break shield (3-second cooldown)
- Immune to sunlight

#### Electric Zombie
**Role**: Area Denial

| Stat | Value |
|------|-------|
| Health | 25 |
| Damage | 4 |
| Speed | 0.22 |

- Charges lightning over 3 seconds
- AoE lightning damage (3-block radius)
- Distance-based damage falloff
- Constant electrical particles
- Burns in daylight

## Installation

### Requirements
- Minecraft 1.21.8
- Fabric Loader 0.16.0+
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

## Usage

### Horde Registration
All zombies automatically register with `HordeMobRegistry` at appropriate gamestage thresholds.

### Manual Spawning
```
/summon stn_zombies:bruiser_zombie
/summon stn_zombies:sprinter_zombie
/summon stn_zombies:spitter_zombie
...
```

## Technical Details

### File Structure
```
stn-zombies/
├── src/main/java/com/stn/zombies/
│   ├── STNZombies.java                  # Main entry point
│   ├── config/
│   │   └── STNZombiesConfig.java        # Balance values
│   ├── entity/
│   │   ├── BruiserZombieEntity.java
│   │   ├── SprinterZombieEntity.java
│   │   ├── SpitterZombieEntity.java
│   │   ├── ZombabieEntity.java
│   │   ├── HowlerZombieEntity.java
│   │   ├── PlagueZombieEntity.java
│   │   ├── ShieldedZombieEntity.java
│   │   ├── ElectricZombieEntity.java
│   │   ├── LeechZombieEntity.java
│   │   ├── LumberjackZombieEntity.java
│   │   ├── ai/
│   │   │   ├── WindupAttackGoal.java
│   │   │   ├── SpitAttackGoal.java
│   │   │   └── LeapAttackGoal.java
│   │   └── projectile/                  # Placeholder
│   └── registry/
│       └── STNZombieEntities.java
├── src/client/java/com/stn/zombies/client/
│   ├── STNZombiesClient.java
│   └── render/
│       └── ScaledZombieRenderer.java
└── src/main/resources/
    └── fabric.mod.json
```

### Integration Points
- **stn-mobai**: Implements `BlockBreakAnimatable` and `IBlockBreaker`
- **stn-survival**: Registers with `HordeMobRegistry`
- **stn-core**: Uses `ISoundEmitter` for howler sounds

## Dependencies

### Required
- stn-core
- stn-mobai
- stn-survival

## License

MIT License
