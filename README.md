# thoughts

> An offline-first, minimal monochrome screenwriting studio for Android & Desktop.

[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Desktop-black?style=flat-square)](https://github.com/Rizirfan/Thought)
[![Kotlin](https://img.shields.io/badge/Kotlin-Multiplatform-black?style=flat-square&logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Compose%20Multiplatform-black?style=flat-square)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-Apache%202.0-black?style=flat-square)](LICENSE)

---

## Downloads & Releases

Get the standalone binary for your platform from [GitHub Releases](https://github.com/Rizirfan/Thought/releases/latest):

| Platform | Package | Description |
|:---|:---|:---|
| **Android** | [**`thoughts-android.apk`**](https://github.com/Rizirfan/Thought/releases/latest/download/thoughts-android.apk) | Standalone APK for phones & tablets (API 24+) • [Local mirror](apk/thoughts.apk) |
| **Desktop (Windows)** | [**`thoughts-windows-x64.zip`**](https://github.com/Rizirfan/Thought/releases/latest/download/thoughts-windows-x64.zip) | Portable 64-bit Windows app (bundled with embedded runtime, no Java needed) |
| **Desktop (Linux)** | [**`thoughts-linux-x64.tar.gz`**](https://github.com/Rizirfan/Thought/releases/latest/download/thoughts-linux-x64.tar.gz) / [**`.deb`**](https://github.com/Rizirfan/Thought/releases/latest/download/thoughts-linux-amd64.deb) | Tarball and Debian package for Linux x64 |

---

## Overview

**thoughts** is a distraction-free, privacy-first screenwriting environment crafted for screenwriters, playwrights, and storytellers. Built from the ground up with Kotlin Multiplatform and Compose Multiplatform, it brings standard theatrical script formatting into a clean, physical-inspired digital workspace.

No cloud lock-in. No subscription popups. No distracting colors or cluttered toolbars. Just your ideas, structured typography, and ink on paper.

---

## Key Features

### 1. Natural 3D Script Dossiers
- **Borderless Physical Folder Design**: Screenplay projects are housed in custom-rendered, natural 3D script folders reminiscent of physical production binders.
- **Tactile Long-Press Context**: Long press any folder to reveal a minimal context action menu (Open, Rename, Duplicate, Delete) accompanied by device haptic feedback.
- **Zero Screen Clutter**: No unnecessary borders or card outlines—folders sit naturally on the studio floor.

### 2. Standard Fountain Screenplay Editor
- **Theatrical Industry Standards**: Strict adherence to industry formatting guidelines with standard 12pt Courier typography, precise dialogue indents, character cues, and scene slugs.
- **Element Block System**: Dedicated elements for Scene Headings (`INT.`/`EXT.`), Action, Character, Parenthetical, Dialogue, Transition, and Shot.
- **Fluid Keyboard Navigation**: 
  - `Enter` intuitively advances to the expected next element (e.g., Character &rarr; Dialogue &rarr; Action).
  - `Tab` / `Shift+Tab` cycles element types in place.
  - Mobile accessory bar provides one-tap type switching directly above the virtual keyboard.

### 3. Pure Minimal Monochrome Design
- **Paper & Ink Philosophy**: The entire application is rendered in a dedicated, high-contrast light monochrome theme designed to reduce eye strain and mirror the physical scriptwriting experience.
- **Restraint Over Noise**: Subtle hairline dividers, elegant typographic weight variations, and clean negative space keep the focus entirely on the words.

### 4. Seamless Export Engine
- **PDF Export**: Single-tap generation of industry-standard formatted PDFs complete with title page, proper margins, and page breaks.
- **Plain Text & Fountain**: Export clean `.fountain` / `.txt` files compatible with Final Draft, Highland 2, Fade In, and Scrivener.
- **Native File System**: Uses Android Storage Access Framework (SAF) and native desktop file pickers—your files save exactly where you choose.

### 5. 100% Offline & Private
- **Zero Network Permissions**: The app does not request Android internet permissions.
- **No Analytics or Trackers**: Your intellectual property never leaves your local hardware.
- **Local Persistence**: Scripts and editor preferences are stored strictly in private app storage.

---

## Architecture & Tech Stack

- **Language**: Kotlin 2.0+
- **UI Toolkit**: Compose Multiplatform (Material 3, Adaptive Layouts)
- **Target Platforms**:
  - **Android**: API 24+ (Android 7.0 through Android 15+)
  - **Desktop**: Windows, macOS, Linux (JVM)
- **Architecture**: MVI / Unidirectional Data Flow with unified `AppState` and repository abstractions.
- **Parser & Formatter**: Native Fountain engine with AST-based block conversion and PDF layout calculation.

---

## Getting Started

### Prerequisites
- **JDK**: OpenJDK 17 or higher
- **Android SDK**: Build tools 34+, Platform API 34+ (for Android builds)

### Prebuilt Android APK
A ready-to-install debug APK is included in this repository:
- **Location**: [`apk/thoughts.apk`](apk/thoughts.apk)
- **Direct install via ADB**:
  ```bash
  adb install -r apk/thoughts.apk
  ```

### Building from Source

#### Android Debug Build
To build the debug APK locally:
```bash
# On macOS / Linux:
./gradlew :composeApp:assembleDebug

# On Windows PowerShell:
.\gradlew :composeApp:assembleDebug
```
The output APK will be generated at:
`composeApp/build/outputs/apk/debug/composeApp-debug.apk`

#### Run on Desktop (JVM)
To run the desktop application directly:
```bash
# On macOS / Linux:
./gradlew :composeApp:run

# On Windows PowerShell:
.\gradlew :composeApp:run
```

#### Package Desktop Distribution
To package a standalone executable for your current OS (MSI/EXE on Windows, DMG/PKG on macOS, DEB/RPM on Linux):
```bash
./gradlew :composeApp:packageReleaseDistributionForCurrentOS
```

---

## Project Structure

```text
thoughts/
├── apk/
│   └── thoughts.apk             # Pre-built distributable Android APK
├── composeApp/
│   ├── src/
│   │   ├── androidMain/         # Android specific manifest, storage, and platform code
│   │   ├── commonMain/          # Multiplatform core
│   │   │   ├── kotlin/com/thoughts/app/
│   │   │   │   ├── data/        # Repositories, local storage, settings
│   │   │   │   ├── domain/      # Fountain parser, PDF renderer, script rules
│   │   │   │   └── ui/          # Compose screens (Home, Editor, Export, Settings)
│   │   │   └── resources/       # Fonts, vectors, icons
│   │   └── desktopMain/         # Desktop JVM entry point and native dialogs
│   └── build.gradle.kts
├── gradle/                      # Gradle wrapper and version catalogs
├── build.gradle.kts
└── settings.gradle.kts
```

---

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
