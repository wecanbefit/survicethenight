# STN-Core - Known Issues & TODOs

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

### 1. ~~No Null Validation in Provider Getters~~ ✅ FIXED
**Priority**: MEDIUM
**Location**: `STNCore.java`
**Status**: **FIXED** (2025-12-20)

**Description**: Direct provider getter methods can return null if no provider is registered.

**Fix Applied**: Added `@Nullable` annotations to all provider getter methods to document potential null returns.

---

## Low Priority Issues

### 2. Empty Client Initializer
**Priority**: LOW
**Location**: `STNCoreClient.java`

**Description**: Client initializer only logs a message and performs no actual setup.

```java
public void onInitializeClient() {
    LOGGER.info("STN-Core client initialized");
    // No actual client setup
}
```

**Impact**: None currently. Client may need setup in future for HUD integration or rendering.

---

## Design Observations

### Event System Simplicity
The current event system using Fabric's EventFactory is simple but limited:
- No event priority/ordering
- No event cancellation mechanism
- Callbacks process sequentially with no error isolation

**Consideration**: May need to enhance if cross-module event coordination becomes complex.

### Initialization Order
Provider registration order depends on mod load order:
- No guarantee which module initializes first
- Overwriting providers logs a warning but doesn't prevent issues

**Recommendation**: Consider adding initialization phases or dependency-based ordering.

---

## Enhancement Opportunities

1. **Add Provider Validation**: Throw informative exceptions when calling methods on unregistered providers
2. **Event Priority System**: Add priority levels to event listeners
3. **Documentation**: Add more inline documentation for API consumers
4. **Version Interface**: Add API versioning for future compatibility

---

## Related Files

| File | Issue Count |
|------|-------------|
| `STNCore.java` | 1 |
| `STNCoreClient.java` | 1 |
