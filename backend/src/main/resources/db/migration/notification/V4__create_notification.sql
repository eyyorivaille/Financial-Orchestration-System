-- Notification audit trail (com.financial.project.notification.internal.Notification).
-- payment_id is a plain UUID column, not a cross-module foreign key - Payment and
-- Notification stay loosely coupled per the modular monolith's module boundaries.
-- DDL verified against the entity mapping via Hibernate's schema-generation script
-- action, same technique as V1/V2/V3.
create table notification (
    id           binary(16)   not null,
    payment_id   binary(16)   not null,
    customer_id  varchar(255) not null,
    channel      enum ('EMAIL') not null,
    recipient    varchar(255) not null,
    subject      varchar(255) not null,
    message      varchar(255) not null,
    status       enum ('SENT', 'FAILED') not null,
    created_at   datetime(6)  not null,
    primary key (id)
) engine = InnoDB;

create index idx_notification_payment_id on notification (payment_id);
