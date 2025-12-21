# STN-Survival - Known Issues & TODOs

> Last updated: 2025-12-20

## Issues Summary

| Priority | Count |
|----------|-------|
| Critical | 0 |
| Medium | 2 |
| Low | 1 |
| **Total** | **3** |

---

## Medium Priority Issues

### 1. HUD Overlay Not Rendering
**Priority**: MEDIUM
**Location**: `GamestageHudOverlay.java`

**Description**: The HUD overlay's `register()` method doesn't actually render anything. Comment indicates data is used by other HUD elements.

**Comment in Code**:
```java
// Rendering disabled - data is used by other HUD elements
```

**Impact**: Gamestage HUD relies entirely on stn-hud module.

**Consideration**: Intentional design, but could add fallback rendering.

---

### 2. Boss Mob Selection Incomplete
**Priority**: MEDIUM
**Location**: `SurvivalNightManager.java`

**Description**: `selectBossMob()` only searches for "giant" and "warden" entities, neither of which are registered by default in vanilla Minecraft with the expected identifiers.

**Impact**: Boss spawning at gamestage 100+ may return null and skip boss spawn.

**Recommendation**: Add support for custom boss registration similar to `registerCustomMob()`.

---

## Low Priority Issues

### 3. Jockey Spawn Rate Capping
**Priority**: LOW
**Location**: `JockeySpawner.java`

**Description**: Jockey spawn chance capped at 20% maximum regardless of gamestage.

**Calculation**:
```java
float chance = 0.05f + (gamestage / 20f) * 0.01f;
return Math.min(chance, 0.20f);
```

**Impact**: At very high gamestages (100+), jockeys may feel predictable.

**Recommendation**: Consider higher cap or alternative scaling at extreme gamestages.

---

## Design Observations

### Mob Persistence
All spawned horde mobs are set to persistent (`setPersistent()`), meaning they won't despawn when players move away.

**Impact**: Large hordes could persist in the world, causing performance issues.

**Consideration**: Add cleanup for mobs far from all players after survival night ends.

### Horde Size Hard Cap
Maximum horde size is capped at 200 regardless of player count or gamestage.

**Impact**: Large multiplayer servers may find this limit too low.

**Recommendation**: Make cap configurable.

---

## Enhancement Opportunities

1. **Boss Registry**: Allow custom boss mob registration like regular mobs
2. **Difficulty Modes**: Easy/Normal/Hard presets for survival night
3. **Statistics Tracking**: Track survival nights completed, mobs killed, etc.
4. **Achievement Integration**: Advancements for milestones
5. **Configurable Jockey Cap**: Adjust max jockey spawn chance

---

## Related Files

| File | Issue Count |
|------|-------------|
| `GamestageHudOverlay.java` | 1 |
| `SurvivalNightManager.java` | 1 |
| `JockeySpawner.java` | 1 |
