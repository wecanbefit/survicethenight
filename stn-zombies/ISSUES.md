# STN-Zombies - Known Issues & TODOs

> Last updated: 2025-12-20

## Issues Summary

| Priority | Count |
|----------|-------|
| Critical | 0 |
| Medium | 3 |
| Low | 1 |
| **Total** | **4** |

---

## Medium Priority Issues

### 1. Configuration Not Loaded from File
**Priority**: MEDIUM
**Location**: `STNZombiesConfig.java:79`

**Description**: All balance values are hardcoded. The `init()` method is a no-op.

**TODO Comment in Code**:
```java
// TODO: Load from config file
```

**Impact**: Users cannot adjust zombie stats without code changes.

**Fix Required**: Implement config file loading via Cloth Config or JSON.

---

### 2. ~~Plague Stacks Memory Leak~~ ✅ FIXED
**Priority**: MEDIUM
**Location**: `PlagueZombieEntity.java`, `STNZombies.java`
**Status**: **FIXED** (2025-12-20)

**Description**: Static `Map<UUID, Integer>` tracks plague stacks per player but never cleared entries for disconnected players.

**Fix Applied**: Added cleanup handler in `STNZombies.onInitialize()`:
```java
ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
    PlagueZombieEntity.clearPlagueStacks(handler.player.getUuid());
});
```

Plague stacks are now properly cleared when players disconnect.

---

### 3. Custom Acid Projectile Missing
**Priority**: MEDIUM
**Location**: `SpitterZombieEntity.java:77`, `entity/projectile/`

**Description**: Spitter zombie uses vanilla `SmallFireballEntity` as placeholder. The `projectile/` directory exists but is empty.

**Comment in Code**:
```java
// will customize later
```

**Impact**: Acid attack uses fire particles/damage instead of custom acid mechanics.

**Fix Required**: Implement custom `AcidProjectileEntity` with poison damage and appropriate visuals.

---

## Low Priority Issues

### 4. Missing Custom Textures
**Priority**: LOW
**Location**: `ScaledZombieRenderer.java`

**Description**: Renderer expects custom textures at `assets/stn_zombies/textures/entity/{name}.png` but no textures exist.

**Impact**: All zombies use vanilla zombie/husk textures.

**Recommendation**: Create unique textures for each variant.

---

## Code Quality Notes

### Shield Re-equipping
**Location**: `ShieldedZombieEntity.java:61`

```java
if (this.getOffHandStack().isEmpty() && !this.getWorld().isClient()) {
    this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
}
```

Shield is re-equipped every tick if lost. Consider cooldown or event-based approach.

### AI Goal Priorities
All custom goals use priority 2. Consider explicit priority management for cleaner AI behavior.

---

## Enhancement Opportunities

1. **Custom Loot Tables**: Unique drops per zombie variant
2. **Sound Effects**: Custom sounds for each zombie type
3. **Advancement Integration**: Achievements for killing each variant
4. **Spawn Eggs**: Creative mode spawn eggs for testing

---

## Related Files

| File | Issue Count |
|------|-------------|
| `STNZombiesConfig.java` | 1 |
| `PlagueZombieEntity.java` | 1 |
| `SpitterZombieEntity.java` | 1 |
| `ScaledZombieRenderer.java` | 1 |
