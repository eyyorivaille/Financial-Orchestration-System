-- Payment aggregate (com.financial.project.payment.internal.Payment). DDL
-- verified against the entity mapping by generating it with Hibernate's
-- schema-generation script action (database.action=none), same technique
-- used for V1__init_event_publication.sql.
create table payment_transaction (
    id                  binary(16)   not null,
    idempotency_key     varchar(255) not null,
    amount_minor_units  bigint       not null,
    currency            varchar(3)   not null,
    status              enum ('CANCELLED', 'COMPLETED', 'CREATED', 'FAILED', 'PENDING', 'PROCESSING') not null,
    failure_reason      varchar(255),
    created_at          datetime(6)  not null,
    updated_at          datetime(6)  not null,
    version             bigint       not null,
    primary key (id)
) engine = InnoDB;

alter table payment_transaction
    add constraint uk_payment_transaction_idempotency_key unique (idempotency_key);
