# STN-Spiders - Known Issues & TODOs

> Last updated: 2025-12-20

## Issues Summary

| Priority | Count |
|----------|-------|
| Critical | 0 |
| Medium | 2 |
| Low | 2 |
| **Total** | **4** |

---

## Medium Priority Issues

### 1. Stalker Camouflage Rendering Incomplete
**Priority**: MEDIUM
**Location**: `StalkerSpiderEntity.java`, `ScaledSpiderRenderer.java`

**Description**: `getVisibility()` method is defined in the entity but `ScaledSpiderRenderer` doesn't implement transparency rendering based on this value.

**Code in Entity**:
```java
@Override
public float getVisibility() {
    if (isCamouflaged()) return 0.2f;
    return 1.0f;
}
```

**Impact**: Stalker spider never actually becomes semi-invisible visually.

**Fix Required**: Implement custom rendering with alpha channel based on visibility value.

---

### 2. stn-mobai Dependency Unused
**Priority**: MEDIUM
**Location**: `build.gradle`, entity classes

**Description**: Module declares dependency on stn-mobai but no custom AI goals or interfaces are used.

**Impact**: Dependency overhead with no benefit.

**Options**:
- Remove stn-mobai dependency if not needed
- Implement custom AI behaviors using the framework

---

## Low Priority Issues

### 3. Missing Custom Textures
**Priority**: LOW
**Location**: `ScaledSpiderRenderer.java`

**Description**: Renderer references texture paths but no actual texture files exist.

**Impact**: All spiders use vanilla spider texture.

---

### 4. Broodmother Spawn Limit Not Cumulative
**Priority**: LOW
**Location**: `BroodmotherSpiderEntity.java`

**Description**: Death burst spawns fixed 3 spiderlings regardless of already-spawned count.

**Impact**: Could create exploits or balancing issues.

**Recommendation**: Track total spawned and respect limit across death burst.

---

## Enhancement Opportunities

1. **Implement Stalker Transparency**: Custom render layer for visibility
2. **Custom AI Goals**: Use stn-mobai for web placement, coordinated attacks
3. **Sound Effects**: Unique sounds per spider type
4. **Particles**: Web particles for Webspinner, venom drips for Venom
5. **Loot Tables**: Custom drops per variant

---

## Related Files

| File | Issue Count |
|------|-------------|
| `StalkerSpiderEntity.java` | 1 |
| `ScaledSpiderRenderer.java` | 1 |
| `build.gradle` | 1 |
| `BroodmotherSpiderEntity.java` | 1 |
