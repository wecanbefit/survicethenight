# Survive The Night - Issues Summary

> Aggregated issues across all modules | Last updated: 2025-12-20

## Overview

| Module | Critical | Medium | Low | Total |
|--------|----------|--------|-----|-------|
| stn-core | 0 | 1 | 1 | 2 |
| stn-fortifications | 1 | 3 | 2 | 6 |
| stn-mobai | 1 | 2 | 1 | 4 |
| stn-survival | 0 | 2 | 1 | 3 |
| stn-zombies | 0 | 3 | 1 | 4 |
| stn-skeletons | 0 | 3 | 2 | 5 |
| stn-spiders | 0 | 2 | 2 | 4 |
| stn-hud | 1 | 4 | 3 | 8 |
| stn-repair | 0 | 1 | 1 | 2 |
| stn-traders | 0 | 0 | 1 | 1 |
| stn-wastelands | 0 | 2 | 2 | 4 |
| **TOTAL** | **3** | **23** | **17** | **43** |

---

## Critical Issues (Fix Immediately)

### 1. stn-fortifications: Rendering Disabled
**File**: `BlockDurabilityOverlay.java:20-22`

Block durability overlay completely disabled due to Minecraft 1.21.8 RenderPipelines API changes. Durability tracking works server-side but clients see no visual feedback.

### 2. stn-mobai: Missing Mixin Registrations
**File**: `stn_mobai.mixins.json:5-6`

`ZombieEntityMixin` and `ExplosionMixin` exist in source but aren't registered in the mixin JSON. Zombie AI injection and explosion sound detection may not work.

### 3. stn-hud: String Comparison Bugs
**Files**: `ModRPGHud.java:77`, `Settings.java:378-380`

Using `==` instead of `.equals()` for string comparison. Settings may fail to load correctly.

---

## Cross-Module Issues

### Configuration Systems Not Loaded
All modules have hardcoded configuration with TODO comments for file-based loading:
- stn-zombies: `STNZombiesConfig.java:79`
- stn-mobai: `STNMobAIConfig.java:8`
- stn-skeletons: `STNSkeletonsConfig.java`
- stn-spiders: `STNSpidersConfig.java`
- stn-repair: `STNRepairConfig.java:18`
- stn-wastelands: `WastelandConfig.java:5` (partially addressed)

### Missing Textures/Models
Several modules reference texture paths that don't exist:
- stn-zombies: Custom zombie textures
- stn-skeletons: Custom skeleton textures
- stn-spiders: Custom spider textures

---

## Per-Module Issue Files

For detailed issue breakdowns, see individual module ISSUES.md files:

- [stn-core/ISSUES.md](stn-core/ISSUES.md)
- [stn-fortifications/ISSUES.md](stn-fortifications/ISSUES.md)
- [stn-mobai/ISSUES.md](stn-mobai/ISSUES.md)
- [stn-survival/ISSUES.md](stn-survival/ISSUES.md)
- [stn-zombies/ISSUES.md](stn-zombies/ISSUES.md)
- [stn-skeletons/ISSUES.md](stn-skeletons/ISSUES.md)
- [stn-spiders/ISSUES.md](stn-spiders/ISSUES.md)
- [stn-hud/ISSUES.md](stn-hud/ISSUES.md)
- [stn-repair/ISSUES.md](stn-repair/ISSUES.md)
- [stn-traders/ISSUES.md](stn-traders/ISSUES.md)
- [stn-wastelands/ISSUES.md](stn-wastelands/ISSUES.md)

---

## Priority Matrix

### Immediate (Before Next Release)
1. Fix stn-fortifications rendering API
2. Register missing stn-mobai mixins
3. Fix stn-hud string comparisons

### Short Term
1. Implement config file loading across modules
2. Add missing language translations
3. Create custom textures for mob variants

### Long Term
1. Complete stn-traders implementation
2. Add advancement system
3. Improve documentation coverage
