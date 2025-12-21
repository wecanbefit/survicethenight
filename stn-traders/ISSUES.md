# STN-Traders - Known Issues & TODOs

> Last updated: 2025-12-20

## Issues Summary

| Priority | Count |
|----------|-------|
| Critical | 0 |
| Medium | 0 |
| Low | 1 |
| **Total** | **1** |

---

## Low Priority Issues

### 1. Module Not Implemented
**Priority**: LOW (skeleton module)
**Location**: Entire module

**Description**: This is an empty skeleton module with no actual implementation.

**Current State**:
- `STNTraders.java`: Empty `onInitialize()` with TODO
- `STNTradersClient.java`: Empty `onInitializeClient()` with TODO
- No entity classes
- No trading mechanics
- No UI components
- No textures or models

**TODOs in Code**:
```java
// STNTraders.java:20
// TODO: Register traders and trading mechanics

// STNTradersClient.java:8
// TODO: Register client-side rendering
```

---

## Implementation Requirements

To complete this module, the following needs to be implemented:

### Entity System
- [ ] Merchant entity base class
- [ ] Specific merchant types (weapons, supplies, etc.)
- [ ] Entity registration
- [ ] Spawn mechanics

### Trading System
- [ ] Trade offer data model
- [ ] Trade registry
- [ ] Currency/barter system
- [ ] Gamestage-locked trades

### User Interface
- [ ] Trading GUI screen
- [ ] Trade offer display
- [ ] Inventory integration

### Client Rendering
- [ ] Entity models
- [ ] Entity textures
- [ ] Entity renderers
- [ ] Animation if needed

### Assets
- [ ] Merchant textures
- [ ] UI textures
- [ ] Sound effects
- [ ] Language strings

### Integration
- [ ] stn-core provider integration
- [ ] Gamestage awareness
- [ ] Survival night behavior

---

## Related Files

| File | Status |
|------|--------|
| `STNTraders.java` | Empty skeleton |
| `STNTradersClient.java` | Empty skeleton |
