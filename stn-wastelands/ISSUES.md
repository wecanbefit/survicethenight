# STN-Wastelands - Known Issues & TODOs

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

### 1. Config Integration TODO
**Priority**: MEDIUM
**Location**: `WastelandConfig.java:5`

**Description**: TODO comment indicates config system needs proper integration.

**TODO in Code**:
```java
// TODO: Integrate with a proper config system (JSON, TOML, etc.)
```

**Current State**: JSON persistence exists and works, but marked as TODO.

**Impact**: Config works but may not follow best practices.

---

### 2. Client Rendering Not Implemented
**Priority**: MEDIUM
**Location**: `STNWastelandsClient.java:8`

**Description**: Client initializer is empty with TODO for rendering.

**TODO in Code**:
```java
// TODO: Register client-side rendering
```

**Impact**: No visual effects for wasteland atmosphere.

**Potential Features**:
- Red-tinted sky in villages
- Decay particles
- Zombie sound ambience

---

## Low Priority Issues

### 3. Unused Config Parameters
**Priority**: LOW
**Location**: `WastelandConfig.java`, `WastelandLootModifier.java`

**Description**: Several config parameters are defined but not used:
- `rareLootChance`
- `maxDistanceForBonus`
- `maxLootQualityMultiplier`

**Impact**: Config options exist but have no effect.

**Fix**: Either implement the features or remove unused parameters.

---

### 4. Potential Array Index Issue
**Priority**: LOW
**Location**: `LootConfig.java`

**Description**: Gamestage tier calculations could exceed array bounds if gamestage > 99.

**Calculation**:
```java
int tier = gamestage / 10;  // tier 10+ if gamestage >= 100
```

**Impact**: ArrayIndexOutOfBoundsException at very high gamestages.

**Fix**: Add bounds checking: `Math.min(tier, 9)`.

---

## Design Notes

### Sunlight Immunity Workaround
**Location**: `VillageZombifier.java`

Zombies are made sunlight-immune by equipping stone buttons as invisible helmets:
```java
zombie.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.STONE_BUTTON));
```

**Consideration**: This workaround may break if players notice items drop on death.

**Alternative**: Use custom attribute or mixin for true sunlight immunity.

---

## Enhancement Opportunities

1. **Client-Side Effects**: Implement atmospheric rendering for villages
2. **Distance-Based Loot**: Use `maxDistanceForBonus` parameter
3. **Rare Loot System**: Implement `rareLootChance` parameter
4. **Tier Bounds Check**: Add safety for high gamestages
5. **Custom Sunlight Immunity**: Replace helmet workaround

---

## Related Files

| File | Issue Count |
|------|-------------|
| `WastelandConfig.java` | 2 |
| `STNWastelandsClient.java` | 1 |
| `LootConfig.java` | 1 |
