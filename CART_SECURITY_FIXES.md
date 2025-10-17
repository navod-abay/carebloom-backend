# Cart Security & Functionality Fixes

**Date:** October 17, 2025  
**Status:** ✅ Completed  
**Impact:** Security improved, cart auto-loads properly

---

## 🔒 Security Fix: Controlled Dev Mode for Unregistered Users

### Problem
The backend was allowing **ANY authenticated Firebase user** to access cart endpoints, even if they weren't registered in the `mothers` collection. This was a security vulnerability in production.

### Solution
Added a **configuration flag** to control this behavior:

#### 1. New Configuration Property

**File:** `application.properties`
```properties
# Cart & Authentication Configuration
# WARNING: Set to false in production!
app.auth.allow-unregistered-mothers=true  # Development mode
```

**File:** `application-prod.properties`
```properties
# SECURITY: Cart & Authentication - PRODUCTION SETTINGS
# CRITICAL: Must be false in production for security
app.auth.allow-unregistered-mothers=false  # Production mode (SECURE)
```

#### 2. Updated Authentication Filter

**File:** `RoleAuthenticationFilter.java`

**Changes:**
- Added `@Value` annotation to inject configuration
- Updated `authenticateMother()` method to check the flag
- Added clear logging to warn about dev mode usage

**Behavior:**
- **Development (`true`)**: Allows cart access for testing, logs warning ⚠️
- **Production (`false`)**: Blocks access, logs error ❌

```java
if (allowUnregisteredMothers) {
    // DEV/TEST MODE: Allow temporary authentication
    logger.warn("⚠️ DEV MODE: User {} not in mothers collection, granting temporary cart access. DISABLE IN PRODUCTION!", firebaseUid);
    setAuthentication(firebaseUid, "MOTHER", firebaseUid, null);
} else {
    // PRODUCTION MODE: Block access
    logger.error("❌ PRODUCTION: User {} attempted cart access but not registered as mother. Access DENIED.", firebaseUid);
    // Don't set authentication - Spring Security will reject the request
}
```

### How to Use

#### For Development:
- Keep `app.auth.allow-unregistered-mothers=true` in `application.properties`
- Cart will work for testing without needing mother registration
- You'll see warnings in logs (this is intentional)

#### For Production:
- Ensure `app.auth.allow-unregistered-mothers=false` in `application-prod.properties`
- Only properly registered mothers can access cart
- Unregistered users will get 403 Forbidden

---

## 🛒 Cart Auto-Loading Fix

### Problem
Cart items weren't loading automatically when the app started. Users had to navigate to the cart page to see their items.

### Solution
Modified `CartContext` to auto-load cart when user authenticates.

**File:** `context/cartContext.tsx`

**Changes:**
1. Added `isInitialized` state to prevent infinite loops
2. Added `useEffect` to load cart when user authenticates
3. Cart clears automatically when user logs out
4. Initialization happens only once per session

```typescript
useEffect(() => {
  if (user && !isInitialized) {
    loadCart();
    setIsInitialized(true);
  } else if (!user && isInitialized) {
    // Clear cart when user logs out
    setCartItems([]);
    setIsInitialized(false);
  }
}, [user, isInitialized]);
```

### Benefits
- ✅ Cart count badge updates immediately on app launch
- ✅ No infinite loops or repeated API calls
- ✅ Cart persists across app navigation
- ✅ Cart clears on logout (security)
- ✅ Lightweight - loads only once per session

---

## 🧪 Testing Checklist

### Backend Testing

1. **Test Dev Mode:**
```bash
# Start backend with dev properties (default)
./mvnw spring-boot:run

# Try to access cart with any Firebase authenticated user
curl -X GET "http://localhost:8082/api/v1/cart" \
  -H "Authorization: Bearer <firebase-token>"

# Expected: Works, see warning in logs
```

2. **Test Production Mode:**
```bash
# Start backend with prod properties
./mvnw spring-boot:run -Dspring.profiles.active=prod

# Try to access cart with unregistered user
curl -X GET "http://localhost:8082/api/v1/cart" \
  -H "Authorization: Bearer <firebase-token>"

# Expected: 403 Forbidden (unless user is in mothers collection)
```

### Mobile Testing

1. **Test Cart Auto-Load:**
   - [ ] Login to mobile app
   - [ ] Add items to cart
   - [ ] Close and reopen app
   - [ ] Cart count badge shows correct number immediately
   - [ ] Navigate to cart - items are already there

2. **Test Cart Persistence:**
   - [ ] Add items to cart
   - [ ] Navigate away from marketplace
   - [ ] Return to marketplace
   - [ ] Cart count still shows correct number

3. **Test Logout:**
   - [ ] Add items to cart
   - [ ] Logout
   - [ ] Cart clears automatically
   - [ ] Login again
   - [ ] Cart loads from backend (if items were saved)

---

## 📊 Configuration Summary

| Environment | Property Value | Behavior | Security |
|-------------|---------------|----------|----------|
| **Development** | `true` | Allows any authenticated user to use cart | ⚠️ Low (for testing) |
| **Production** | `false` | Only registered mothers can use cart | ✅ High (secure) |

---

## ⚠️ Important Notes for Team

1. **DO NOT** set `allow-unregistered-mothers=true` in production
2. **ALWAYS** check logs for warning messages about dev mode
3. Cart will work for team testing without requiring mother registration
4. Before production deployment, verify `application-prod.properties` has the flag set to `false`

---

## 🔄 What Wasn't Changed

To maintain compatibility with the existing codebase:
- ✅ No changes to authentication flow structure
- ✅ No changes to Firebase authentication logic
- ✅ No changes to other user role authentication (Midwife, Admin, Vendor, MOH)
- ✅ No changes to existing API endpoints
- ✅ Cart API structure remains the same

Only added:
- Configuration flag for security control
- Better logging for debugging
- Cart auto-load on app start

---

## 🐛 Issues Fixed

1. ✅ **Security vulnerability** - Uncontrolled cart access by any user
2. ✅ **Cart not loading** - Had to manually navigate to cart page
3. ✅ **No production safety** - No way to enforce security in production

---

## 📝 Remaining Issues (For Future)

These issues were **NOT** fixed to maintain compatibility:

1. **Missing `vendorName` field** in `ProductResponse.java` (Issue #1)
2. **Missing `rating` fields** in Product model (Issue #2)
3. **Hardcoded API URLs** in mobile app (Issue #10, #11)
4. **No stock validation** on frontend before add to cart (Issue #13)

See `MARKETPLACE_CLEANUP_SUMMARY.md` for full issue list.

---

## 🚀 Deployment Instructions

### Development
```bash
# No changes needed, works as before
./mvnw spring-boot:run
```

### Production
```bash
# Make sure prod profile is active
./mvnw spring-boot:run -Dspring.profiles.active=prod

# Or set environment variable
export SPRING_PROFILES_ACTIVE=prod
./mvnw spring-boot:run
```

---

**Questions?** Check logs for detailed authentication messages with emoji indicators:
- ⚠️ = Warning (dev mode active)
- ❌ = Error (production mode blocking access)
- ✅ = Success (proper authentication)
