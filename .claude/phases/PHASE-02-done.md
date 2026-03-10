# Phase 02 — Hot Potato
Status: DONE

## Goal
Implement "Hot Potato" coop mod: real-time, timed, random bomb bubbles that give -3 points when claimed.

## Tasks
- [x] CoopMod.HOT_POTATO enum entry + isTimed = true
- [x] CoopModSelectionScreen: HOT_POTATO visual
- [x] CoopBubble'a isBomb field ekle
- [x] CoopStateManager: bomb placement (host belirler, GAME_START ile gönderilir)
- [x] CoopMessage: bomb bilgisini GAME_START'ta paylaş
- [x] CoopGameManager handleCoopClaimBubble: count-based scoring + bomb penalty tracking
- [x] CoopMessageHandler BUBBLE_CLAIM: aynı skor mantığı
- [x] HUD: skor negatif olabilir
- [x] Oyun bitince bomba bubble'ları ROSE renkte göster
- [x] Bomba patladığında titreşim + "-3" floating text animasyonu

## Decisions Made This Phase
- Scoring model: score = bubbles held at end - (bombs triggered × 3)
- Bubbles are reclaimable (like Bubble Pop)
- Bombs deactivate after first claim (single-use)
- Blind Mode card color changed from Peach to Indigo for better text contrast
