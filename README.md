# Gallery by TheKami

An Android-first, open-source gallery app. Android application ID: `com.gallery.thekami`.
Website: https://thekami.tech

> Status: Phase 1 (project foundation). No gallery features yet.

## Architecture

Kotlin, Jetpack Compose, Material 3. Modules:

| Module | Purpose |
| --- | --- |
| `app` | Application entry point, wiring |
| `core:ui` | Theme and shared Compose UI |
| `core:domain` | Pure Kotlin models and logic (no Android) |
| `core:data` | Media access and storage (Android) |

Dependencies point inward: `app` -> `core:*`, `core:data` -> `core:domain`.

## Build and test

Builds run on GitHub Actions (see `.github/workflows/ci.yml`), which runs unit tests,
lint and a debug build, and uploads the debug APK as an artifact. Install that APK on a
device to test. With Gradle and the Android SDK available locally:
gradle test lintDebug assembleDebug
## Contributing

- Keep changes small and focused; one concern per pull request.
- CI (tests + lint) must pass.
- Never commit keystores, passwords or other secrets.

## License

GPL-3.0, see `LICENSE`.
