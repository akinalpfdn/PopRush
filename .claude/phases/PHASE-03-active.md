# Phase 03 — Chain Reaction
Status: ACTIVE

## Goal
Implement "Chain Reaction" coop mod: turn-based, flood fill mechanic where claiming a bubble also claims adjacent unclaimed bubbles.

## Tasks
- [ ] CoopMod.CHAIN_REACTION enum entry + isTimed = false, isTurnBased = true
- [ ] CoopModSelectionScreen: CHAIN_REACTION visual
- [ ] CoopGameState: turn tracking (currentTurnPlayerId)
- [ ] CoopMessage: TURN_END mesaj tipi (claimedBubbleIds listesi)
- [ ] Flood fill algoritması: hex grid komşuluk + BFS yayılma
- [ ] CoopGameManager: turn-based claim + flood fill + sıra değişimi
- [ ] CoopMessageHandler: TURN_END mesajını işle
- [ ] UI: sıra göstergesi, sıra değilken tıklama engelle
- [ ] Tüm alanlar dolunca veya hamle kalmayınca oyun biter

## Acceptance Criteria
- Host ilk hamleyi yapar
- Tıklanan bubble + komşu boş bubble'lar claim edilir
- Rakibin bubble'ını geçen flood fill olmaz
- Sıra otomatik olarak karşı tarafa geçer
- Kimin sırası olduğu HUD'da gösterilir
- Sıran değilken tıklayamazsın
- Tüm alanlar dolunca oyun biter
