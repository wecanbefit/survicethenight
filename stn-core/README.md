# STN-Core

> Foundation API module for the Survive The Night modpack

## Overview

STN-Core is the foundational module that provides shared interfaces, events, and utilities for all other Survive The Night modules. It uses a provider pattern for loose coupling, allowing modules to expose functionality without direct dependencies.

This module has **zero external dependencies** beyond the Fabric API, making it the base layer upon which all other STN modules are built. Every other module in the Survive The Night ecosystem depends on stn-core.

## Features

### Provider Interfaces

STN-Core defines four key provider interfaces that other modules implement:

#### ISurvivalNightProvider
Exposes survival night event state and control.

```java
// Check if survival night is active
boolean isActive = STNCore.isSurvivalNightActive();

// Get days until next survival night
int daysRemaining = STNCore.getDaysUntilSurvivalNight();

// Force start a survival night (for commands/testing)
STNCore.getSurvivalNightProvider().forceStart();
```

#### IGamestageProvider
Provides access to the gamestage progression system.

```java
// Get world gamestage (affects spawning)
int worldStage = STNCore.getWorldGamestage();

// Get player-specific gamestage
int playerStage = STNCore.getPlayerGamestage(player.getUuid());

// Get horde size multiplier based on gamestage
float multiplier = STNCore.getHordeSizeMultiplier();
```

#### ISoundEmitter
Registers sounds that mobs can detect.

```java
// Available sound types
enum SoundType {
    GENERIC, BLOCK_BREAK, BLOCK_PLACE, DOOR_USE,
    CHEST_OPEN, COMBAT, MOVEMENT, EXPLOSION,
    INTERACTION, LEVER, BUTTON, GOAT_HORN
}

// Emit a sound at a location
STNCore.getSoundEmitter().emitSound(world, pos, SoundType.COMBAT, volume);
```

#### IDurabilityProvider
Exposes block durability tracking system.

```java
// Check block durability
int current = STNCore.getDurabilityProvider().getDurability(world, pos);
int max = STNCore.getDurabilityProvider().getMaxDurability(world, pos);

// Get durability percentage
float percent = STNCore.getDurabilityProvider().getDurabilityPercent(world, pos);
```

### Event System

STN-Core provides a centralized event system using Fabric's EventFactory:

```java
// Available events
STNEvents.SURVIVAL_NIGHT_START    // When survival night begins
STNEvents.SURVIVAL_NIGHT_END      // When survival night ends
STNEvents.GAMESTAGE_CHANGED       // When player gamestage changes
STNEvents.SOUND_REGISTERED        // When sounds are registered
STNEvents.BLOCK_DAMAGED           // When tracked blocks take damage
STNEvents.BLOCK_REPAIRED          // When blocks are repaired
STNEvents.BLOCK_DESTROYED         // When block durability reaches 0

// Register a listener
STNEvents.SURVIVAL_NIGHT_START.register((world) -> {
    // Handle survival night start
});
```

## Installation

### Requirements
- Minecraft 1.21.8
- Fabric Loader 0.18.3+
- Java 21+
- Fabric API

### As a Dependency
Add to your `build.gradle`:

```gradle
dependencies {
    implementation project(':stn-core')
}
```

Add to your `fabric.mod.json`:

```json
{
    "depends": {
        "stn_core": "*"
    }
}
```

## Technical Details

### File Structure
```
stn-core/
├── src/main/java/com/stn/core/
│   ├── STNCore.java                    # Main entry point
│   └── api/
│       ├── STNEvents.java              # Event registry
│       ├── ISurvivalNightProvider.java
│       ├── IGamestageProvider.java
│       ├── ISoundEmitter.java
│       └── IDurabilityProvider.java
├── src/client/java/com/stn/core/client/
│   └── STNCoreClient.java              # Client initializer
└── src/main/resources/
    └── fabric.mod.json
```

### Provider Registration
Modules register their provider implementations during initialization:

```java
// In stn-survival's initialization
STNCore.registerSurvivalNightProvider(survivalNightManager);
STNCore.registerGamestageProvider(gamestageManager);

// In stn-mobai's initialization
STNCore.registerSoundEmitter(senseManager);

// In stn-fortifications' initialization
STNCore.registerDurabilityProvider(durabilityProviderImpl);
```

### Thread Safety
- All provider fields are static and set once during mod initialization
- Getter methods are null-safe with sensible defaults
- No synchronization required for normal usage

## Dependent Modules

All Survive The Night modules depend on stn-core:

| Module | Uses Providers |
|--------|----------------|
| stn-survival | Implements ISurvivalNightProvider, IGamestageProvider |
| stn-mobai | Implements ISoundEmitter |
| stn-fortifications | Implements IDurabilityProvider |
| stn-zombies | Uses all providers |
| stn-skeletons | Uses gamestage, survival |
| stn-spiders | Uses gamestage, survival |
| stn-hud | Uses gamestage |
| stn-repair | Uses durability |
| stn-wastelands | Uses all providers |
| stn-traders | Uses core (planned) |

## API Reference

### STNCore Static Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `isSurvivalNightActive()` | boolean | Check if survival night is active |
| `getDaysUntilSurvivalNight()` | int | Days until next survival night |
| `getWorldGamestage()` | int | Current world gamestage |
| `getPlayerGamestage(UUID)` | int | Player-specific gamestage |
| `getHordeSizeMultiplier()` | float | Horde size multiplier |
| `getSurvivalNightProvider()` | ISurvivalNightProvider | Get provider instance |
| `getGamestageProvider()` | IGamestageProvider | Get provider instance |
| `getSoundEmitter()` | ISoundEmitter | Get emitter instance |
| `getDurabilityProvider()` | IDurabilityProvider | Get provider instance |

## Dependencies

### Required
- Fabric API (any version)
- Minecraft ~1.21
- Java 21+
- Fabric Loader 0.18.3+

### No Project Dependencies
stn-core has zero dependencies on other STN modules.

## License

MIT License
