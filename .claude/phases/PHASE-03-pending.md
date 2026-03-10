# Phase 03 — Chain Reaction
Status: PENDING

## Goal
Implement "Chain Reaction" coop mod: turn-based, flood fill mechanic where claiming a bubble also claims adjacent unclaimed bubbles.

## Mod Rules
- Süresiz, turn-based
- Sırayla oynanır (host ilk hamle)
- Bir bubble'a tıklayınca: o bubble + komşu BOŞ bubble'lar otomatik claim edilir (flood fill)
- Sadece boş (owner == null) bubble'lar flood fill'e dahil — rakibin bubble'ını geçemez
- Geri alma yok (Territory War gibi, claim edilen geri alınamaz)
- Tüm alanlar dolunca veya hamle kalmayınca oyun biter
- En çok alan kazanır

## Tasks
- [ ] CoopMod.CHAIN_REACTION enum entry + isTimed = false
- [ ] CoopModSelectionScreen: CHAIN_REACTION visual
- [ ] CoopGameState: turn tracking (isLocalPlayerTurn, currentTurnPlayerId)
- [ ] CoopMessage: TURN_END mesaj tipi (hangi bubble tıklandı + flood fill sonuçları)
- [ ] Flood fill algoritması: tıklanan bubble'dan komşu boş bubble'lara yayılma
- [ ] Hexagonal grid komşuluk hesaplaması (row/col bazlı)
- [ ] CoopGameManager: turn-based claim + flood fill + sıra değişimi
- [ ] CoopMessageHandler: TURN_END mesajını işle
- [ ] UI: sıra göstergesi (kimin sırası), sıra değilken tıklama engelle
- [ ] CoopGameplayScreen: turn-based modda HUD'da sıra bilgisi göster
- [ ] Tüm alanlar dolunca oyun biter

## Acceptance Criteria
- Host ilk hamleyi yapar
- Tıklanan bubble + komşu boş bubble'lar claim edilir
- Rakibin bubble'ını geçen flood fill olmaz
- Sıra otomatik olarak karşı tarafa geçer
- Kimin sırası olduğu HUD'da gösterilir
- Sıran değilken tıklayamazsın
- Tüm alanlar dolunca oyun biter
