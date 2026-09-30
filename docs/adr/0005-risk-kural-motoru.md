# 0005 — Risk motoru: basit konfigüre edilebilir kural motoru, ML/Drools yok

## Context

Gerçek risk/fraud sistemleri genellikle ML modelleri veya Drools gibi kural motorları kullanır. Bu proje için ne eğitilecek bir model ne de Drools'un getirdiği DSL/kural dosyası yönetimi karmaşıklığı gerekli.

## Decision

Üç bağımsız `RiskRule` implementasyonu (tutar limiti, ülke kısıtı, velocity/frequency), her biri kendi skorunu üretir; `RiskScoringEngine` skorları toplar ve eşik değerlere göre APPROVE/REVIEW/REJECT'e çevirir. Tüm eşikler/skorlar `application.properties`'ten `@Value` ile konfigüre edilebilir.

## Consequences

- Yeni bir kural eklemek = `RiskRule` interface'ini implemente eden yeni bir `@Component` yazmak; `RiskScoringEngine` otomatik olarak (Spring'in `List<RiskRule>` injection'ı) bunu toplar.
- REVIEW sonucu şu an APPROVE gibi işleniyor (`PaymentService` içinde açıkça yorumlanmış) — gerçek bir "manuel inceleme kuyruğu" iş akışı bu projenin kapsamı dışında bırakıldı.
