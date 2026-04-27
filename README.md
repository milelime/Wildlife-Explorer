# Wildlife Explorer

A desktop app for browsing hiking trails, recording wildlife sightings, photos, and ratings. It uses **Java Swing** for the UI and stores data in JSON files next to the project (no Maven or Gradle).

The source lives on GitHub: [milelime/Wildlife-Explorer](https://github.com/milelime/Wildlife-Explorer).

## Get the code

Install [Git](https://git-scm.com/) if you do not have it, then:

```bash
git clone https://github.com/milelime/Wildlife-Explorer.git
cd Wildlife-Explorer
```

All build and run commands below assume your shell is in this `Wildlife-Explorer` directory (the project root).

## Prerequisites

You need a **JDK** (Java Development Kit) so `javac` and `java` are available—not only a JRE.

Check your install:

```bash
javac -version
java -version
```

If `javac` is missing, install a JDK (for example **Eclipse Temurin**, **Oracle JDK**, or your platform’s package manager).

---

## Important: run from the project root

Always build and launch with your **current working directory** set to this repository’s root (the folder that contains `src/`, `defaults/`, and `Makefile`). The app resolves image paths and `defaults/` relative to that folder (or set `-Dwildlifeexplorer.root=/path/to/Wildlife-Explorer`).

---

## Build and run (Linux, macOS, WSL)

### Option A — Make (if `make` is installed)

```bash
cd /path/to/Wildlife-Explorer
make build          # compile to build/classes
make run            # compile + open the Swing window
make clean          # delete build/classes
```

### Option B — Shell script

```bash
cd /path/to/Wildlife-Explorer
chmod +x build.sh   # once
./build.sh            # compile only
./build.sh run        # compile + run
```

### GUI on Linux/WSL

- **WSL2 on Windows 11** often supports GUI apps via WSLg; the window should appear when you run `make run` or `./build.sh run`.
- If nothing appears, ensure a display is available (`$DISPLAY` or Wayland). On WSL without WSLg, install an **X server** on Windows, then set `export DISPLAY=:0` (or the value your X server docs give) before running.

---

## Build and run (Windows — PowerShell)

Open PowerShell **in the repository root**:

```powershell
cd C:\path\to\Wildlife-Explorer
.\build.ps1           # compile only → build\classes
.\build.ps1 run       # compile + launch the app
.\build.ps1 clean     # remove the build folder
```

If execution policy blocks scripts:

```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```

---

## Run without Make (any OS)

After compiling:

```bash
java -cp build/classes wildlifeexplorer.Main
```

Compile first with `make build`, `./build.sh`, or `.\build.ps1`.

---

## Using the app

1. **Search** — Lists trails; search filters by name. **Refresh** reloads from disk. **New Trail** creates a trail (assigns the next free id).
2. **Open** — Opens the trail detail view: trail photos (right), wildlife blocks with text and thumbnails (left).
3. **Rating** — **Rate** opens a dialog with buttons **0–10** (integers only). The trail stores a running average of submitted ratings.
4. **Trail images** — **Add Trail Image…** picks a file; the app saves a resized JPEG under `images/trails/` and stores a relative path.
5. **Wildlife** — **Add Wildlife…** asks whether to **choose from list** (species already in your catalog) or **create new animal** (opens the form). Optional photo uses the file chooser; previews are saved under `images/wildlife/`.
6. **Remove** — Each wildlife block has **Remove** to detach that species **from this trail only**; the global catalog entry remains for other trails.
7. **Sample data** — On first launch, if `trails.json` and `wildlife.json` are missing, they are copied from `defaults/`. You may be offered a one-time prompt to clear that sample data; tracked files under `defaults/` are never deleted by the app.

---

## Data files (what to back up or zip for a friend)

| Location | Role |
|----------|------|
| `defaults/` | **Tracked in git**: sample trails, wildlife, and placeholder images. |
| `trails.json`, `wildlife.json` | **Your working database** at the repo root (often gitignored). |
| `images/trails/`, `images/wildlife/` | **Your uploaded JPEG previews** (often gitignored). |

To reset to bundled samples only, delete `trails.json` and `wildlife.json` (and optionally `images/`) from the project root, then run the app again.

---

## Development: regenerate default placeholder PNGs

From the repo root:

```bash
javac tools/GenerateDefaultImages.java && java -cp tools GenerateDefaultImages
```

This overwrites PNGs under `defaults/images/`.
