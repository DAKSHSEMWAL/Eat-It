# Capability roadmap for Daksh Semwal

## Experience basis

Reviewed [your portfolio](https://dakshsemwal.dev/) and its published application content on 2026-09-09. It states more than five years of Android experience: H&M Senior Software Engineer since July 2025; H&M Android engineer from December 2023; ZopSmart SDE2 January 2022–December 2023; Triveous Software Engineer 2 March–December 2021. Focus areas include Fabric design-system engineering, migrations, test coverage, offline-first health software and Fabric Compass work with RAG/MCP/retrieval. This roadmap uses those stated strengths, without inventing an exact tenure or borrowing employer IP.

Build a credible commerce and developer-platform reference implementation. The strongest portfolio story is a complete customer/staff journey with measurable reliability and reusable UI contracts.

## Delivery sequence

| Priority | Capability | Experience connection | Acceptance evidence |
| --- | --- | --- | --- |
| P0 | Authenticated server-priced checkout, quote mismatch handling, durable idempotency and order ownership | Platform architecture | Emulator tests: simultaneous retries create one order; changed prices need confirmation; failed checkout retains cart; cross-user access denied. Required before live launch. |
| P0 | Account migration and scoped local data | UI/toolkit migrations | No plaintext password import; verified enrollment/reset; approved UID/order mapping; process-death and account-switch tests; staging rollback rehearsal. |
| P1 | Governed design-system release pipeline | Fabric | Versioned tokens, API compatibility, state registry, light/dark/RTL/2x screenshot matrix, migration recipes. |
| P1 | Offline menu and resilient cart | Triveous offline-first work | Room source of truth, freshness indicator, WorkManager refresh; airplane-mode browsing; unavailable/repriced dishes reconciled before checkout. No invisible queued charge. |
| P1 | Compose staff workspace using shared components | Multi-surface architecture | Role-enforced catalog editing, availability, kitchen queue, explicit order state machine and audit events. Depends on P0 authorization. |
| P2 | Adaptive list/detail and accessibility | Android platform depth | Tablet/foldable layouts, TalkBack/keyboard journey, font scaling and process restoration. Rail/bottom navigation is the current first step. |
| P2 | Performance and observability | Senior engineering ownership | Macrobenchmarks, baseline profiles, startup/scroll budgets measured on a named device, redacted checkout traces and dashboards. Measure baseline before setting targets. |
| P2 | Favorites, reorder, saved addresses | Commerce experience | UID-scoped data; reorder reprices and flags missing dishes; explicit address confirmation. Depends on auth/catalog reliability. |
| P3 | Grounded design-system assistant | Fabric Compass, RAG/MCP | Read-only search over public tokens/contracts/examples, source/version citations and curated evaluation questions; no invented component APIs. Requires stable registry/docs. |
| P3 | Search quality and recommendations | Product/platform growth | Begin with deterministic filters, labeled relevance evaluation and consent/deletion flows. Never infer allergen safety from generated text. |

## Milestones

1. Foundation: buildable Compose demo, version catalog, shared patterns, tests and component documentation.
2. Transaction integrity: backend/rules/emulator suite, migration rehearsal and staging checkout. Gates live ordering.
3. Multi-surface product: offline catalog, staff queue, accessibility evidence and performance baseline.
4. Developer platform: governed library release, usage/token checks and grounded assistant.

Deliver these as small PRs. Record ADRs for module boundaries, pricing/idempotency and token governance. A portfolio case study should include before/after screenshots, a failure/retry trace, accessibility findings and measured outcomes. Backend, staff migration, payments, production operations and AI tooling remain planned capabilities, not completed features.
