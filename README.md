# Gallery by TheKami

An Android-first, open-source gallery app. Android application ID: `com.gallery.thekami`.
Website: https://thekami.tech

> Status: Phase 3 (local media data layer). Home shows library status only; the timeline comes next.

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

## Design system

- Brand: lime `#C1FF72` and near-black `#100A09`. Lime is the accent in both Dark and Light
  themes (active navigation, Search action, selected controls). Default theme is Dark.
- Font: Ubuntu Mono is bundled and is the default; Android System Default is selectable via
  `FontChoice` (UI for it arrives with Settings). There is no silent fallback to another font.
  Android still draws glyphs Ubuntu Mono lacks (for example Bengali or CJK) with system fonts.
- Ubuntu Mono is Copyright 2011 Canonical Ltd., under the Ubuntu Font Licence 1.0.
  See `third_party/ubuntu-mono/`.
- Preferences (`ThemeMode`, `FontChoice`, `NavBarStyle`) live in `core:domain` and are
  persisted by `SettingsRepository` (`core:data`, Jetpack DataStore).

## Media data layer

- Source: Android MediaStore (images and videos from every volume MediaStore reports, so
  DCIM, Pictures, Download, Movies and other apps' media are found without a folder list).
- Read-only: the app only queries metadata. It never modifies, moves, uploads or deletes media,
  and indexing never decodes pixels.
- Permissions: Android 12 and below `READ_EXTERNAL_STORAGE`; 13 `READ_MEDIA_IMAGES` and
  `READ_MEDIA_VIDEO`; 14+ also `READ_MEDIA_VISUAL_USER_SELECTED` (partial access). Denied,
  partial and revoked states are handled; nothing else is requested.
- Refresh: a debounced MediaStore observer, an explicit Refresh, and a cheap check on resume.
  On Android 11+ the MediaStore generation counter skips reloads when nothing changed.
- Logic with unit tests (`core:domain`): sorting, filtering, duplicate prevention, date grouping,
  permission policy.

Known limits: GPS is only read where MediaStore still exposes it (Android 9 and below);
reading EXIF location on Android 10+ needs another permission and is left to the Places phase.
The full list is held in memory as metadata only (no pixels), without paging.
