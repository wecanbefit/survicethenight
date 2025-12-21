# STN-Repair - Known Issues & TODOs

> Last updated: 2025-12-20

## Issues Summary

| Priority | Count |
|----------|-------|
| Critical | 0 |
| Medium | 1 |
| Low | 1 |
| **Total** | **2** |

---

## Medium Priority Issues

### 1. Steel Block Repair Commented Out
**Priority**: MEDIUM
**Location**: `HammerItem.java:131-144`

**Description**: Steel ingot repair requirement for steel blocks is commented out with note "STN may not have steel ingots".

**Code**:
```java
// Steel blocks would require steel ingots
// But STN may not have steel ingots
// Commented out for now
```

**Impact**: Steel blocks (if they exist) cannot be repaired with materials.

**Options**:
- Implement steel ingot item in stn-fortifications
- Use alternative repair material (iron blocks?)
- Remove steel block repair entirely

---

## Low Priority Issues

### 2. Config File Not Loaded
**Priority**: LOW
**Location**: `STNRepairConfig.java:18`

**Description**: Config values are hardcoded, with TODO for file-based loading.

**TODO in Code**:
```java
// TODO: Load from config file in future if needed
```

**Impact**: HUD position cannot be configured by users without code changes.

**Recommendation**: Implement config file via Cloth Config.

---

## Code Quality Notes

### HammerItem Size
`HammerItem.java` is 298 lines, handling both tracked block and special block repair logic.

**Recommendation**: Consider splitting into separate handler classes for:
- Tracked block repairs
- Special block repairs
- Material requirement lookup

### Client-Server Sync
HUD shows "Checking..." when durability cache is empty. This is working as designed but may confuse users briefly.

---

## Enhancement Opportunities

1. **Steel Block Support**: Add steel ingot or alternative repair method
2. **Config File Loading**: Implement persistent configuration
3. **Repair Particles**: More visual feedback during repair
4. **Sound Variety**: Different sounds per hammer tier
5. **Repair Efficiency Enchantment**: Custom enchantment for better repairs

---

## Related Files

| File | Issue Count |
|------|-------------|
| `HammerItem.java` | 1 |
| `STNRepairConfig.java` | 1 |
