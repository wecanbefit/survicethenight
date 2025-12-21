# STN-Traders

> Trading mechanics and merchant entities for Survive The Night

## Overview

STN-Traders is planned to provide trading NPCs and merchant mechanics to the Survive The Night modpack. Currently, this module is a skeleton with placeholder implementations awaiting development.

The envisioned system will allow players to find or summon merchant NPCs who trade survival supplies, weapons, and rare materials in exchange for currency or barter items.

## Status

**Development Status**: Not Implemented

This module contains only the basic Fabric mod structure:
- Main initializer class
- Client initializer class
- Mod metadata

All actual trading functionality remains to be implemented.

## Planned Features

### Merchant NPCs
- Wandering traders with survival-themed goods
- Stationary merchants for player bases
- Unique merchant types (Weapons Dealer, Medical Supplier, etc.)

### Trading System
- Currency or barter-based economy
- Tiered goods based on gamestage
- Supply/demand mechanics

### Integration Points
- Gamestage-locked items
- Survival night safe zones
- Village integration

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

## Technical Details

### Current File Structure
```
stn-traders/
├── src/main/java/com/stn/traders/
│   └── STNTraders.java              # Entry point (empty)
├── src/client/java/com/stn/traders/client/
│   └── STNTradersClient.java        # Client init (empty)
└── src/main/resources/
    └── fabric.mod.json
```

### TODOs in Code
```java
// STNTraders.java:20
// TODO: Register traders and trading mechanics

// STNTradersClient.java:8
// TODO: Register client-side rendering
```

## Dependencies

### Required
- stn-core

## License

MIT License
