# STN-MobAI

> Advanced mob AI framework with sensory detection for Survive The Night

## Overview

STN-MobAI transforms hostile mobs into intelligent hunters with realistic sensory capabilities. Mobs can now detect players through sound, smell, light, heat, and village structures—making stealth and strategy essential for survival.

The module provides a framework for enhanced mob AI that other STN modules use to create challenging encounters. Zombies hear your footsteps, smell your presence through walls during survival nights, and actively break through obstacles to reach you.

## Features

### Sensory Detection System

Mobs equipped with the ISensoryMob interface can detect targets through five independent senses:

| Sense | Range | Through Walls | Trigger |
|-------|-------|---------------|---------|
| Sound | 32 blocks | No | Block breaking, combat, movement |
| Smell | 64 blocks | Yes | Players/villagers (survival night only) |
| Light | 48 blocks | No | Torches, fire, lanterns |
| Heat | 32 blocks | Yes | Fire, lava, furnaces |
| Village | 96 blocks | N/A | POI system (homes, meeting points) |

Each sense has:
- Configurable detection range
- Weight multiplier (0.0-1.0) affecting priority
- Enable/disable toggle per mob type

### Sound Event System

Centralized registry for all sound-producing actions:

```java
// Sound types tracked
GENERIC, BLOCK_BREAK, BLOCK_PLACE, DOOR_USE, CHEST_OPEN,
COMBAT, MOVEMENT, EXPLOSION, INTERACTION, LEVER, BUTTON, GOAT_HORN
```

**Automatic Sound Generation:**
- Block breaking/placing
- Door, chest, trapdoor interactions
- Combat (weapon-based volume)
- Player movement (sprint louder than walk)
- Armor noise multiplier
- Explosions (TNT, creepers)
- Goat horn usage (extra loud)

### Block Breaking AI

Mobs with `IBlockBreaker` interface actively destroy obstacles:

- **Hardness-aware**: Break time scales with material
- **Material multipliers**: Wood breaks faster than stone
- **Durability integration**: Uses stn-fortifications durability system
- **Unbreakable blocks**: Bedrock, portals, command blocks, barriers
- **Priority targeting**: Doors, fences, glass, walls

### Performance Optimizations

- **Staggered Processing**: Spreads AI checks across ticks
- **Caching**: 10-second region caches for light/heat detection
- **Block Sampling**: 2-block step sampling for scanning
- **Cooldown Throttling**: Sense checks every 3 seconds

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
    compileOnly project(':stn-survival')  // Optional
}
```

## Usage

### Debug Commands
```
/mobai debug     - Toggle debug mode (shows sense targets)
/mobai env       - Display environment info
```

### Implementing ISensoryMob

```java
public class CustomZombie extends ZombieEntity implements ISensoryMob {

    @Override
    public float getSenseRange(SenseType type) {
        return switch(type) {
            case SOUND -> 32.0f;
            case SMELL -> 64.0f;
            case LIGHT -> 48.0f;
            case HEAT -> 32.0f;
            case VILLAGE -> 96.0f;
        };
    }

    @Override
    public float getSenseWeight(SenseType type) {
        return 1.0f; // Full priority for all senses
    }
}
```

### Emitting Sounds

```java
// Emit a sound at a location
SenseManager.emitSound(world, pos, SoundType.COMBAT, 1.0f);

// Sound will decay over 5 seconds
// Nearby sensory mobs will investigate
```

## Technical Details

### File Structure
```
stn-mobai/
├── src/main/java/com/stn/mobai/
│   ├── STNMobAI.java                    # Main entry point
│   ├── config/
│   │   └── STNMobAIConfig.java          # Configuration
│   ├── entity/
│   │   ├── BlockBreakAnimatable.java    # Animation interface
│   │   ├── IBlockBreaker.java           # Block breaking interface
│   │   ├── ISensoryMob.java             # Sensory detection interface
│   │   └── STNHostileMob.java           # Base hostile class
│   ├── entity/ai/
│   │   ├── BreakBlockGoal.java          # Block breaking goal
│   │   ├── MobSenseGoal.java            # Sensory targeting goal
│   │   └── sense/
│   │       ├── HeatDetection.java
│   │       ├── LightDetection.java
│   │       ├── SenseManager.java        # Central sense system
│   │       ├── SoundEvent.java          # Sound data model
│   │       └── SoundVolumes.java        # Volume constants
│   ├── event/
│   │   ├── MobSpawnHandler.java         # AI injection on spawn
│   │   └── SoundEventHandler.java       # Event→sound conversion
│   ├── mixin/
│   │   ├── ExplosionMixin.java
│   │   ├── PlayerMovementMixin.java
│   │   └── ZombieEntityMixin.java
│   └── command/
│       └── DebugCommand.java
├── src/client/java/com/stn/mobai/client/
│   └── STNMobAIClient.java
└── src/main/resources/
    ├── fabric.mod.json
    ├── stn_mobai.mixins.json
    └── stn_mobai.accesswidener
```

### Sense Scoring Formula
```
score = volume * proximity * baseWeight * 100.0
proximity = 1.0 - (distance / maxRange)
```

### Block Breaking Speed
```
damagePerTick = (breakSpeedMultiplier * configMultiplier * materialMultiplier) / hardness
```

### Sense Evaluation Order (Performance)
1. Sound (pre-computed list) - Fastest
2. Smell (AABB entity query)
3. Light (cached scanning)
4. Heat (cached scanning)
5. Village (POI lookup) - Slowest

## API Integration

### Registering as Sound Emitter
```java
// During initialization
STNCore.registerSoundEmitter(senseManager);
```

### Using with Custom Mobs
```java
// In mob's initGoals()
this.goalSelector.add(1, new MobSenseGoal(this));
this.goalSelector.add(2, new BreakBlockGoal(this));
```

## Dependencies

### Required
- stn-core
- stn-fortifications (for durability integration)

### Optional
- stn-survival (for survival night smell detection)

## License

MIT License
