# 0001 — Modül içi iletişim senkron, modüller arası dışa akış Kafka ile

## Context

Payment; Risk'e bir karar sormak zorunda (APPROVE/REVIEW/REJECT) ve bu kararı aynı istek içinde, aynı transaction'da kullanmak zorunda. Buna karşılık Notification, Audit ve Settlement, Payment'ın sonucunu sadece *öğrenen*, Payment'ın kendisinin hiç bilmesi gerekmeyen bağımsız tüketicilerdir.

## Decision

- Payment ↔ Risk: Spring Modulith'in `ApplicationEventPublisher` / `@ApplicationModuleListener`'ı üzerinden **senkron**, aynı JVM/transaction içinde çağrı. Aralarında Kafka yok.
- Payment → Notification / Audit / Settlement: Spring Modulith'in event externalization'ı (`@Externalized`) ile **Kafka**'ya (`payment-events` topic) yayın. Her tüketici kendi `groupId`'siyle topic'in tam bir kopyasını alır.

## Consequences

- Risk kararı olmadan Payment ilerleyemez — senkron çağrı bunu doğal olarak garanti eder, ek bir "bekleme" mekanizması gerekmez.
- Notification/Audit/Settlement'tan biri çökse veya yavaş olsa Payment'ın kendi akışı hiç etkilenmez (gevşek bağlılık).
- Yeni bir tüketici eklemek (örn. gelecekte bir "fraud-ml" modülü) Payment'a hiç dokunmadan, sadece `payment-events`'e yeni bir consumer group eklemekle mümkün.
- Bedel: Notification/Audit/Settlement'ın gördüğü veri, Payment'ın *event yayınladığı andaki* kopyası — gerçek zamanlı tutarlılık değil, "eventually consistent" bir model.
