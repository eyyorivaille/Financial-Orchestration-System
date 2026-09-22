-- Settlement aggregate (com.financial.project.settlement.internal.SettlementRecord,
-- LedgerEntry). DDL verified against the entity mappings by generating it with
-- Hibernate's schema-generation script action (database.action=none), same
-- technique used for V1..V5.
create table settlement_record (
    id                      binary(16)   not null,
    payment_id              binary(16)   not null,
    customer_id             varchar(255) not null,
    amount_minor_units      bigint       not null,
    currency                varchar(3)   not null,
    status                  enum ('FAILED', 'SETTLED') not null,
    core_banking_reference  varchar(255),
    failure_reason          varchar(255),
    settled_at              datetime(6)  not null,
    primary key (id)
) engine = InnoDB;

-- One DEBIT + one CREDIT leg per settled payment (sum(debit) = sum(credit)).
create table ledger_entry (
    id                     binary(16)   not null,
    settlement_record_id   binary(16)   not null,
    account                varchar(255) not null,
    entry_type             enum ('CREDIT', 'DEBIT') not null,
    amount_minor_units     bigint       not null,
    currency               varchar(3)   not null,
    recorded_at            datetime(6)  not null,
    primary key (id)
) engine = InnoDB;

create index idx_ledger_entry_settlement_record_id on ledger_entry (settlement_record_id);
