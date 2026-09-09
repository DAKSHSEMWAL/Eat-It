# Eat It design system

`android/designsystem` supplies the independent visual language for the Compose customer app and future staff app. It depends on Compose, not Firebase or navigation controllers. Open Account > Explore design system in a debug build.

## Foundations

| Foundation | Contract |
| --- | --- |
| Color | Forest primary, citrus secondary, clay tertiary, warm surfaces. Consume semantic Material color roles rather than palette primitives. Light/dark schemes have paired foregrounds. |
| Type | All 15 Material roles, 57/64sp display through 11/16sp labels; system sans serif. Preserve system font scaling. |
| Spacing | `EatItTheme.spacing`: 4, 8, 12, 16, 24, 32, 48dp. |
| Shape | Extra-small 4, small 8, medium 16, large 24, extra-large 32dp. |
| Sizing | Touch target 48dp; button minimum 56dp; reading width 720dp; food image 168dp. Text must be allowed to grow. |
| Elevation | None 0, card 1, raised 3, overlay 6dp. |
| Motion | Fast 150, standard 250, emphasis 400ms. Prefer built-in Material interactions; motion must not be essential to understanding state. |
| Layout | Navigation rail at available width >=600dp, bottom bar below. Menu uses an adaptive grid. Reading/form surfaces can use `EatItContent`. |

## Component contracts

| Component | States and ownership |
| --- | --- |
| Button | Primary/secondary/quiet; enabled/disabled/loading. Loading suppresses repeat activation. |
| Text field | Label, error, enabled, multiline, keyboard and transformation. Feature owns validation. |
| Search | Query and conditional clear action. Feature owns filtering. |
| Category chip | Selected/unselected; minimum 48dp target. |
| Quantity selector | 0..maximum (default 99); zero means removal; disabled actions at both bounds. |
| Badge | Neutral/positive/attention/error; text communicates meaning without color. |
| Food card | Image slot, title/category/price, open/add callbacks. Disabled applies to both actions. |
| State views | Empty, error/retry and loading. Errors describe a recoverable next action. |
| Summary | Normal/emphasized rows; currency formatting belongs to domain code. |
| Notice | Semantic tone, heading, message and optional action. |
| Setting | Labeled enabled/disabled switch. |
| Confirmation | Caller supplies copy and confirm/dismiss callbacks. Dismiss never confirms. |
| Order progress | Valid current step, completed/current/upcoming semantics; unknown status uses neutral fallback. |
| Navigation | Destination descriptors and callbacks, independent of navigation implementation. |
| Content | Centered bounded column; caller owns scrolling. |

```kotlin
EatItTheme {
    EatItContent {
        EatItSectionHeading("Delivery", "Where should we bring it?")
        EatItTextField(address, onAddressChange, "Full address")
        EatItButton("Place order", onSubmit, loading = submitting)
    }
}
```

## Accessibility and validation

Shared component labels are Android resources. Feature copy is currently English and needs localization before international release. Icon-only actions need labels; decorative images do not. Headings expose heading semantics. Avoid fixed text heights and truncating essential names/prices. Review TalkBack, keyboard, RTL, 320dp, 2x text, dark mode, landscape and split screen. Gallery previews cover light/dark, large text and tablet; previews do not establish device accessibility.

Run `:designsystem:connectedDebugAndroidTest` for loading/disabled actions, quantity bounds and search clearing. Before declaring a component stable, capture reference screenshots across the review matrix and verify contrast (4.5:1 normal text, 3:1 large text/essential controls). Screenshot baselines and manual device review remain release gates.

## Governance

Prefer additive API changes. Deprecate replacements with `ReplaceWith` and migration examples before major-version removal. Every component change needs a use case, state matrix, accessibility behavior and gallery example. Foundations belong here; commerce rules belong in domain code. This is an original system: do not copy proprietary Fabric tokens or implementation.

Planned platform work: token export, screenshot baselines, API compatibility checks, lint for primitive-token use, and documentation generated from a component registry. These are not yet implemented.
