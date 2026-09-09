# Eat-It Compose modernization — work-in-progress handoff

## User request
Modernize https://github.com/DAKSHSEMWAL/Eat-It using Jetpack Compose and create an entire design system. User then requested moving the work to Codex. Continue this implementation, validate it, and open a draft PR; do not treat this checkpoint as complete.

## Current checkpoint
- New standalone Android project in `android/` with `app` and reusable `designsystem` modules. Original customer and staff apps remain untouched.
- AGP 8.9.2, Kotlin/Compose compiler 2.1.20, Gradle 8.11.1, Compose BOM 2025.04.01, SDK 35, Java 17, min SDK 24. Versions are pinned, not claimed to be latest.
- Design direction: forest green, citrus accents, warm neutral surfaces. Light/dark semantic color schemes, all typography roles, spacing, sizing, shapes, elevation and motion primitives.
- Components: buttons (primary/secondary/quiet, loading/disabled), fields, search, chips, quantity selector, badges, food cards, headers, top bar, summaries, loading/error/empty states. Debug gallery includes light/dark/large-text Compose previews.
- Customer Compose screen drafts: menu/category/search, details, cart, checkout, orders, account, email sign-in/signup/reset and order confirmation. Navigation, lifecycle collection, ViewModel, demo repository, Firebase repository, local cart persistence.
- Demo flavor uses sample data and locally persisted orders. Live flavor retains original application ID, uses Firebase Auth and reads legacy `Category`/`Food` nodes with case-tolerant field parsing.
- Live checkout calls a `placeOrder` callable function that DOES NOT EXIST YET. No backend implementation, rules, tests or CI have been written. No Firebase config copied into new project.

## Validation status
NOT compiled, tested or visually verified. Android SDK was not found in this environment. A connectivity probe to Gradle distribution service was attempted; no build was run. All source files are initial drafts requiring review and fixes.

## Required next work
1. Read repository instructions. Review and compile `android/` using Java 17 and Android SDK 35: `./gradlew :app:assembleDemoDebug :app:testDemoDebugUnitTest :app:lintDemoDebug`. Fix all compiler/lint failures.
2. Finish and validate the design system; extract hardcoded component copy into resources, verify accessibility, TalkBack, dark mode, font scale 2x, compact/tablet layouts and process recreation. Add app icons, meaningful preview coverage and screenshot evidence. Demo image placeholders are Material icons, not food photography.
3. Implement authenticated server-authoritative `placeOrder` callable, validation, price checks, idempotency across retries and ownership. Current client sends item IDs/quantities, delivery data and requestId. Include expected prices or a quote-confirmation contract so changed prices cannot silently be charged. Persist pending idempotency keys across process death. Handle catalog removals/price changes in restored cart.
4. Write Firebase rules and emulator tests. New orders should include userId plus compatible legacy fields (phone/name/address/total/foods/status) and integer totalPaise. Query order history by userId. Do not deploy rules/functions or migrate production data automatically.
5. Legacy auth reads plaintext passwords in `User` and has confusing staff checks. Do not reproduce it. Document a Firebase Auth account migration/reset strategy and how old orders become owned by UIDs. No production compatibility claim until this is implemented.
6. Review ViewModel/repository lifecycle, auth race conditions, cancellation, back stack reset, checkout confirmation and error/retry behavior. Scope order/cart state to user; avoid showing previous user's orders during auth changes.
7. Add meaningful unit tests (cart arithmetic/quantity bounds, price parsing, validation, state transitions, failed checkout retaining cart, duplicate submit/idempotency) and Compose interaction tests. Add CI and actual build verification.
8. Assess staff-app modernization using shared DS (original `ServerSide/EatItServer`); it has not yet been migrated. User requested modernizing the repo, not only a mockup. Document scope and remaining backend/account migration work honestly.
9. Add README and design-system usage/migration documentation and open a draft PR. No merge or production deployment requested.

## Legacy reference
- Customer: `Client Side/EatIt2`, Java/XML, Firebase Realtime Database, SQLite cart, SDK 29/min 17.
- Staff: `ServerSide/EatItServer`, Java/XML catalog management.
- `Food` fields from Java getters: name, image, price, discount, menuid (some stored data may use capitalized keys).
- `Requests`: phone, name, address, total (formatted currency string), foods (productId/productName/quantity/price/discount), status. Status codes 0 placed / 1 on its way / 2 delivered.
- Old checkout trusts device-computed prices and clears cart before write acknowledgement; new implementation should fix this.
- No AGENTS.md was found during initial source discovery.
