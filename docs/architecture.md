# DriftOpen - Architecture & Implementation Document

## 1. Overview
The **DriftOpen** game is an Android application built purely using **Jetpack Compose** for rendering and **Kotlin Coroutines / StateFlow** for state management and the game loop. 

It implements a modern take on the classic arcade shooter, featuring various alien formations, power-ups, screen shake effects, bosses, and multiple weapon types. The game follows a unidirectional data flow (UDF) architecture.


## 2. Architecture

The codebase is organized into logical domains, cleanly separating the state, the game logic, and the UI rendering.

### 2.1. State Management (`models/Models.kt`)
The game relies on an immutable state tree represented by pure Kotlin data classes.

- **`GameState`**: The root of the state tree. It holds the player ship, aliens, bullets, particles (stars, explosions), current level, score, and the current `GamePhase` (START, PLAYING, PAUSED, GAME_OVER).
- **`Entities`**: `PlayerShip`, `Alien`, `Bullet`, `Star`, `Explosion`, `PowerUp`.
- **`Enums`**: Drive behaviors and visuals, such as `AlienType` (NORMAL, FAST, BOSS, SUPERBOSS) and `PowerUpType` (SHIELD, LASER_BEAM, HOMING_MISSILES, etc.).

### 2.2. Game Engine (`GameEngine.kt`)
The `GameEngine` acts as the ViewModel / Controller. It encapsulates a `MutableStateFlow<GameState>` and exposes intents (e.g., `moveShipRelative`, `shoot`, `startGame`).

**Game Loop**: The `update(dtNanos: Long)` function is the heart of the engine. It is called on every frame. It computes the *next* state by applying velocity, checking bounds, updating AI behavior, and resolving collisions, and then atomically updates the `MutableStateFlow`.

### 2.3. Formations & AI (`formations/Formations.kt`)
The `FormationGenerator` object is a factory for enemy waves.

- Generates dense, filled geometric formations based on the level (Grid, Triangle, Diamond, Filled Circle, Filled Heart, Scatter).
- Configures alien types, health, and grid coordinates.

**AI Logic** (handled in `GameEngine`): Aliens move back and forth as a group. Periodically, an alien breaks formation to dive at the player (managed by `isAttacking` and `attackPhase`). To maintain game balance, attackers are throttled by a global cooldown (which decreases every 5 levels) and a per-alien 1-second stay-in-formation requirement.

### 2.4. Presentation Layer (`ui/GameScreen.kt`)
The UI is a declarative Jetpack Compose function.

- **State Observation**: Collects the `GameState` via `collectAsState()`.
- **Render Loop**: Uses `LaunchedEffect` with `withFrameNanos` to trigger `engine.update()`, tying the game logic directly to the display refresh rate.
- **Input Handling**: Uses `pointerInput` with `detectDragGestures` for relative, 1:1 ship movement, and `detectTapGestures` for shooting.
- **Rendering**: A single Compose `Canvas` handles rendering the background, stars, entities, and UI overlays using low-level drawing commands (`drawPath`, `drawRect`, `drawCircle`).

## 3. Implementation Details

### 3.1. The Game Loop
```kotlin
LaunchedEffect(Unit) {
    while (true) {
        withFrameNanos { frameTimeNanos ->
            engine.update(frameTimeNanos)
        }
    }
}
```
This loop ensures the game logic advances precisely when a frame is ready to be drawn by Compose, providing smooth animations.

### 3.2. Collision Detection
Collision detection uses basic Axis-Aligned Bounding Box (AABB) checks. Every entity (Ship, Alien, Bullet) exposes a `getRect()` method returning a Compose `Rect`. Collisions are resolved in the `update()` loop using `Rect.overlaps()`.

### 3.3. Relative Touch Controls
To prevent the user's finger from blocking the view of the player ship, the control scheme uses relative drag gestures. 

- The `GameEngine.moveShipRelative` adds the `dragAmount` to the ship's current position.
- Visually, the ship is rendered at `y - 120f` relative to the actual hitbox `y` coordinate. 

### 3.4. Weaponry and Power-Ups
- When an alien is destroyed, there is a chance to spawn a `PowerUp` entity that falls down the screen.
- Upon collision with the player, the `GameEngine` sets the `activePowerUp` in the state.
- The player is awarded an extra life for every 5000 points earned.
- The `shoot()` function uses a `when` expression on the `activePowerUp` type to spawn different bullet combinations (e.g., Spread Shot spawns 3 bullets with different `vx`, Laser creates a high-pierce, elongated bullet).

### 3.5. Visual Polish
* **Screen Shake**: When the player takes damage, `screenShakeIntensity` is set. The `Canvas` translates the entire draw context by a randomized value based on this intensity.
* **Particles**: Explosions are data classes that track their own `life`, `alpha`, and `radius`. They fade out dynamically over time.
* **Starfield**: A parallax effect is created by continuously moving `Star` entities downwards and resetting their Y coordinate when they exit the screen.


## 4. Future Extensibility
Because the game uses strict Unidirectional Data Flow, extending the game is straightforward:

1. **New Enemy Types**: Add to `AlienType`, define behavior in `update()`, and map a visual representation in `GameScreen`.
2. **New Weapons**: Add to `PowerUpType`, and modify the `shoot()` function in `GameEngine` to spawn the desired `Bullet` trajectory.
3. **Sound**: The architecture already includes a `SoundManager` hook that is called sequentially when game events occur (e.g., `soundManager?.playSound("shoot")`).
