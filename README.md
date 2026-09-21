# DriftOpen

**DriftOpen** is a fast-paced, modern arcade space shooter built natively for Android using Jetpack Compose. 

Beyond being a fun retro-inspired shooter, this project is a complete walkthrough of **how to build a production-grade Android game from scratch using Google DeepMind's Antigravity (Advanced Agentic AI Pair Programmer)**. Every feature, from the low-level 60 FPS Compose Canvas rendering loop to complex enemy dive patterns, Greek mythos boss encounters, audio synthesis, and unit tests, was developed iteratively through conversational pair programming.

---

## Architecture at a Glance

Instead of relying on heavy third-party game engines (like Unity or Godot), DriftOpen is built 100% with modern Android idioms:

- **Jetpack Compose Canvas**: The entire rendering engine runs inside a Compose `Canvas`, updating at 60 FPS via `withFrameNanos`.
- **Unidirectional Data Flow (UDF)**: A decoupled `GameEngine` manages an immutable `StateFlow<GameState>`. State changes flow in one direction from input events and ticks to rendering.
- **Pure Vector Path Graphics**: Every ship, alien, and bullet silhouette is drawn with mathematical Compose `Path` curves. No raster bitmaps required — graphics stay sharp on every screen density and fold state.
- **Dual-Tier Audio**: Ultra-low-latency sound effects via Android `SoundPool`, accompanied by background music streaming through `MediaPlayer`.
- **Jetpack DataStore**: High scores and game settings persist across launches without SQL boilerplate.
- **Modular Playtesting**: Dedicated Activities (`CheatActivity`, `HelpActivity`, `CollectionActivity`, `SettingsActivity`) keep developer tools and codex lore isolated from the performance-critical rendering code.

---

## Development Journey: Round by Round

Here is the step-by-step progress of building DriftOpen with Antigravity, including the prompts and architectural milestones across each round.

### Round 1: Project Initialization & The Frame Loop
- **Commit**: `e5f59b0` `[driftopen][INIT]: project initialization;`
- **Prompt**:
  > "Initialize a modern Android arcade shooter using Jetpack Compose. Set up a `GameEngine` using `MutableStateFlow<GameState>`, define entities for `PlayerShip`, `Alien`, `Bullet`, and `Explosion`, and implement a `GameScreen` using Compose `Canvas` linked to display frame updates with `withFrameNanos`."
- **Implementation**:
  Created the base Gradle build with Compose BOM, the core `GameEngine` tick loop, entity data models, and smooth touch-drag ship controls on Canvas.

### Round 2: Enemy AI Diving Attacks & Roadmap
- **Commit**: `1053483` `[driftopen][DEV]: improve the game play; [driftopen][CHORE]: add GEMINI.md to record key features and arch;`
- **Prompt**:
  > "Improve gameplay by making aliens break formation to perform dive attacks at the player ship. Add visual health bars and score increments upon destroying enemies. Also create a GEMINI.md file documenting the architecture, current features, and future roadmap."
- **Implementation**:
  Introduced dive attack state machines (`isAttacking`, `attackPhase`) and added `GEMINI.md` to track feature milestones.

### Round 3: Alien Tiers & Collectible Power-ups
- **Commit**: `9fdead7` `[driftopen][DEV]: improve the game play;`
- **Prompt**:
  > "Add multiple alien enemy types: NORMAL, FAST (quick dodging), and high-health BOSS. Introduce drop mechanics for collectible power-ups when aliens die, including Double Fire, Shield, Speed Boost, Spread Shot, Laser Beam, and Homing Missiles. Support multi-level wave progression."
- **Implementation**:
  Implemented enemy speed/health tiers and a drop system spawning multi-shot weapon trajectories in `GameEngine.shoot()`.

### Round 4: Audio Engine & Dynamic Screen Shake
- **Commit**: `9ba1a99` `[driftopen][DEV]: improve the game audio and visual effect;`
- **Prompt**:
  > "Create a sound manager that plays background music and low-latency sound effects for shooting, hits, explosions, and powerups. Add a dynamic screen shake effect to GameScreen whenever the player takes damage."
- **Implementation**:
  Built `SoundManager.kt` using `SoundPool` for SFX and `MediaPlayer` for BGM, and added Canvas camera shake offsets that decay over time.

### Round 5: Cheats & Persistent High Scores
- **Commit**: `c4e5ea2` `[driftopen][DEV]: improve game play; [driftopen][DEV]: add cheat code;`
- **Prompt**:
  > "Add a CheatManager with hidden gesture/tap combinations for testing (invincibility, instant win, level warp). Implement high score persistence using Jetpack DataStore so scores survive app restarts."
- **Implementation**:
  Integrated Jetpack DataStore in `ScoreManager.kt` and added hidden developer gesture listeners in `CheatManager.kt`.

### Round 6: Invincibility Aura & Pause Support
- **Commit**: `1e0a89d` `[driftopen][DEV]: add new power-up invincibility; [driftopen][DEV]: support game pause;`
- **Prompt**:
  > "Add an Invincibility power-up with a visual protective aura, and implement a pause/resume mechanism in GameEngine so players can pause gameplay seamlessly."
- **Implementation**:
  Added `GamePhase.PAUSED` state handling, pause UI toggles, and glowing protective shields.

### Round 7: Unit Tests & Ship Silhouette Polish
- **Commit**: `29e0fa9` `[driftopen][DEV]: game play improve (ship shapes and performance; [driftopen][DEV]: add unit test;`
- **Prompt**:
  > "Write comprehensive unit tests for GameEngine using kotlinx.coroutines.test. Test initial states, ship boundary constraints, bullet collisions, life deductions, and level transitions. Refine the player ship graphics."
- **Implementation**:
  Added `GameEngineTest.kt` with coroutine test dispatchers to verify collision physics and state transitions.

### Round 8: Procedural Formations & Vector Path Silhouettes
- **Commit**: `cb272a8` `[driftopen][DEV]: more alien and player ship shape; [driftopen][DEV]: adjust game level difficulties;`
- **Prompt**:
  > "Extract custom vector shapes using Compose Path into a dedicated Paths file. Design distinct geometric silhouettes for Normal, Fast, and Boss aliens, as well as multiple player ship hulls (Default, Heavy, Stealth, Retro). Create a FormationGenerator for Grid, Triangle, Diamond, and Circle waves."
- **Implementation**:
  Separated vector rendering into `Paths.kt` and procedural wave shapes into `Formations.kt`.

### Round 9: Architecture Documentation & Balancing
- **Commit**: `a53b5fa` `[driftopen][DEV]: update game policy: life increment, wait time reduce, alien matrix fill; [driftopen][DOC]: add architect doc;`
- **Prompt**:
  > "Rebalance the difficulty curve: adjust life awards to every 10,000 points, shorten attack cooldowns as levels advance, and fill empty slots in alien formations. Write an architectural document in docs/architecture.md explaining the UDF design."
- **Implementation**:
  Authored `docs/architecture.md` detailing the unidirectional data flow architecture and balanced level pacing.

### Round 10: Adaptive Large Screen Support
- **Commit**: `7e0a1be` `[driftopen][DEV]: adaption for large screen;`
- **Prompt**:
  > "Ensure GameScreen adapts to tablets and foldable devices by scaling play areas and HUD elements proportionally based on viewport dimensions."
- **Implementation**:
  Added responsive aspect-ratio boundaries and HUD scaling for foldables, tablets, and widescreen devices.

### Round 11: Dedicated Playtesting UI (CheatActivity)
- **Commits**: `f47847c`, `16dfa26`, `3be2441`, `2191599`, `7500d02`
- **Prompt**:
  > "Move the cheat code UI from the in-game canvas overlay into a clean, dedicated CheatActivity with Material3 toggle switches and sliders. Improve Boss AI behaviors and movement curves."
- **Implementation**:
  Extracted playtesting controls into `CheatActivity.kt` with instant switches for God Mode, infinite ammo, and stage selection.

### Round 12: 60 FPS Performance Optimization
- **Commits**: `0059e0a`, `124fc8b`, `8063560`
- **Prompt**:
  > "Profile and optimize the game engine and canvas renderer: eliminate object allocations in the update() loop, cache Path instances, precalculate starfield parallax offsets, and optimize collision loop traversals."
- **Implementation**:
  Eliminated garbage collection spikes in the draw loop, cached path geometry, and optimized matrix iteration for rock-solid 60 FPS performance.

### Round 13: Mega Bomb & Launcher Branding
- **Commits**: `c18e11a`, `46b4e05`, `e88a032`, `a73507d`, `b5e7d58`
- **Prompt**:
  > "Introduce a Mega Bomb mechanic that awards a screen-clearing bomb every 50,000 points. Add adaptive launcher icons for all mipmap densities."
- **Implementation**:
  Added the screen-clearing Mega Bomb detonation effect and high-resolution adaptive launcher icons (`ic_launcher.xml` and WebP mipmaps).

### Round 14: Interactive Game Guide (HelpActivity)
- **Commits**: `e3cb588`, `71b2693`, `2f38c35`
- **Prompt**:
  > "Create a HelpActivity with an arcade-style layout explaining game controls, touch drag gestures, alien enemy behaviors, and power-up descriptions."
- **Implementation**:
  Built `HelpActivity.kt` providing interactive explanations of controls, alien tactics, and weapon types.

### Round 15: Boss Encounters & Greek Mythos Codex
- **Commits**: `f613c2a`, `a18778f`, `ab8ef90`
- **Prompt**:
  > "Add a gauntlet of Bosses and Superbosses inspired by ancient Greek monsters (Nemean Lion, Hydra, Cerberus). Create a CollectionActivity where players unlock entries in an enemy codex as they encounter them."
- **Implementation**:
  Added `CollectionManager.kt` and `CollectionActivity.kt`, establishing a persistent bestiary with unlocked artwork and lore.

### Round 16: Settings & Audio Controls
- **Commit**: `ad1262d` `[driftopen][DEV]: power-ups appearance update;`
- **Prompt**:
  > "Build a SettingsActivity allowing players to toggle background music and sound effect volumes. Polish power-up drop visuals."
- **Implementation**:
  Added `SettingsActivity.kt` for player sound and music preferences.

### Round 17: Production Audio Looping Fix
- **Commit**: `04b6b64` `[driftopen][FIX]: fix BGM isn't looping issue;`
- **Prompt**:
  > "The background music stops playing after the first cycle instead of looping continuously in MediaPlayer. Fix the audio asset header encoding."
- **Implementation**:
  Replaced audio file encoding with clean PCM loop markers, ensuring seamless non-stop background music during gameplay.

---

## Practical Takeaways for Developing with Antigravity

1. **Establish State Architecture Early**: Designing around an immutable `GameState` and `StateFlow` from Day 1 made subsequent additions (power-ups, boss phases, cheats) simple and bug-free.
2. **Use Vector Code Instead of Raw Bitmaps**: Leveraging Compose `Path` allowed us to design and iterate on dozens of enemy ships and bosses directly in Kotlin code without opening an image editor.
3. **Keep Tests and Builds Green**: Verifying each round with `./gradlew assembleDebug` and JUnit tests prevented regression creep across 30+ iterations.
4. **Decouple Utility Screens**: Separating auxiliary features (`CheatActivity`, `HelpActivity`, `CollectionActivity`) kept the main game loop focused purely on high-performance rendering.

---

## Building and Running

### Prerequisites
- JDK 17 or JDK 21
- Android SDK Platform 34
- Android Studio Ladybug / Koala or newer

### Terminal Build
```bash
# Clone the repository
git clone <repo-url> driftopen
cd driftopen

# Build Debug APK
./gradlew assembleDebug

# Output APK:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)**. See the [LICENSE](LICENSE) file for complete details.
