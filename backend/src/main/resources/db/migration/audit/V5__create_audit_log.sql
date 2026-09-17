-- Append-only audit trail (com.financial.project.audit.internal.AuditLog).
-- One row per payment-events message, regardless of type - a complete record
-- of everything that happened, independent of Payment's/Notification's own
-- tables. DDL verified against the entity mapping via Hibernate's schema-
-- generation script action, same technique as V1-V4. payload is explicitly
-- "text" (not tinytext, which is @Lob's default MySQL mapping and far too
-- small at 255 bytes for a serialized event's JSON).
create table audit_log (
    id           binary(16)   not null,
    event_type   varchar(255) not null,
    actor        varchar(255) not null,
    payload      text         not null,
    occurred_at  datetime(6)  not null,
    recorded_at  datetime(6)  not null,
    primary key (id)
) engine = InnoDB;

create index idx_audit_log_event_type_actor on audit_log (event_type, actor);
