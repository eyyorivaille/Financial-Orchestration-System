# 0004 — Sahte ödeme sağlayıcısı + Resilience4j circuit breaker

## Context

Gerçek bir ödeme sağlayıcısına (Stripe vb.) bağlanmak sandbox hesabı, API anahtarı yönetimi gibi bu proje için gereksiz bir dış bağımlılık getirir. Ama "dış bir bağımlılık arızalanabilir" senaryosunu göstermek gerçek bankacılık sistemlerinin ayrılmaz bir parçası.

## Decision

`FakePaymentProviderClient`, belirli bir "chaos" tutarında (`999_999`) deterministik olarak hata fırlatan, aksi halde her zaman başarılı olan sahte bir implementasyon. Çağrı `@CircuitBreaker(name="paymentProvider")` ile korunuyor; devre açıldığında fallback metodu ödemeyi FAILED olarak işaretliyor (Payment'ın state machine'i bunu zaten destekliyor).

Aynı desen `settlement` modülünde de (farklı bir chaos tutarıyla, `888_888`) SOAP çağrısı için tekrarlandı.

## Consequences

- Gerçek bir sandbox API'sine bağımlı olmadan circuit breaker'ın gerçekten devreye girdiği, göstermeye değer bir senaryo test edilebilir ve canlı demo edilebilir.
- Chaos tutarları ayrı seçildi (Payment: 999_999, Settlement: 888_888) ki bir ödeme sağlayıcıda başarılı olup mutabakatta bağımsız olarak başarısız olabilsin — iki farklı arıza noktası ayrı ayrı gösterilebiliyor.
