# Account Migration and Order Ownership Strategy

This document outlines the account migration and order ownership strategy for transitioning from legacy Eat-It (Java/XML) to the modernized Jetpack Compose app and Firebase Auth backend.

## Legacy Context & Risks

The original Eat-It customer application (`Client Side/EatIt2`) used custom phone-number keys in Firebase Realtime Database with plaintext password fields (`User/$phone/password`). Orders in `Requests` were keyed by timestamp/push IDs and contained phone numbers, names, and addresses, but no cryptographically verified `userId`.

### Key Security Requirements
1. **No Plaintext Password Import**: Plaintext passwords stored in the legacy `User` node must **never** be imported or converted into Firebase Authentication credentials.
2. **Password Resets / Verification**: Users reset passwords or enroll via standard Firebase Auth mechanisms (`sendPasswordResetEmail`, `createUserWithEmailAndPassword`).
3. **Explicit Order Ownership**: Orders are mapped to Firebase Auth UIDs through verified phone/email claims.

---

## Migration Architecture

```
+---------------------+          +----------------------+
| Legacy Database     |          | Firebase Auth        |
| User/$phone         | ------>  | UID: auth_uid_123    |
| (Name, Phone, Pass) |          | Email: user@ex.com   |
+---------------------+          +----------------------+
                                            |
                                            v
                                 +----------------------+
                                 | Requests/$orderId    |
                                 | userId: auth_uid_123 |
                                 | phone: +919876543210 |
                                 +----------------------+
```

---

## Step-by-Step Migration Workflow

### Phase 1: User Enrollment & Auth Reset
1. Existing users open the new Compose application.
2. User enters their email and requests a password reset link, or creates a new Firebase Auth account.
3. Upon first sign-in, Firebase Auth produces a secure JWT token with a unique `uid`.

### Phase 2: Order Ownership Linking
1. An administrative Cloud Function `linkLegacyOrders` is invoked when a user links their phone number or completes email verification.
2. The function queries `Requests` where `phone == user.phone` and `userId` is missing.
3. It updates matching `Requests` records with `userId = context.auth.uid` and integer `totalPaise`.
4. Security rules enforce that users can only query orders matching their authenticated `auth.uid`.

### Phase 3: Staging Rollback Rehearsal
1. Prior to production release, export a sanitized copy of legacy database nodes to Firebase Emulator.
2. Run the ownership migration script against emulator data.
3. Verify that:
   - Order history query (`orderByChild('userId').equalTo(uid)`) returns only the user's past orders.
   - Cross-user order reads are rejected by Database Security Rules.
   - Rollback script restores database to pre-migration backup state if validation fails.
