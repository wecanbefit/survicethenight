# STN-MobAI - Known Issues & TODOs

> Last updated: 2025-12-20

## Issues Summary

| Priority | Count |
|----------|-------|
| Critical | 1 |
| Medium | 2 |
| Low | 1 |
| **Total** | **4** |

---

## Critical Issues

### 1. ~~Missing Mixin Registrations~~ ✅ PARTIALLY FIXED
**Priority**: CRITICAL
**Location**: `stn_mobai.mixins.json`
**Status**: **PARTIALLY FIXED** (2025-12-20)

**Description**: The mixin JSON was missing mixin registrations.

**Fix Applied**:
- Added `ZombieEntityMixin` to mixin config ✅
- `ExplosionMixin` disabled due to 1.21 API compatibility issues (Shadow fields and injection target don't match current Explosion class)

**Current mixin config**:
```json
{
    "mixins": [
        "PlayerMovementMixin",
        "ZombieEntityMixin"
    ]
}
```

**Remaining Work**: ExplosionMixin needs rewrite for 1.21 Explosion API changes before re-enabling.

---

## Medium Priority Issues

### 2. Configuration Not Loaded from File
**Priority**: MEDIUM
**Location**: `STNMobAIConfig.java:8`

**Description**: All configuration values are hardcoded. No way for users to adjust detection ranges, speeds, or mob exclusions.

**TODO Comment in Code**:
```java
// TODO: Load from config file
```

**Impact**: Users cannot customize AI behavior without code changes.

**Fix Required**: Implement JSON or TOML config file loading.

---

### 3. Armor Noise Calculation Fragile
**Priority**: MEDIUM
**Location**: `SoundEventHandler.java`

**Description**: Armor noise calculation uses string-based item ID matching which depends on naming conventions.

**Example**:
```java
if (itemId.contains("chainmail")) multiplier *= 1.2f;
if (itemId.contains("iron")) multiplier *= 1.1f;
```

**Impact**: Custom armor mods may not contribute appropriate noise.

**Recommendation**: Use armor material or attribute-based detection.

---

## Low Priority Issues

### 4. Cache Staleness
**Priority**: LOW
**Location**: `HeatDetection.java`, `LightDetection.java`

**Description**: 200-tick (10-second) cache for heat/light detection may become stale during active gameplay.

**Impact**: Mobs may target removed light/heat sources briefly.

**Recommendation**: Add cache invalidation on block change events.

---

## Performance Observations

### Stagger Offset Collisions
**Location**: `MobSenseGoal.java`

Using `mob.getId() % STAGGER_RANGE` for tick offset can cause collisions when many mobs have similar IDs.

**Recommendation**: Use hash-based distribution instead.

### SenseDebugger Overhead
**Location**: `SenseDebugger.java`

Scans all mobs every second even when debug mode is disabled.

**Recommendation**: Add early exit when debug is off.

---

## API Gaps

1. No way to register custom sound types beyond the 7 defined
2. No direct access to modify active sense targets from other mods
3. No callbacks for sense detection events (would help debugging)

---

## Enhancement Opportunities

1. **Custom Sound Types**: Allow mods to register additional sound categories
2. **Sense Event Callbacks**: Fire events when mobs detect targets
3. **Per-Mob Configuration**: Different ranges for different mob types
4. **Visual Debug Mode**: Particle effects showing detection ranges

---

## Related Files

| File | Issue Count |
|------|-------------|
| `stn_mobai.mixins.json` | 1 (critical) |
| `STNMobAIConfig.java` | 1 |
| `SoundEventHandler.java` | 1 |
| `HeatDetection.java` | 1 |
