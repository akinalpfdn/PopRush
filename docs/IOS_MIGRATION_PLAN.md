# PopRush iOS Migration Plan
# Kotlin/Compose → Swift/SwiftUI

**Son Guncelleme:** 2026-03-11
**Kaynak Platform:** Android (Kotlin, Jetpack Compose, Hilt, Room, Nearby Connections)
**Hedef Platform:** iOS 17+ (Swift 5.9+, SwiftUI, Swift Data, Multipeer Connectivity)

---

## Icindekiler

1. [Genel Bakis](#1-genel-bakis)
2. [Mimari Esitleme Tablosu](#2-mimari-esitleme-tablosu)
3. [Phase Plani](#3-phase-plani)
4. [Phase 1 — Proje Iskeleti ve Tema](#phase-1--proje-iskeleti-ve-tema)
5. [Phase 2 — Domain Katmani](#phase-2--domain-katmani)
6. [Phase 3 — Data Katmani](#phase-3--data-katmani)
7. [Phase 4 — Single-Player Oyun Mantigi](#phase-4--single-player-oyun-mantigi)
8. [Phase 5 — Single-Player UI](#phase-5--single-player-ui)
9. [Phase 6 — Ses Sistemi](#phase-6--ses-sistemi)
10. [Phase 7 — Coop Multiplayer](#phase-7--coop-multiplayer)
11. [Phase 8 — Coop UI](#phase-8--coop-ui)
12. [Phase 9 — Polish ve Test](#phase-9--polish-ve-test)
13. [Dosya Esitleme Referansi](#dosya-esitleme-referansi)
14. [iOS-Native Avantajlar](#ios-native-avantajlar)
15. [Bilinen Riskler](#bilinen-riskler)

---

## 1. Genel Bakis

PopRush, hexagonal grid uzerinde bubble popping mekanigi kullanan bir mobil oyun. Tek oyunculu (Classic, Speed) ve coop multiplayer (5 mod) destekliyor. Migration'in amaci:

- **UI birebir ayni gorunum** (renk paleti, font, layout, animasyonlar)
- **iOS-native performans** (Metal rendering, Core Haptics, Game Center)
- **Ayni mimari kaliplar** (MVI → MVVM+Intent, Strategy Pattern, Repository Pattern)

### Mimari Karar: MVVM+Intent (MVI Benzeri)

Android'deki MVI pattern, iOS'ta `@Observable` ViewModel + sealed enum Action olarak karsilanacak. SwiftUI'nin `@State`/`@Binding` sistemi zaten tek yonlu veri akisini destekler.

```
Android MVI                    iOS MVVM+Intent
─────────────                  ────────────────
GameIntent (sealed interface)  → GameAction (enum)
GameState (data class)         → GameState (struct)
GameViewModel (StateFlow)      → GameViewModel (@Observable)
Composable (collector)         → SwiftUI View (observed)
```

---

## 2. Mimari Esitleme Tablosu

| Android (Kotlin)               | iOS (Swift)                        | Notlar |
|--------------------------------|------------------------------------|--------|
| Jetpack Compose                | SwiftUI                            | Declarative UI, ayni paradigma |
| Hilt (@Inject, @Module)        | Swift DI Container / Environment   | Lightweight DI, factory pattern |
| Room Database                  | SwiftData / Core Data              | SwiftData tercih (iOS 17+) |
| DataStore Preferences          | UserDefaults + @AppStorage         | Direkt karsilik |
| Kotlin Coroutines              | Swift Concurrency (async/await)    | Structured concurrency |
| StateFlow / Flow               | @Published / AsyncSequence         | Reactive streams |
| Kotlin sealed interface        | Swift enum with associated values  | Pattern matching |
| data class                     | struct (value type)                | Copy-on-write semantics |
| Nearby Connections API         | MultipeerConnectivity Framework    | P2P (BT + WiFi) |
| SoundPool                      | AVAudioEngine / AVAudioPlayer      | Daha zengin API |
| Vibrator/VibrationEffect       | Core Haptics (CHHapticEngine)      | Daha hassas kontrol |
| Canvas (Compose)               | Canvas (SwiftUI) / Shape           | Ayni isim, ayni konsept |
| AnimateFloatAsState            | .animation() / withAnimation       | Spring animasyonlar |
| LaunchedEffect                 | .task / .onAppear                  | Side effect management |
| rememberScrollState            | ScrollView (native)                | Native scroll |
| Material3 Icons                | SF Symbols                         | Zengin ikon seti |
| NunitoFontFamily               | Custom font bundle                 | Ayni font dosyalari kullanilacak |
| Timber (logging)               | os.Logger / print                  | Native logging |
| Gson                           | Codable (native)                   | Dependency yok |
| JUnit + MockK                  | XCTest + Swift Testing             | Native test framework |

---

## 3. Phase Plani

```
Phase 1: Proje Iskeleti + Tema          (~1 session)
Phase 2: Domain Katmani                  (~1 session)
Phase 3: Data Katmani                    (~1 session)
Phase 4: Single-Player Oyun Mantigi     (~2 session)
Phase 5: Single-Player UI               (~2-3 session)
Phase 6: Ses Sistemi                     (~1 session)
Phase 7: Coop Multiplayer               (~2-3 session)
Phase 8: Coop UI                         (~2-3 session)
Phase 9: Polish ve Test                  (~1-2 session)
                                    ─────────────────
                                    Toplam: ~12-17 session
```

---

## Phase 1 — Proje Iskeleti ve Tema

### Hedef
Xcode projesi, dosya yapisi, tema sistemi ve font entegrasyonu.

### Dosya Yapisi

```
PopRush/
├── PopRushApp.swift                     ← @main entry point
├── Info.plist
│
├── Core/
│   ├── DI/
│   │   └── DependencyContainer.swift    ← Lightweight DI
│   ├── Domain/
│   │   ├── Model/                       ← Domain modeller
│   │   ├── Repository/                  ← Protocol'ler (interface)
│   │   └── Util/
│   │       └── GameClock.swift          ← Testable clock
│   ├── Data/
│   │   ├── Local/                       ← SwiftData models
│   │   └── Repository/                  ← Protocol implementasyonlari
│   └── UI/
│       ├── Component/                   ← Shared UI components
│       └── Theme/
│           └── PastelColors.swift
│
├── Theme/
│   ├── AppColors.swift                  ← Tum renkler
│   ├── AppTypography.swift              ← Font tanimlari
│   └── AppSpacing.swift                 ← Spacing constants
│
├── Game/
│   ├── Domain/
│   │   └── UseCase/                     ← Is mantigi
│   ├── Presentation/
│   │   ├── GameViewModel.swift          ← Ana ViewModel
│   │   ├── Strategy/                    ← Game mode strategies
│   │   ├── Processor/                   ← Intent processors
│   │   ├── Component/                   ← Game UI parcalari
│   │   └── Screen/                      ← Game ekranlari
│
├── Coop/
│   ├── Domain/
│   │   ├── Model/                       ← Coop domain modeller
│   │   └── UseCase/
│   ├── Data/
│   │   ├── MultipeerManager.swift       ← P2P iletisim
│   │   └── Model/
│   │       └── CoopMessage.swift
│   └── Presentation/
│       ├── Component/
│       ├── Screen/
│       └── Permission/
│
├── Audio/
│   ├── AudioManager.swift               ← AVAudioEngine wrapper
│   └── HapticManager.swift              ← Core Haptics wrapper
│
├── Resources/
│   ├── Fonts/
│   │   ├── Nunito-Regular.ttf
│   │   ├── Nunito-Medium.ttf
│   │   ├── Nunito-SemiBold.ttf
│   │   ├── Nunito-Bold.ttf
│   │   └── Nunito-ExtraBold.ttf
│   └── Sounds/
│       ├── bubble_pop.wav
│       └── ...
│
└── Tests/
    └── PopRushTests/
```

### Gorevler

- [ ] Xcode projesi olustur (iOS 17+, SwiftUI App lifecycle)
- [ ] Dosya yapisini kur
- [ ] Nunito font dosyalarini ekle, Info.plist'e kaydet
- [ ] `AppColors.swift` — Android `AppColors.kt`'nin birebir kopyasi

```swift
// AppColors.swift
import SwiftUI

enum AppColors {
    // Stone palette
    static let darkGray = Color(hex: 0xFF1C1917)
    static let stoneGray = Color(hex: 0xFF44403C)
    static let stoneMedium = Color(hex: 0xFF57534E)
    static let stoneLight = Color(hex: 0xFF78716C)
    static let stonePale = Color(hex: 0xFFA8A29E)
    static let lightGray = Color(hex: 0xFFF5F5F4)
    static let softWhite = Color(hex: 0xFFFAFAFA)

    enum Bubble {
        static let coral = Color(hex: 0xFFFF6B6B)
        static let coralPressed = Color(hex: 0xFFEE5A5A)
        static let skyBlue = Color(hex: 0xFF5B9EFF)
        static let skyBluePressed = Color(hex: 0xFF4A8BE8)
        static let mint = Color(hex: 0xFF5AD8A6)
        static let mintPressed = Color(hex: 0xFF45C795)
        static let lemon = Color(hex: 0xFFFFE66D)
        static let grape = Color(hex: 0xFFA66CFF)
        static let grapePressed = Color(hex: 0xFF8B4FE8)
        static let indigo = Color(hex: 0xFF6366F1)
        static let amber = Color(hex: 0xFFF59E0B)
        // ... tum renkler Android'den birebir
    }

    enum Text { ... }
    enum Background { ... }
    enum Button { ... }
    enum Score { ... }
    enum GameAnimation { ... }
}

extension Color {
    init(hex: UInt, alpha: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0,
            opacity: alpha
        )
    }
}
```

- [ ] `AppTypography.swift` — Nunito font tanimlari
- [ ] `PastelColors.swift` — BubbleColor → renk mapping

### Kabul Kriterleri
- Proje build ediyor
- Tum Android renkleri iOS'ta tanimli
- Nunito fontu calisir durumda

---

## Phase 2 — Domain Katmani

### Hedef
Tum domain modelleri, enum'lar, protocol'ler (repository interface'leri) ve util'ler.

### Dosya Esitlemeleri

| Android                           | iOS                                |
|------------------------------------|------------------------------------|
| `GameState.kt` (data class)       | `GameState.swift` (struct)         |
| `Bubble.kt` (data class)          | `Bubble.swift` (struct)            |
| `BubbleColor.kt` (enum)           | `BubbleColor.swift` (enum)         |
| `BubbleShape.kt` (enum)           | `BubbleShape.swift` (enum)         |
| `GameMode.kt` (enum)              | `GameMode.swift` (enum)            |
| `GameMod.kt` (enum)               | `GameMod.swift` (enum)             |
| `GameDifficulty.kt` (enum)        | `GameDifficulty.swift` (enum)      |
| `SoundType.kt` (enum)             | `SoundType.swift` (enum)           |
| `SpeedModeState.kt` (data class)  | `SpeedModeState.swift` (struct)    |
| `GameIntent.kt` (sealed interface)| `GameAction.swift` (enum)          |
| `GameplayIntent.kt`               | `GameplayAction.swift`             |
| `NavigationIntent.kt`             | `NavigationAction.swift`           |
| `SettingsIntent.kt`               | `SettingsAction.swift`             |
| `CoopIntent.kt`                   | `CoopAction.swift`                 |
| `AudioIntent.kt`                  | `AudioAction.swift`                |
| `GameRepository.kt` (interface)   | `GameRepository.swift` (protocol)  |
| `SettingsRepository.kt`           | `SettingsRepository.swift`         |
| `AudioRepository.kt`              | `AudioRepository.swift`            |
| `MatchHistoryRepository.kt`       | `MatchHistoryRepository.swift`     |
| `PlayerProfileRepository.kt`      | `PlayerProfileRepository.swift`    |

### Ornek Donusum

```kotlin
// Android — GameIntent.kt
sealed interface GameIntent
sealed interface GameplayIntent : GameIntent {
    data object StartGame : GameplayIntent
    data object TogglePause : GameplayIntent
    data class BubblePress(val bubbleId: Int) : GameplayIntent
    data object TimerTick : GameplayIntent
}
```

```swift
// iOS — GameAction.swift
enum GameAction {
    case gameplay(GameplayAction)
    case navigation(NavigationAction)
    case settings(SettingsAction)
    case coop(CoopAction)
    case audio(AudioAction)
}

enum GameplayAction {
    case startGame
    case togglePause
    case bubblePress(bubbleId: Int)
    case timerTick
}
```

```kotlin
// Android — GameState.kt
data class GameState(
    val isPlaying: Boolean = false,
    val score: Int = 0,
    val bubbles: List<Bubble> = emptyList(),
    ...
)
```

```swift
// iOS — GameState.swift
struct GameState {
    var isPlaying: Bool = false
    var score: Int = 0
    var bubbles: [Bubble] = []
    ...
}
```

### Gorevler

- [ ] Tum enum'lari migrate et (birebir degerler)
- [ ] Tum struct'lari olustur (computed property'ler dahil)
- [ ] Tum protocol'leri tanimla
- [ ] `GameAction` enum hiyerarsisini kur
- [ ] `GameClock` protocol + `SystemClock` implementasyonu

### Kabul Kriterleri
- Tum domain tipler compile ediyor
- Android'deki her model/enum iOS'ta karsiligi var
- Computed property'ler ayni mantikta

---

## Phase 3 — Data Katmani

### Hedef
Veri kaliciligi: SwiftData, UserDefaults, repository implementasyonlari.

### Teknoloji Secimi

| Android          | iOS              | Neden |
|------------------|------------------|-------|
| Room Database    | SwiftData        | iOS 17+ native, declarative, minimal boilerplate |
| DataStore Prefs  | @AppStorage      | SwiftUI ile seamless entegrasyon |
| Gson (JSON)      | Codable          | Native, dependency yok |

### SwiftData Modelleri

```swift
// HighScoreRecord.swift
@Model
final class HighScoreRecord {
    var score: Int
    var levelsCompleted: Int
    var totalBubblesPressed: Int
    var accuracyPercentage: Float
    var difficulty: String
    var gameDurationMs: Int64
    var timestamp: Date
    var performanceRating: String

    init(...) { ... }
}

// MatchResult.swift
@Model
final class MatchResult {
    var localPlayerId: String
    var localPlayerName: String
    var opponentPlayerId: String
    var opponentPlayerName: String
    var localScore: Int
    var opponentScore: Int
    var coopMod: String
    var winnerId: String?
    var timestamp: Date

    init(...) { ... }
}
```

### UserDefaults / @AppStorage

```swift
// GamePreferences.swift
final class GamePreferences: ObservableObject {
    @AppStorage("bubbleShape") var bubbleShape: String = "CIRCLE"
    @AppStorage("soundEnabled") var soundEnabled: Bool = true
    @AppStorage("musicEnabled") var musicEnabled: Bool = true
    @AppStorage("soundVolume") var soundVolume: Double = 1.0
    @AppStorage("musicVolume") var musicVolume: Double = 0.7
    @AppStorage("zoomLevel") var zoomLevel: Double = 1.0
    @AppStorage("playerName") var playerName: String = "Player"
    @AppStorage("playerColor") var playerColor: String = "ROSE"
}
```

### Gorevler

- [ ] SwiftData `@Model`'leri olustur (HighScoreRecord, MatchResult)
- [ ] `GamePreferences` olustur (@AppStorage)
- [ ] `GameRepositoryImpl` — SwiftData ile high score CRUD
- [ ] `SettingsRepositoryImpl` — UserDefaults wrapper
- [ ] `MatchHistoryRepositoryImpl` — SwiftData ile match CRUD
- [ ] `PlayerProfileRepositoryImpl` — UserDefaults'tan isim/renk
- [ ] ModelContainer setup (SwiftData container, `@main` app'te)

### Kabul Kriterleri
- High score kayit/okuma calisiyor
- Match result kayit/okuma calisiyor
- Settings persist ediyor

---

## Phase 4 — Single-Player Oyun Mantigi

### Hedef
Use case'ler, strategy pattern, ViewModel.

### Strategy Pattern

```swift
// GameModeStrategy.swift
protocol GameModeStrategy {
    var config: GameModeConfig { get }
    func initialize(viewModel: GameViewModel) async
    func handleBubblePress(bubbleId: Int, state: GameState) -> GameState
    func handleTimerTick(state: GameState) -> GameState
    func cleanup()
}

// ClassicModeStrategy.swift
final class ClassicModeStrategy: GameModeStrategy { ... }
// SpeedModeStrategy.swift
final class SpeedModeStrategy: GameModeStrategy { ... }
```

### ViewModel

```swift
// GameViewModel.swift
@Observable
@MainActor
final class GameViewModel {
    private(set) var state = GameState()
    private var activeStrategy: GameModeStrategy?

    private let gameRepository: GameRepository
    private let settingsRepository: SettingsRepository
    private let audioManager: AudioManager

    init(
        gameRepository: GameRepository,
        settingsRepository: SettingsRepository,
        audioManager: AudioManager
    ) { ... }

    func send(_ action: GameAction) {
        switch action {
        case .gameplay(let a): handleGameplay(a)
        case .navigation(let a): handleNavigation(a)
        case .settings(let a): handleSettings(a)
        case .coop(let a): handleCoop(a)
        case .audio(let a): handleAudio(a)
        }
    }
}
```

### Use Case'ler

| Android                        | iOS                          |
|---------------------------------|------------------------------|
| `GenerateLevelUseCase`         | `LevelGenerator`             |
| `HandleBubblePressUseCase`     | `BubblePressHandler`         |
| `InitializeGameUseCase`        | `GameInitializer`            |
| `TimerUseCase`                 | `GameTimer` (Timer.publish)  |
| `SpeedModeUseCase`             | `SpeedModeManager`           |

### Timer Implementasyonu

```swift
// GameTimer.swift — iOS native Timer
final class GameTimer {
    private var timer: Timer?

    func start(interval: TimeInterval = 1.0, onTick: @escaping () -> Void) {
        timer = Timer.scheduledTimer(withTimeInterval: interval, repeats: true) { _ in
            onTick()
        }
    }

    func stop() {
        timer?.invalidate()
        timer = nil
    }
}
```

### Gorevler

- [ ] `LevelGenerator` — 44 bubble hex grid olusturma
- [ ] `BubblePressHandler` — press mantigi (classic + speed)
- [ ] `GameInitializer` — oyun baslangic state
- [ ] `GameTimer` — Timer.publish veya Timer wrapper
- [ ] `SpeedModeManager` — speed mode state yonetimi
- [ ] `ClassicModeStrategy` implementasyonu
- [ ] `SpeedModeStrategy` implementasyonu
- [ ] `GameModeStrategyFactory`
- [ ] `GameViewModel` — MVI intent processing

### Kabul Kriterleri
- Classic mode: bubble press → skor artisi → level tamamlama → timer bitisi → game over
- Speed mode: random bubble activation → fast tap → bonus → game over
- High score kaydediliyor

---

## Phase 5 — Single-Player UI

### Hedef
Tum ekranlar ve componentler, Android ile pixel-perfect gorunum.

### Ekranlar

| Android                  | iOS                     |
|--------------------------|-------------------------|
| `StartScreen`            | `StartView`             |
| `ModeSelectionScreen`    | `ModeSelectionView`     |
| `ModPickerScreen`        | `ModPickerView`         |
| `GameScreen`             | `GameView`              |
| `GameOverScreen`         | `GameOverView`          |

### Kritik UI Componentleri

#### Hexagonal Bubble Grid

```swift
// BubbleGridView.swift
struct BubbleGridView: View {
    let bubbles: [Bubble]
    let selectedShape: BubbleShape
    let zoomLevel: CGFloat
    let onBubblePress: (Int) -> Void
    let enabled: Bool

    private let rowSizes = [5, 6, 7, 8, 7, 6, 5]

    var body: some View {
        GeometryReader { geometry in
            let bubbleSize = calculateBubbleSize(geometry: geometry)
            ZStack {
                ForEach(bubbles, id: \.id) { bubble in
                    BubbleView(
                        bubble: bubble,
                        shape: selectedShape,
                        size: bubbleSize
                    )
                    .position(hexPosition(for: bubble, in: geometry, size: bubbleSize))
                    .onTapGesture {
                        if enabled && bubble.canBePressed {
                            onBubblePress(bubble.id)
                        }
                    }
                }
            }
            .scaleEffect(zoomLevel)
        }
    }

    private func hexPosition(for bubble: Bubble, in geo: GeometryProxy, size: CGFloat) -> CGPoint {
        let rowSizesArr = rowSizes
        let maxRowSize = rowSizesArr.max()!
        let totalWidth = geo.size.width
        let spacing = totalWidth / CGFloat(maxRowSize + 1)
        let rowSize = rowSizesArr[bubble.row]
        let xOffset = (CGFloat(maxRowSize) - CGFloat(rowSize)) / 2.0 * spacing
        let x = xOffset + CGFloat(bubble.col + 1) * spacing
        let y = CGFloat(bubble.row + 1) * spacing * 0.866  // hex vertical spacing
        return CGPoint(x: x, y: y)
    }
}
```

#### Bubble Shape Rendering

```swift
// BubbleView.swift
struct BubbleView: View {
    let bubble: Bubble
    let shape: BubbleShape
    let size: CGFloat

    var body: some View {
        ZStack {
            bubbleShape
                .fill(bubbleGradient)
                .frame(width: size, height: size)

            // Highlight
            bubbleShape
                .fill(
                    RadialGradient(
                        colors: [.white.opacity(0.4), .clear],
                        center: .topLeading,
                        startRadius: 0,
                        endRadius: size * 0.5
                    )
                )
                .frame(width: size, height: size)
        }
        .scaleEffect(bubble.isPressed ? 0.0 : 1.0)
        .animation(.spring(response: 0.3, dampingFraction: 0.6), value: bubble.isPressed)
        .opacity(Double(bubble.effectiveTransparency))
    }

    @ViewBuilder
    private var bubbleShape: some Shape {
        switch shape {
        case .circle: Circle()
        case .square: RoundedRectangle(cornerRadius: size * 0.2)
        case .hexagon: Hexagon()
        case .star: Star(corners: 5, smoothness: 0.45)
        case .heart: Heart()
        case .triangle: Triangle()
        }
    }
}
```

#### Animasyonlar

| Android                              | iOS                                  |
|--------------------------------------|--------------------------------------|
| `animateFloatAsState(spring(...))`   | `.animation(.spring(...))`           |
| `AnimatedVisibility`                 | `.transition()` + `if/switch`        |
| `AnimatedContent`                    | View + `.id()` modifier             |
| `LaunchedEffect(key) { delay() }`   | `.task(id: key) { try await ... }`   |
| `Canvas { drawCircle(...) }`        | `Canvas { context in context.fill }` |
| `graphicsLayer { scaleX = s }`      | `.scaleEffect(s)`                    |
| `Modifier.shadow(...)`              | `.shadow(...)`                       |
| `Modifier.blur(...)`                | `.blur(radius: ...)`                 |

### Gorevler

- [ ] `BubbleView` — 6 sekil destegi (Circle, RoundedRect, Hexagon, Star, Heart, Triangle)
- [ ] `BubbleGridView` — hex layout, zoom, press
- [ ] `StartView` — ana menu
- [ ] `ModeSelectionView` — Single vs Coop secimi
- [ ] `ModPickerView` — Classic/Speed + duration picker
- [ ] `GameView` — aktif oyun ekrani (header + grid + overlay'ler)
- [ ] `GameOverView` — sonuc ekrani
- [ ] `GameHeaderView` — skor, timer, level
- [ ] `PauseButtonView`
- [ ] `DurationPickerView`
- [ ] `SettingsOverlayView`
- [ ] `BackConfirmationDialog` — native alert veya custom
- [ ] `SpeedBonusOverlay`
- [ ] Custom Shape'ler: `Hexagon`, `Star`, `Heart`, `Triangle`

### Kabul Kriterleri
- Tum ekranlar Android ile ayni gorunumde
- Bubble pop animasyonu, scale + fade
- Hex grid layout dogru
- Timer, skor, level gorunuyor
- Zoom calisiyor

---

## Phase 6 — Ses Sistemi

### Hedef
Ses efektleri, muzik, haptic feedback.

### iOS-Native Avantaj: AVAudioEngine + Core Haptics

```swift
// AudioManager.swift
@MainActor
final class AudioManager: ObservableObject {
    private var engine: AVAudioEngine
    private var players: [SoundType: AVAudioPlayerNode] = [:]
    private var musicPlayer: AVAudioPlayer?

    func playSound(_ type: SoundType) { ... }
    func playMusic(_ track: MusicTrack) { ... }
    func stopMusic() { ... }
    func setVolume(sound: Float, music: Float) { ... }
}

// HapticManager.swift
final class HapticManager {
    private var engine: CHHapticEngine?

    func prepare() throws { ... }

    func playBubblePop() {
        let intensity = CHHapticEventParameter(parameterID: .hapticIntensity, value: 0.6)
        let sharpness = CHHapticEventParameter(parameterID: .hapticSharpness, value: 0.8)
        let event = CHHapticEvent(eventType: .hapticTransient, parameters: [intensity, sharpness], relativeTime: 0)
        // play pattern
    }

    func playBombExplosion() {
        // Medium vibration — Android'deki VibrationEffect.createOneShot(150) karsiligi
        let intensity = CHHapticEventParameter(parameterID: .hapticIntensity, value: 0.9)
        let sharpness = CHHapticEventParameter(parameterID: .hapticSharpness, value: 0.3)
        let event = CHHapticEvent(eventType: .hapticContinuous, parameters: [intensity, sharpness], relativeTime: 0, duration: 0.15)
        // play pattern
    }
}
```

### Gorevler

- [ ] `AudioManager` — AVAudioEngine ile ses efektleri
- [ ] `HapticManager` — Core Haptics ile titresim
- [ ] Ses dosyalarini Resources/Sounds'a ekle
- [ ] Bubble pop, level complete, game over, countdown sesleri
- [ ] Muzik loop destegi
- [ ] Audio session yonetimi (AVAudioSession)

### Kabul Kriterleri
- Bubble pop sesi calisiyor
- Haptic feedback calisiyor
- Muzik arka planda caliyor
- Volume kontrol calisiyor

---

## Phase 7 — Coop Multiplayer

### Hedef
P2P multiplayer: host/join, mesajlasma, 5 coop mod mantigi.

### Nearby Connections → MultipeerConnectivity

| Android (Nearby Connections)           | iOS (MultipeerConnectivity)        |
|----------------------------------------|------------------------------------|
| `Nearby.getConnectionsClient()`       | `MCNearbyServiceAdvertiser`        |
| `startAdvertising()`                   | `advertiser.startAdvertisingPeer()` |
| `startDiscovery()`                     | `MCNearbyServiceBrowser`           |
| `requestConnection()`                  | `browser.invitePeer()`             |
| `acceptConnection()`                   | Session delegate `didReceiveInvitation` |
| `sendPayload()`                        | `session.send(data, toPeers:)`     |
| `onPayloadReceived()`                  | `session(_:didReceive:fromPeer:)`  |
| `ConnectionLifecycleCallback`          | `MCSessionDelegate`               |

```swift
// MultipeerManager.swift
final class MultipeerManager: NSObject, ObservableObject {
    private let serviceType = "poprush-coop"  // max 15 chars, lowercase + hyphen
    private let myPeerId: MCPeerID
    private var session: MCSession
    private var advertiser: MCNearbyServiceAdvertiser?
    private var browser: MCNearbyServiceBrowser?

    @Published var connectionState: ConnectionState = .disconnected
    @Published var discoveredPeers: [MCPeerID] = []

    func startHosting(playerName: String) { ... }
    func startDiscovery() { ... }
    func send(_ message: CoopMessage) throws { ... }
    func disconnect() { ... }
}

extension MultipeerManager: MCSessionDelegate {
    func session(_ session: MCSession, peer peerID: MCPeerID, didChange state: MCSessionState) { ... }
    func session(_ session: MCSession, didReceive data: Data, fromPeer peerID: MCPeerID) { ... }
}
```

### Mesaj Formatı

```swift
// CoopMessage.swift
struct CoopMessage: Codable {
    let type: CoopMessageType
    var content: String = ""
    var bubbleId: Int?
    var playerColor: String?
    var playerName: String?
    var localScore: Int?
    var remoteScore: Int?
    var gameDuration: Int64?
    var coopMod: String?
    var bombBubbleIds: [Int]?
    var claimedBubbleIds: [Int]?
    var timestamp: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
}

enum CoopMessageType: String, Codable {
    case chat, bubbleClaim = "bubble_claim", gameSetup = "game_setup"
    case gameStart = "game_start", gameEnd = "game_end"
    case scoreUpdate = "score_update", colorSelection = "color_selection"
    case readyState = "ready_state", heartbeat, playerProfile = "player_profile"
    case turnEnd = "turn_end"
}
```

### Coop Game Manager'lar

| Android                      | iOS                        |
|------------------------------|----------------------------|
| `CoopConnectionManager`     | `CoopConnectionManager`    |
| `CoopGameManager`           | `CoopGameManager`          |
| `CoopMessageHandler`        | `CoopMessageHandler`       |
| `CoopStateManager`          | `CoopStateManager`         |
| `CoopHandler` (facade)      | `CoopHandler` (facade)     |

### Onemli: Flood Fill (Chain Reaction)

```swift
// CoopStateManager.swift
func floodFill(bubbles: [CoopBubble], startBubbleId: Int, playerId: String) -> [Int] {
    guard let startBubble = bubbles.first(where: { $0.id == startBubbleId }),
          startBubble.owner == nil else { return [] }

    var claimed = [startBubbleId]
    for neighbor in getHexNeighbors(row: startBubble.row, col: startBubble.col, bubbles: bubbles) {
        if neighbor.owner == nil {
            claimed.append(neighbor.id)
        }
    }
    return claimed
}
```

### Gorevler

- [ ] `MultipeerManager` — MCSession, Advertiser, Browser
- [ ] `CoopMessage` — Codable model (Android Gson formatina uyumlu)
- [ ] `CoopConnectionManager` — baglanti lifecycle
- [ ] `CoopGameManager` — oyun mantigi (5 mod)
- [ ] `CoopMessageHandler` — gelen mesaj isleme
- [ ] `CoopStateManager` — state yonetimi, flood fill, bomb placement
- [ ] `CoopHandler` — facade
- [ ] `CoopUseCase` — is mantigi
- [ ] Turn-based logic (Chain Reaction)
- [ ] Bomb logic (Hot Potato)
- [ ] Blind mode logic

### Kabul Kriterleri
- Host telefonunda advertising basliyor
- Client kesfedip baglaniyor
- Mesajlar iki yonlu iletiliyor
- 5 coop mod mantigi dogru calisiyor
- Disconnect/reconnect duzenli

---

## Phase 8 — Coop UI

### Hedef
Tum coop ekranlari ve componentleri.

### Ekranlar

| Android                          | iOS                             |
|----------------------------------|---------------------------------|
| `CoopSetupScreen`               | `CoopSetupView`                 |
| `CoopConnectionScreen`          | `CoopConnectionView`            |
| `CoopPlayerSetupScreen`         | `CoopPlayerSetupView`           |
| `CoopModSelectionScreen`        | `CoopModSelectionView`          |
| `CoopGameplayScreen`            | `CoopGameplayView`              |
| `CoopStatsDialog`               | `CoopStatsSheet`                |
| `CoopConnectionOverlay`         | `CoopConnectionOverlay`         |
| `CoopColorPicker`               | `CoopColorPicker`               |
| `CoopPermissionsDialog`         | Gereksiz (iOS otomatik sorar)   |

### iOS-Specific: Sheet vs Dialog

Android'deki `Dialog(onDismissRequest)` → iOS'ta `.sheet(isPresented:)` veya `.fullScreenCover`. Stats dialog icin `.sheet` daha dogal:

```swift
.sheet(isPresented: $showStatsDialog) {
    CoopStatsSheet(
        localPlayerId: coopState.localPlayerId,
        localPlayerName: coopState.localPlayerName,
        opponentPlayerId: coopState.opponentPlayerId,
        opponentPlayerName: coopState.opponentPlayerName,
        repository: matchHistoryRepository
    )
}
```

### Permission Farki

Android'de Bluetooth/WiFi/Location izinleri manuel isteniyor. iOS'ta `MultipeerConnectivity` kullanildiginda:
- **Local Network izni** otomatik sorulur (Info.plist'te `NSLocalNetworkUsageDescription`)
- **Bluetooth izni** gerekli degildir (MultipeerConnectivity bunu soyutlar)
- `CoopPermissionsDialog` iOS'ta **gereksiz** — Info.plist aciklamasi yeterli

### Gorevler

- [ ] `CoopSetupView` — host/join secimi
- [ ] `CoopConnectionView` — peer listesi, baglanti durumu
- [ ] `CoopPlayerSetupView` — isim/renk secimi
- [ ] `CoopModSelectionView` — 5 mod kart listesi, scrollable
- [ ] `CoopGameplayView` — bubble grid + HUD + timer + turn indicator
- [ ] `CoopStatsSheet` — TabView ile Stats + History
- [ ] `CoopColorPicker` — renk secimi
- [ ] `CoopConnectionOverlay` — baglanti durumu overlay
- [ ] `BombPenaltyText` — floating "-3" animasyonu
- [ ] `CompactGameHUD` — skor, timer, turn indicator
- [ ] Info.plist: `NSLocalNetworkUsageDescription` ekle

### Kabul Kriterleri
- Tum coop ekranlari Android ile ayni gorunumde
- Stats dialog dogru veri gosteriyor
- Turn indicator calisiyor (Chain Reaction)
- Bomb feedback (haptic + animasyon) calisiyor

---

## Phase 9 — Polish ve Test

### Hedef
Edge case'ler, test suite, performans optimizasyonu.

### Test Plani

| Test Tipi        | Framework        | Kapsam |
|------------------|------------------|--------|
| Unit Test        | XCTest + Swift Testing | Use case'ler, ViewModel, Strategy |
| UI Test          | XCUITest         | Temel kullanici akislari |
| Snapshot Test    | swift-snapshot-testing | Kritik ekranlar |

### Test Dosyalari

```
Tests/PopRushTests/
├── Domain/
│   ├── LevelGeneratorTests.swift
│   ├── BubblePressHandlerTests.swift
│   └── GameTimerTests.swift
├── Strategy/
│   ├── ClassicModeStrategyTests.swift
│   └── SpeedModeStrategyTests.swift
├── ViewModel/
│   └── GameViewModelTests.swift
├── Coop/
│   ├── CoopStateManagerTests.swift     ← flood fill, hex neighbors
│   └── CoopGameManagerTests.swift
└── Helpers/
    └── TestClock.swift
```

### Performans Optimizasyonlari

- [ ] `Canvas` yerine `TimelineView` ile 60fps animasyonlar
- [ ] `LazyVStack` scroll performansi
- [ ] `.drawingGroup()` ile off-screen rendering (bubble grid)
- [ ] `@Observable` makrosu ile minimal view invalidation (iOS 17+)
- [ ] `MCSession` reliable/unreliable mode secimi (skor → reliable, heartbeat → unreliable)

### Gorevler

- [ ] Unit testler (en az %60 coverage)
- [ ] Edge case: iki oyuncunun ayni bubble'a ayni anda basması
- [ ] Edge case: baglanti kopması mid-game
- [ ] Edge case: arka plana gecis
- [ ] Memory leak kontrolu (Instruments)
- [ ] Accessibility: VoiceOver support
- [ ] Dark mode kontrolu (renkler dark mode'da nasil gorunuyor?)
- [ ] iPad layout destegi (size class adaptasyon)

### Kabul Kriterleri
- Tum testler geciyor
- Memory leak yok
- 60fps bubble animasyonlari
- VoiceOver temel destek

---

## Dosya Esitleme Referansi

### Toplam Dosya Sayisi Tahmini

| Katman               | Android Dosya | iOS Dosya (tahmin) |
|----------------------|---------------|---------------------|
| Domain Model         | ~25           | ~20 (bazilari merge) |
| Data / Repository    | ~12           | ~10                  |
| Use Case             | ~7            | ~6                   |
| ViewModel / Strategy | ~15           | ~12                  |
| UI Screen            | ~11           | ~11                  |
| UI Component         | ~15           | ~13                  |
| Coop Logic           | ~15           | ~13                  |
| Audio                | ~1            | ~2 (Audio + Haptic)  |
| DI                   | ~4            | ~1 (DI Container)    |
| Theme                | ~3            | ~3                   |
| Test                 | ~6            | ~8                   |
| **Toplam**           | **~114**      | **~99**              |

---

## iOS-Native Avantajlar

Bu migration'da kullanilabilecek iOS-native ozellikler:

### 1. Core Haptics
Android'deki basit `VibrationEffect.createOneShot(150)` yerine, iOS'ta hassas haptic pattern'lar tasarlanabilir. Bubble pop icin hafif "tap", bomb icin agir "thud" gibi.

### 2. Metal Rendering
SwiftUI'nin `Canvas` ve `.drawingGroup()` Metal uzerine render eder. Android'in `Canvas`'indan daha performansli olabilir buyuk grid'lerde.

### 3. Game Center
Opsiyonel: Leaderboard ve achievement sistemi eklenebilir. Android'de Google Play Games yerine.

### 4. SharePlay
Opsiyonel: `MultipeerConnectivity` yerine veya ek olarak, FaceTime uzerinden coop oyun destegi.

### 5. Widget / Live Activity
Opsiyonel: Aktif coop oyunu sirasinda Dynamic Island'da skor gosterimi.

### 6. SF Symbols
Material Icons yerine SF Symbols — daha zengin, daha tutarli, system-native.

### 7. App Intents
Siri ile "PopRush ac" veya "Skorumu goster" gibi komutlar.

---

## Bilinen Riskler

| Risk | Etki | Azaltma |
|------|------|---------|
| MultipeerConnectivity Android cihazlarla uyumsuz | Coop sadece iOS-iOS arasi calışır | Cross-platform icin WebSocket backend gerekir (gelecekte) |
| SwiftData iOS 17+ gerektiriyor | iOS 16 ve alti desteklenmez | Min deployment target iOS 17 |
| Nunito font lisansi | Font ticari kullanima uygun olmayabilir | OFL lisansini dogrula (Google Fonts = OFL, sorun yok) |
| Hex grid layout farki | Pixel-perfect eslesme zor olabilir | Referans screenshot'lar ile karsilastirma |
| Coop mesaj formati uyumsuzlugu | Android ve iOS cihazlar birbirleriyle oynayamaz | JSON format ayni tutulabilir ama transport layer farkli |

---

## Sonraki Adimlar

1. Bu dokumani incele, sorularin varsa sor
2. Onayladiginda Phase 1'den baslayabiliriz
3. Her phase sonunda review + onay
4. Ayri bir iOS repo olusturulacak

---

*Bu dokuman, PopRush Android projesinin (Kotlin/Compose) iOS'a (Swift/SwiftUI) migration'i icin kapsamli bir rehberdir. Her phase bagimsiz olarak uygulanabilir ve test edilebilir sekilde tasarlanmistir.*
