# Phase 01 — Blind Mode
Status: DONE

## Goal
Implement "Blind Mode" coop mod: real-time, timed, opponent's bubbles invisible until game ends.

## Tasks
- [x] CoopMod.BLIND_MODE enum entry + isTimed = true + isBlind property
- [x] CoopModSelectionScreen: BLIND_MODE visual (VisibilityOff icon, Peach renk)
- [x] CoopGameplayScreen HUD: Blind modda rakip skorunu "?" göster
- [x] CoopGameStateExtensions toGameState(): Blind modda rakip bubble'larını gri göster
- [x] Oyun bitince (FINISHED) tüm bubble'lar gerçek renkleriyle gösterilir (hideOpponent = false when FINISHED)
