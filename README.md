# Gallery by TheKami

An Android-first, open-source gallery app. Android application ID: `com.gallery.thekami`.
Website: https://thekami.tech

> Status: Phase 6 (Collections, albums, folders). Search, favourites, People and the rest come in later phases.

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

## Home timeline

- Default view is Camera Roll (media under DCIM, excluding screenshot folders). All Media adds
  screenshots and other apps' media according to the toggles. Photos and Videos toggles apply to
  both views. Grouping by Day, Month or Year. Settings persist with DataStore.
- Classification uses the folder MediaStore reports, not file names. It is a convention-based
  heuristic: a camera app that saves outside DCIM counts as "other apps", and screen recordings
  are not treated as screenshots.
- Hiding something from Home never removes it from the library, Albums, Folders or search.
- Thumbnails: Coil 3 with bounded memory cache (20% of the app's memory class), decoded at cell
  size, requests cancelled when cells scroll away. Video thumbnails use `coil-video`.
- Hidden/private and Trash exclusion is wired through `selectHomeMedia(excludedKeys = ...)`
  and will be fed when those systems exist.

## Viewer

- Opens from a Home grid item and navigates exactly the sequence Home was showing (a snapshot;
  albums, folders and search will pass their own sequences). Swipe between items; a thumbnail
  filmstrip stays in sync with the pager and tapping a thumbnail jumps to that item.
- Photos: pinch zoom, pan, double-tap. Decoded to at most 2560 px on the long side, never full
  resolution. Animated GIF plays in the viewer (grid thumbnails stay still).
- Video: Media3 ExoPlayer with play/pause, seek, progress and duration. Playback depends on the
  device's decoders: 4K plays only where the device, codec and container support it, otherwise a
  clear "can't play" message is shown. The player pauses when the app stops; position survives
  rotation.
- HEIC/HEIF: decoded by the Android platform (Android 9+). On older versions such photos show an
  error message instead of a picture.
- Live Photos: an iPhone Live Photo exported to Android is a still plus a short .MOV. The viewer
  only shows a LIVE control when it finds a same-folder, same-base-name, short QuickTime video
  next to a still. Motion Photos embedded in a JPEG (Google/Samsung) are not supported yet.
- Delete: Android 11+ moves the item to the system Trash after Android's own confirmation.
  Below Android 11 the viewer explains that deleting is unavailable. Restoring from inside
  Gallery arrives with the Trash phase.
- Share sends the original file through the system chooser; nothing is uploaded by the app.
- The viewer is not restored after the app process is killed; it opens closed.

## Collections

- Order: Search, People, Albums, Folders, Moments, Places. People, Moments and Places are shown as
  "Not available yet" and are not clickable until their phases. The bottom Search action is
  hidden while Collections is active because Collections has its own Search entry.
- Albums are Gallery's own logical collections stored with Jetpack DataStore. An album stores only
  media keys (MediaStore content URIs). Creating, renaming, deleting an album or removing items
  never moves, copies or deletes a file. An item can be in many albums and appears once per album.
  If MediaStore later reports a different URI for a file (for example after restoring a phone or
  a full media re-index), that album entry shows as "can't be found" instead of silently vanishing.
- Folders are the folders MediaStore reports. Camera and screenshot folders are listed first;
  every other folder is reachable once through "Other folders". Gallery cannot browse arbitrary
  storage locations under Android scoped storage; folders without indexed media, or hidden from
  media apps with a .nomedia file, do not appear. The same relative path on two storage volumes
  is shown as one folder.
- Add to album: from the viewer ("Add to album") or from an album ("Add photos", multi-select).
  Long-press in an album to select items; remove them or set a cover.
