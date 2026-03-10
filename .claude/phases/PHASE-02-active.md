# Phase 02 — Hot Potato
Status: PENDING

## Goal
Implement "Hot Potato" coop mod: real-time, timed, random bomb bubbles that give -3 points when claimed.

## Mod Rules
- Süreli (isTimed = true)
- 44 bubble'dan 6-8 tanesi rastgele "bomba" olarak işaretli
- Hangi bubble'ların bomba olduğu oyunculara gösterilmez
- Bomba bubble'a tıklayan oyuncu -3 puan alır
- Normal bubble +1 puan
- Bubble çalınabilir (Bubble Pop gibi)
- Süre bitince en yüksek skor kazanır

## Tasks
- [ ] CoopMod.HOT_POTATO enum entry + isTimed = true
- [ ] CoopModSelectionScreen: HOT_POTATO visual
- [ ] CoopBubble'a isBomb field ekle
- [ ] CoopStateManager: generateInitialCoopBubbles'da bomb placement (host belirler, GAME_START ile gönderilir)
- [ ] CoopMessage: bomb bilgisini GAME_START'ta paylaş
- [ ] CoopGameManager handleCoopClaimBubble: bomba ise -3 puan, normal ise +1 puan
- [ ] CoopMessageHandler BUBBLE_CLAIM: aynı skor mantığı
- [ ] HUD: skor negatif olabilir
- [ ] Oyun bitince bomba bubble'ları farklı renkte (kırmızı/turuncu) göster
- [ ] Bomba patladığında kısa animasyon/feedback

## Acceptance Criteria
- Oyun başladığında bombalar rastgele yerleştirilmiş
- Bomba bubble'a tıklayınca -3 puan
- Normal bubble +1 puan
- Bombalar oyun sırasında gizli, bitince görünür
- Negatif skor mümkün
