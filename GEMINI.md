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

### Visuals & Audio
- [ ] **Sound Effects**: Add classic arcade sounds for shooting, explosions, and diving.
- [ ] **Music**: Background chiptune music.
- [ ] **Enhanced Graphics**: Use vector graphics or sprite sheets instead of simple shapes.
- [ ] **Screen Shake**: Add subtle screen shake on player hit.

### Technical Tasks
- [ ] **Performance Profiling**: Optimize `Canvas` rendering for high alien counts.
- [ ] **Save State**: Persist high scores using DataStore or Room.
- [ ] **Unit Tests**: Add tests for collision logic and state transitions in `GameEngine`.

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **State Management**: StateFlow
- **Graphics**: Compose Canvas
