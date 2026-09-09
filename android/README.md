# Eat It Compose

Open this directory in a recent Android Studio compatible with AGP 9.4. The archived Java/XML apps are separate projects.

## Toolchain

- Gradle 9.7.1, checksum-verified wrapper.
- AGP 9.4.0 with built-in Kotlin 2.2.10; Compose compiler uses the same Kotlin version.
- Kotlin DSL (`build.gradle.kts`, `settings.gradle.kts`), centralized dependencies/plugins in `gradle/libs.versions.toml`.
- JDK 17+, SDK 35, Build Tools 36.0.0, minimum Android 24.

Versions checked on 2026-09-09 against [Gradle's release metadata](https://services.gradle.org/versions/current) and [AGP release notes](https://developer.android.com/build/releases/gradle-plugin). Library versions remain pinned from the initial checkpoint; they are not claimed to be latest. AGP owns Kotlin compilation, so do not reapply `org.jetbrains.kotlin.android` or restore `android.kotlinOptions`.

## Run and verify

Set `JAVA_HOME` to a compatible JDK and `ANDROID_HOME` to the Android SDK (or provide ignored `local.properties`). Select the `demoDebug` variant and run `app`.

```sh
./gradlew :app:assembleDemoDebug :app:testDemoDebugUnitTest :app:lintDemoDebug :designsystem:lintDebug
./gradlew :designsystem:assembleDebugAndroidTest
# With an emulator/device:
./gradlew :designsystem:connectedDebugAndroidTest
```

On Windows use `gradlew.bat`. CI builds the demo, tests domain logic, runs lint and compiles component interaction tests. Device tests require a connected emulator and are not part of the hosted build job.

The demo includes menu filtering, dish details, cart, local order placement/history, account screens and a debug component gallery. It does not place real deliveries. Demo images are illustrative Material icons.

## Live migration status

`live` retains the original application ID and uses Firebase Auth, legacy Category/Food reads and an unfinished `placeOrder` callable integration. Do not ship it as a working commerce backend: server pricing/quote validation, idempotency, security rules, ownership migration and deployment remain required. Do not reuse the legacy plaintext-password sign-in. Configure Firebase only in an isolated test project while finishing that migration.

See [design-system contracts](../docs/DESIGN_SYSTEM.md) and [experience-based roadmap](../docs/CAPABILITY_ROADMAP.md).
