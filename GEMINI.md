# DriftOpen Android Project

This is a modern Android implementation of the classic arcade game "DriftOpen" built using Jetpack Compose.

## Project Structure

- **`com.dailystudio.vibecoding.driftopen.game`**: Contains the core game logic.
    - `GameEngine.kt`: Manages game state, entity movement, collisions, and phase transitions.
- **`com.dailystudio.vibecoding.driftopen.game.models`**: Data models for game entities.
    - `Models.kt`: Defines `GameState`, `PlayerShip`, `Alien`, `Bullet`, `Explosion`, etc.
- **`com.dailystudio.vibecoding.driftopen.ui`**: UI components.
    - `GameScreen.kt`: The main game screen using Compose `Canvas` for performance-optimized rendering.
- **`com.dailystudio.vibecoding.driftopen`**: Main entry point.
    - `MainActivity.kt`: Sets up the game engine and screen.

## Key Features

- **Fluid Movement**: Ship controlled via drag gestures.
- **Formation Logic**: Aliens move in a synchronized formation with occasional diving attacks.
- **Dynamic Explosions**: Particle-based visual feedback for hits.
- **Parallax Background**: Moving stars to create a sense of depth.
- **Game Lifecycle**: Start, Playing, Win, and Game Over phases.

## Development Roadmap & Feature Ideas

### Gameplay Improvements
- [x] **Alien Types**: Implement different behaviors for `FAST` and `BOSS` alien types.
- [x] **Power-ups**: Add collectible power-ups (e.g., double fire, shield, speed boost).
- [x] **Levels**: Introduce multiple levels with increasing difficulty and different alien formations.
- [x] **Attack Patterns**: More complex diving maneuvers for attacking aliens.
- [x] **Difficulty Balancing**: Gradual introduction of alien types (Normal on Lvl 1, Fast on Lvl 2, Boss on Lvl 3) and smoother scaling of attack rates.
- [x] **Superboss Encounters**: A massive, high-health `SUPERBOSS` enemy that appears every 5 levels, featuring unique movement and a "shotgun" blast attack.
- [x] **Advanced Player Weapons**: Introduce powerful new weapon types (Spread Shot, Laser Beam, Homing Missiles, Explosive Bombs).

### Visuals & Audio
- [x] **Sound Effects**: Implemented `SoundManager` architecture with `SoundPool` triggers. (Requires assets in `res/raw`).
- [x] **Music**: Implemented `MediaPlayer` background logic in `SoundManager`. (Requires assets in `res/raw`).
- [x] **Enhanced Graphics**: Use vector graphics or sprite sheets instead of simple shapes.
- [x] **Distinct Alien Shapes**: Replace simple rectangles with unique Compose `Path` shapes for each alien type (e.g., Chevron for Normal, Diamond for Fast, Hexagon for Boss).
- [x] **Screen Shake**: Implemented dynamic screen shake on player hit.

### Technical Tasks
- [x] **Performance Profiling**: Optimize `Canvas` rendering for high alien counts.
- [x] **Save State**: Persist high scores using Jetpack DataStore.
- [x] **Unit Tests**: Add tests for collision logic and state transitions in `GameEngine`.

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **State Management**: StateFlow
- **Graphics**: Compose Canvas
