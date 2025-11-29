# PopRush Coop Mode Implementation Plan

## Executive Summary

This plan outlines the implementation of cooperative multiplayer gameplay for PopRush using the Nearby Connections API. The implementation follows the existing MVI architecture and maintains clean separation of concerns while building on the current modular codebase.

## Game Mode Requirements

**Core Gameplay:**
- Time-limited gameplay (countdown timer from existing duration selection)
- Players can click cells to color them with their color
- Default color is black/empty for unclaimed cells
- Players can take opponents' cells or empty cells
- When time is up, the player with the most cells wins
- Offline coop using Nearby Connections API

**Enhanced Features (User Preferences):**
- Configurable player colors from existing pastel palette
- Use existing duration picker (10-60 seconds) with 30-second default
- Display both player names during gameplay
- Add particle effects for bubble claiming animations

**Technical Requirements:**
- Follow existing game theme and design (pastel colors, hexagonal grid)
- Use modular, clean code architecture
- Maintain existing MVI patterns
- Design for future extensibility (more coop modes)
- Follow feedback loop approach (approval after each phase)

## Phase-Based Implementation Strategy

### Phase 1: Foundation & Data Models
**Goal:** Establish the data structures and network foundation

**Key Components:**
- Create `CoopGameState.kt` with domain models for multiplayer state
- Extend existing `GameState.kt` with coop-specific properties
- Add coop intents to `GameIntent.kt`
- Set up Nearby Connections API integration
- Configure dependencies and permissions
- Implement color picker for player customization

**Critical Files:**
- `coop/domain/model/CoopGameState.kt` (NEW)
- `core/domain/model/GameState.kt` (MODIFY)
- `core/domain/model/GameIntent.kt` (MODIFY)
- `build.gradle.kts` (MODIFY)
- `AndroidManifest.xml` (MODIFY)
- `coop/presentation/component/CoopColorPicker.kt` (NEW)

### Phase 2: Network Layer & Core Logic
**Goal:** Implement connection management and game logic

**Key Components:**
- `NearbyConnectionsManager` interface and implementation
- `CoopUseCase` for business logic
- Connection flow (advertising/discovery/connecting)
- Message passing system for real-time sync
- Score calculation and bubble ownership logic
- Player name exchange system

**Critical Files:**
- `coop/nearby/NearbyConnectionsManager.kt` (NEW)
- `coop/domain/usecase/CoopUseCase.kt` (NEW)
- `di/NetworkModule.kt` (NEW)
- `di/UseCaseModule.kt` (MODIFY)

### Phase 3: UI Components & Screens
**Goal:** Create user interface for coop gameplay

**Key Components:**
- `CoopConnectionScreen` for hosting/joining games with name/color selection
- `CoopGameScreen` for gameplay with player name displays
- `CoopBubbleGrid` component extending existing hexagonal layout
- `ParticleEffectsComponent` for bubble claiming animations
- Score displays and player indicators
- Integration with existing `GameScreen` navigation

**Critical Files:**
- `coop/presentation/screen/CoopConnectionScreen.kt` (NEW)
- `coop/presentation/screen/CoopGameScreen.kt` (NEW)
- `coop/presentation/component/CoopBubbleGrid.kt` (NEW)
- `coop/presentation/component/ParticleEffectsComponent.kt` (NEW)
- `game/presentation/screen/GameScreen.kt` (MODIFY)

### Phase 4: GameViewModel Integration
**Goal:** Integrate coop logic into existing state management

**Key Components:**
- Extend `GameViewModel` with coop intent handlers
- Implement coop-specific game flow
- Handle connection state changes
- Manage bubble claiming and score updates
- Integrate with existing timer system (duration picker)
- Handle player color selection and validation

**Critical Files:**
- `game/presentation/GameViewModel.kt` (MODIFY)

## Technical Architecture

### Data Models
```kotlin
data class CoopGameState(
    val isHost: Boolean = false,
    val localPlayerId: String = "",
    val localPlayerName: String = "",
    val opponentPlayerId: String = "",
    val opponentPlayerName: String = "",
    val isConnectionEstablished: Boolean = false,
    val connectionPhase: CoopConnectionPhase = CoopConnectionPhase.DISCONNECTED,
    val localScore: Int = 0,
    val opponentScore: Int = 0,
    val localPlayerColor: BubbleColor = BubbleColor.ROSE, // Configurable
    val opponentPlayerColor: BubbleColor = BubbleColor.SKY, // Configurable
    val bubbles: List<CoopBubble> = emptyList(),
    val gamePhase: CoopGamePhase = CoopGamePhase.WAITING
)

data class CoopBubble(
    val id: Int,
    val position: Int,
    val row: Int,
    val col: Int,
    val owner: String? = null, // Player ID or null for unclaimed
    val isTransitioning: Boolean = false, // For particle effects
    val transitionStartTime: Long = 0L
)
```

### Network Integration
- **API:** Google Nearby Connections API for offline P2P communication
- **Permissions:** Bluetooth, WiFi, Location access
- **Flow:** Advertising → Discovery → Connection → Message Exchange
- **Messages:** BubbleClaimed, ScoreSync, GameFinished, Heartbeat, PlayerInfo

### State Management Integration
- **MVI Pattern:** All coop state changes through `processIntent()`
- **Immutable State:** Follow existing `StateFlow<GameState>` pattern
- **Reactive Updates:** Maintain existing reactive architecture
- **Clean Separation:** Coop logic in dedicated use case and components

## Enhanced Features Implementation

### Configurable Player Colors
```kotlin
@Composable
fun CoopColorPicker(
    availableColors: List<BubbleColor>,
    selectedColor: BubbleColor,
    opponentColor: BubbleColor?,
    onColorSelected: (BubbleColor) -> Unit,
    modifier: Modifier = Modifier
)
```
- **Color Options:** Use existing pastel palette (Rose, Sky, Emerald, Amber, Violet)
- **Validation:** Ensure both players select different colors
- **Preview:** Real-time color preview during selection
- **Defaults:** Rose for Player 1, Sky for Player 2

### Duration Integration
- **Reuse Existing Picker:** Leverage classic mode's 10-60 second selection
- **Default Duration:** 30 seconds for balanced coop gameplay
- **Persistence:** Remember player's preferred duration

### Player Name System
- **Name Entry:** Players enter names during connection setup
- **Display:** Show both player names during gameplay
- **Validation:** Character limits and content filtering
- **Privacy:** Allow default names if preferred

### Particle Effects System
```kotlin
@Composable
fun CoopBubbleWithEffects(
    bubble: CoopBubble,
    playerColors: Map<String, BubbleColor>,
    onBubbleClick: (Int) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
fun ParticleEffect(
    isActive: Boolean,
    color: Color,
    modifier: Modifier = Modifier
)
```
- **Claim Animation:** Small particle burst when claiming bubbles
- **Ownership Transfer:** Special effects when stealing opponent's bubbles
- **Performance:** Optimized to maintain 60fps
- **Configurable:** Allow players to adjust effect intensity

## Implementation Dependencies

### Android Dependencies
```kotlin
dependencies {
    implementation("com.google.android.gms:play-services-nearby:18.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}
```

### Required Permissions
```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

## Key Files Summary

### Files to Modify:
1. `GameState.kt` - Add coop state properties
2. `GameIntent.kt` - Add coop intents
3. `GameMode.kt` - Remove coming soon for COOP
4. `GameViewModel.kt` - Add coop logic handlers
5. `GameScreen.kt` - Add coop screen navigation
6. `UseCaseModule.kt` - Add coop use case provider
7. `build.gradle.kts` - Add dependencies
8. `AndroidManifest.xml` - Add permissions

### Files to Create:
1. `CoopGameState.kt` - Coop domain models
2. `NearbyConnectionsManager.kt` - Network layer
3. `CoopUseCase.kt` - Coop business logic
4. `CoopConnectionScreen.kt` - Connection UI
5. `CoopGameScreen.kt` - Game UI
6. `CoopBubbleGrid.kt` - Coop bubble component
7. `ParticleEffectsComponent.kt` - Visual effects
8. `CoopColorPicker.kt` - Color selection
9. `NetworkModule.kt` - Network dependencies

## Testing Strategy

### Unit Tests
- CoopUseCase logic for bubble claiming and scoring
- Connection state management
- Message serialization/deserialization
- Color picker validation

### Integration Tests
- Nearby Connections API integration
- End-to-end game flow
- State synchronization between devices
- Timer integration with coop mode

### UI Tests
- Connection screen interactions
- Color picker functionality
- Game screen responsiveness
- Particle effects performance

## Performance Considerations

### Network Optimization
- **Heartbeat Mechanism:** Regular connection health checks
- **Message Batching:** Combine multiple updates in single messages
- **Connection Recovery:** Automatic reconnection on temporary disconnections
- **Bandwidth Usage:** Minimal data transmission for smooth gameplay

### UI Performance
- **Particle Effects:** Limit to maintain 60fps on all devices
- **State Updates:** Optimize StateFlow emissions during gameplay
- **Memory Management:** Proper cleanup of animations and observers
- **Smooth Transitions:** Maintain existing animation quality

## Error Handling & Edge Cases

### Connection Issues
- **Discovery Failures:** Clear error messages and retry options
- **Connection Drops:** Graceful handling with recovery attempts
- **Permission Denials:** User guidance for enabling required permissions
- **Incompatible Devices:** Fallback messaging for unsupported hardware

### Game State Issues
- **Synchronization Conflicts:** Conflict resolution for simultaneous moves
- **Invalid States:** Recovery from corrupted game states
- **Timeout Handling:** Proper cleanup on abandoned games
- **Score Discrepancies:** Validation and correction mechanisms

## Future Extensibility

The modular architecture supports future coop enhancements:
- **Additional Modes:** Team play, cooperative puzzles, special rules
- **Network Expansion:** Internet multiplayer, lobby system, matchmaking
- **Spectator Mode:** Allow observers to watch games
- **Tournaments:** Bracket systems and leaderboards
- **Cross-Platform:** Compatibility with other platforms

## Success Criteria

1. **Functional:** Players can host/join games and play complete coop matches
2. **Stable:** Reliable connection handling and real-time synchronization
3. **Performant:** Smooth 60fps gameplay with particle effects
4. **Usable:** Intuitive UI with color customization and name display
5. **Maintainable:** Clean code architecture following project standards
6. **Extensible:** Foundation for future multiplayer features

## Implementation Timeline

### Phase 1: Foundation (Week 1)
- Create coop domain models
- Set up Nearby Connections API
- Implement basic connection flow
- Add color picker component
- Dependency injection setup

### Phase 2: Core Logic (Week 2)
- Implement CoopUseCase
- Add coop intents and state management
- Create connection screen with name/color selection
- Extend GameViewModel
- Implement message passing system

### Phase 3: UI/UX (Week 3)
- Complete connection screen with all features
- Implement game screen with player name displays
- Add particle effects component
- Create coop bubble grid with animations
- Handle game over and winner display

### Phase 4: Polish & Testing (Week 4)
- Error handling and edge cases
- Performance optimization
- Unit and integration tests
- Final UI polish and animations
- Documentation and cleanup

## Development Guidelines

### Code Standards
- Follow existing MVI architecture patterns
- Use dependency injection consistently
- Maintain immutable state management
- Write comprehensive unit tests
- Document all public APIs

### Design Consistency
- Use existing pastel color theme
- Maintain hexagonal bubble grid layout
- Follow established typography and spacing
- Preserve existing animation patterns
- Ensure responsive design for different screen sizes

### Quality Assurance
- Code reviews for all changes
- Automated testing pipeline
- Performance profiling on target devices
- User experience testing
- Security and privacy considerations

This implementation plan ensures coop mode integrates seamlessly with existing code while providing the enhanced features you requested. The modular approach allows for incremental development and testing at each phase.