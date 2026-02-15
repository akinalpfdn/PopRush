# PopRush - Project Guidelines

## Architecture & Design Principles

- **SOLID principles** must be followed in every change
  - Single Responsibility: each class/function has one clear purpose
  - Open/Closed: extend behavior through abstractions, not modification
  - Liskov Substitution: subtypes must be substitutable for their base types
  - Interface Segregation: prefer focused interfaces over broad ones
  - Dependency Inversion: depend on abstractions, not concrete implementations

- **Design Patterns**: follow established patterns already in the codebase
  - Strategy Pattern for game modes (ClassicModeStrategy, SpeedModeStrategy)
  - Repository Pattern for data access (GameRepository, AudioRepository)
  - Use Case Pattern for business logic (GenerateLevelUseCase, HandleBubblePressUseCase)
  - MVI (Model-View-Intent) for presentation layer
  - Dependency Injection via Hilt

- **Scalability**: code must be designed to accommodate new game modes, features, and UI components without modifying existing code

## Mobile Development Standards

- All colors must be defined in `AppColors.kt` — no inline `Color()` or `Color.White` in composables
- All fonts must use `NunitoFontFamily` from the theme
- Compose UI must be stateless — state hoisted to ViewModel via StateFlow
- Use `Modifier` parameter as last parameter with default `Modifier` value
- Respect system insets (status bar, navigation bar) with appropriate padding

## Project Structure

- `core/` — shared domain models, repository interfaces, DI modules
- `game/` — game logic, strategies, use cases, UI screens and components
- `coop/` — co-op multiplayer feature
- `audio/` — sound and music management
- `ui/theme/` — centralized theming (AppColors, Typography, NunitoFontFamily)

## Testing

- Unit tests use MockK for mocking and Turbine for Flow testing
- Test files mirror source structure under `src/test/`
- All new business logic must have corresponding unit tests
