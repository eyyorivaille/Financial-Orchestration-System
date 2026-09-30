# 0003 — Transactional outbox: Spring Modulith'in kendi registry'si, özel tablo yok

## Context

Bir event'in DB'ye yazılan state ile birlikte, aynı transaction'da garanti şekilde yayınlanması gerekiyor (klasik "dual write" problemi: DB commit olur ama Kafka'ya publish başarısız olursa event kaybolur).

## Decision

Sıfırdan bir `outbox` tablosu/poller yazılmadı. Spring Modulith'in native Event Publication Registry'si (`spring-modulith-starter-jpa`) + `spring-modulith-events-kafka` externalization'ı kullanıldı. Modulith, event'i `event_publication` tablosuna aynı transaction içinde yazar, sonra ayrı bir thread'de Kafka'ya gönderip başarılı olunca tabloyu günceller.

## Consequences

- Aynı prensip (transactional outbox) sıfırdan kod yazmadan, kütüphanenin garantisine güvenerek elde edildi.
- `event_publication` tablosu (V1 migration) gözle denetlenebilir durumda — hangi event'in hâlâ "unfinished" olduğu doğrudan sorgulanabilir.
- Bedel: Modulith'in kendi retry/polling aralığına bağımlılık; kendi outbox'ını yazsaydık bu davranış üzerinde tam kontrol olurdu. Bu proje ölçeğinde gereksiz bir esneklik.
