# 0006 — SOAP ile sahte legacy core banking entegrasyonu + double-entry ledger

## Context

Gerçek bankacılık sistemlerinde tamamlanan bir ödemenin "para hareketi" olarak defterlenmesi, genellikle eski (legacy) bir çekirdek bankacılık sistemine SOAP gibi bir protokolle yapılan bir mutabakat adımıdır. Bu, REST'in yaygınlaşmasından önceki kurumsal entegrasyon deseninin kendisi — atlanırsa "gerçek dünya bankacılığı" hedefinin önemli bir parçası kaybolur.

## Decision

- Contract-first SOAP: `settlement.xsd` → `shared.WebServiceConfig` üzerinden servis edilen bir WSDL (`/ws/settlement.wsdl`).
- Hem client (`SoapCoreBankingClient`, `WebServiceTemplate`) hem de sahte legacy sistemin kendisi (`CoreBankingSoapEndpoint`, `@Endpoint`) bu projede yazıldı — ikisi de gerçek bir HTTP/SOAP round-trip'i ile haberleşiyor (mock değil).
- Payment'ın kendi `payment_transaction` tablosu bilerek tek taraflı bir *durum* kaydı; gerçek para hareketinin defter kaydı (`sum(debit)=sum(credit)`) ayrı bir `settlement_record` + `ledger_entry` çifti olarak, sadece mutabakat başarılı olduğunda yazılıyor.

## Consequences

- WSDL/XSD/JAXB marshalling, SOAP fault üzerinden circuit-breaker tetiklenmesi gibi gerçek SOAP entegrasyon detayları uçtan uca deneyimlendi/gösterildi.
- Double-entry ledger invariant'ı (`SettlementService.handlePaymentStatusChanged`, `@Transactional`) korunuyor: settlement_record ile ledger_entry çifti birlikte ya yazılır ya hiç yazılmaz.
- `/ws/**` kimlik doğrulamasından muaf tutuldu (`SecurityConfig`) — gerçek bir sistemde bu bacak mTLS/özel ağ ile izole edilirdi, demo kapsamında basit tutuldu.
