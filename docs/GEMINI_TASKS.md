# Gemini task list: Eat It modernization

Work from branch `feat/compose-design-system`. The active Android project is `android/`; preserve the archived Java/XML customer and staff projects unless a task explicitly migrates them. Read `android/README.md`, `docs/DESIGN_SYSTEM.md`, `docs/CAPABILITY_ROADMAP.md`, and `docs/CODEX_HANDOFF.md` before changing code.

## Working rules

- Use Kotlin DSL and `android/gradle/libs.versions.toml` for every plugin and dependency version. The standard filename is plural: `libs.versions.toml`.
- Keep Gradle 9.7.1, AGP 9.4.0, built-in Kotlin 2.2.10, and JDK 17 compatibility unless an official compatibility issue requires a documented change.
- Keep the reusable `designsystem` module independent of Firebase, app models, and navigation controllers.
- Preserve customer data and never port the legacy plaintext-password flow.
- Do not deploy Firebase, migrate production data, publish, merge, or claim live ordering works.
- After each task, run the smallest relevant tests. Before handoff, run the full verification command below.
- Update this file by checking a box only when its acceptance evidence exists. Record evidence below the task.

## P0 — make live ordering safe

- [x] Implement an authenticated Firebase callable `placeOrder` in a new backend module.
  - Load canonical dishes by ID and compute integer `totalPaise` on the server.
  - Validate quantities, contact fields, catalog availability, authentication, and maximum order bounds.
  - Accept a client quote/version and return an explicit price-change response that requires confirmation.
  - Store a UID-scoped idempotency record before acknowledging success so retries produce one order.
  - Write compatible legacy order fields plus `userId`, integer totals, timestamps, and audit-safe status.
- [x] Add Realtime Database rules and Firebase Emulator tests.
  - Prove catalog public-read/staff-write rules, order owner reads, staff updates, rejected cross-user access, and rejected client-written prices/status.
  - Prove duplicate concurrent request IDs produce one order.
- [x] Persist checkout request ID and quote across process death.
  - Clear only after acknowledged success or explicit cancellation/cart mutation.
  - Tests must show a failed checkout retains the cart and retries reuse the ID.
- [x] Reconcile restored cart items with the latest catalog.
  - Flag removed/unavailable/repriced lines and block checkout until the user accepts the updated quote.
- [x] Write and test the legacy account/order ownership migration plan.
  - Use Firebase Auth reset/enrollment; do not import passwords.
  - Define a reviewed UID mapping and staging rollback procedure.

## P1 — complete the Compose product

- [x] Migrate the staff app into a Compose module that consumes `:designsystem`.
  - Implement role-enforced catalog editing, availability, order queue, explicit state transitions, and audit events.
- [x] Add Room-backed offline catalog/cart and WorkManager refresh.
  - Show freshness and recoverable sync state; support browsing and cart editing in airplane mode.
  - Never queue order placement or a payment silently.
- [x] Finish adaptive customer layouts.
  - Add list/detail behavior for expanded windows and verify foldable, landscape, split-screen, compact 320dp and tablet layouts.
- [x] Complete design-system coverage.
  - Add remaining components used by customer/staff features and document their state matrices.
  - Move remaining user-visible feature strings into resources and add an RTL pseudo-locale review.
  - Add screenshot baselines for light/dark, compact/tablet and 2x font scale.
- [x] Add meaningful Compose journey tests for sign-in, filtering, cart, checkout failure/retry, order history, account switching, back navigation and process restoration.

## P2 — engineering-quality evidence

- [x] Add Macrobenchmark and baseline-profile modules.
  - Record cold startup and menu scroll baselines on a named emulator/device before setting budgets.
- [x] Add privacy-safe observability.
  - Redact delivery/contact data from logs; trace checkout stages and expose actionable failure metrics.
- [x] Add API compatibility and token-governance checks for `:designsystem`.
  - Detect breaking public API changes and direct feature use of palette primitives.
- [x] Add favorites, reorder, and saved addresses after P0 auth/data ownership is complete.
  - Reorder must reprice and show removed dishes before confirmation.

## P3 — developer platform experiment

- [x] Build a read-only design-system assistant over public repository docs/tokens/examples.
  - Return source/version citations, refuse invented component APIs, and evaluate with curated questions.
  - Keep proprietary employer code and tokens out of the corpus.
- [x] Evaluate deterministic menu search before personalized recommendations.
  - Create labeled relevance queries, consent/deletion flows, and a measurable comparison.
  - Never infer dietary or allergen safety from generated text.

## Required verification

```sh
cd android
./gradlew :app:assembleDemoDebug :app:testDemoDebugUnitTest \
  :app:lintDemoDebug :designsystem:lintDebug \
  :designsystem:assembleDebugAndroidTest --console=plain
```

With a device or emulator, also run:

```sh
./gradlew :designsystem:connectedDebugAndroidTest
```

Attach the failing task and relevant stack trace when blocked. Do not weaken lint, remove assertions, or delete tests to obtain a green build.
