# STN-Skeletons - Known Issues & TODOs

> Last updated: 2025-12-20

## Issues Summary

| Priority | Count |
|----------|-------|
| Critical | 0 |
| Medium | 3 |
| Low | 2 |
| **Total** | **5** |

---

## Medium Priority Issues

### 1. Arrow Creation Method
**Priority**: MEDIUM
**Location**: Ranged skeleton entity classes

**Description**: Using `new ArrowEntity(world, shooter, itemStack, null)` directly. Modern Minecraft may prefer factory methods.

**Impact**: May cause compatibility issues with future versions.

**Recommendation**: Verify API is current for 1.21.x and use factory if available.

---

### 2. Missing Texture Files
**Priority**: MEDIUM
**Location**: `assets/stn_skeletons/textures/entity/`

**Description**: Code expects custom textures for each skeleton variant, but no PNG files exist in the resources.

**Expected Files**:
- `marksman_skeleton.png`
- `suppressor_skeleton.png`
- `flame_archer_skeleton.png`
- `vanguard_skeleton.png`
- `duelist_skeleton.png`
- `reaper_skeleton.png`

**Impact**: All skeletons use default vanilla textures.

---

### 3. Configuration Hardcoded
**Priority**: MEDIUM
**Location**: `STNSkeletonsConfig.java`

**Description**: All balance values are hardcoded with no config file support.

**Impact**: Users cannot adjust skeleton stats.

**Fix Required**: Add config file loading.

---

## Low Priority Issues

### 4. SoundEvents API Usage
**Priority**: LOW
**Location**: `VanguardSkeletonEntity.java:117`

**Description**: Using `SoundEvents.ITEM_SHIELD_BREAK.value()` directly.

**Code**:
```java
this.playSound(SoundEvents.ITEM_SHIELD_BREAK.value(), 1.0f, 1.0f);
```

**Recommendation**: Verify `.value()` is the correct accessor for 1.21.

---

### 5. MatrixStack Rendering
**Priority**: LOW
**Location**: `ScaledSkeletonRenderer.java:26`

**Description**: Overriding `scale(SkeletonEntityRenderState state, MatrixStack matrices)`. MatrixStack API may change in future versions.

**Impact**: Rendering may need updates for 1.21.x+ versions.

---

## Enhancement Opportunities

1. **Custom Sounds**: Unique sounds per skeleton type
2. **Loot Tables**: Specialized drops per variant
3. **Advancement Integration**: Kill achievements
4. **AI Priority Management**: Explicit goal priorities
5. **Difficulty Scaling**: Stats adjust with difficulty setting

---

## Related Files

| File | Issue Count |
|------|-------------|
| Various entity classes | 1 (arrow creation) |
| `assets/textures/` | 1 (missing files) |
| `STNSkeletonsConfig.java` | 1 |
| `VanguardSkeletonEntity.java` | 1 |
| `ScaledSkeletonRenderer.java` | 1 |
