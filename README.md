# Eat It

Eat It is being modernized as a Kotlin and Jetpack Compose food-ordering app with a reusable Material 3 design system.

The active project is [`android/`](android/README.md). It contains:

- `app`: customer menu, search, dish details, cart, checkout, orders and account flows.
- `designsystem`: semantic foundations, reusable components, adaptive patterns and a debug gallery.
- `demoDebug`: a local, safe-to-explore experience that does not place real orders.
- `live`: a Firebase migration shell whose secure checkout backend and account migration are still required.

The original Java/XML apps remain in `Client Side/EatIt2` and `ServerSide/EatItServer` as migration references.

Read the [design-system contract](docs/DESIGN_SYSTEM.md), [capability roadmap](docs/CAPABILITY_ROADMAP.md), and [Gemini task list](docs/GEMINI_TASKS.md).
