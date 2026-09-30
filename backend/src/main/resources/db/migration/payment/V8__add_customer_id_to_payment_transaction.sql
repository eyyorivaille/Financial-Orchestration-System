-- payment_transaction never stored who a payment belongs to (customer_id only
-- ever flowed through events), which made ownership checks impossible - any
-- authenticated caller could read any payment by id. Default '' lets this
-- apply to the existing (dev-only, disposable) rows; every new row supplies a
-- real value via Payment.create, so the default is never relied on going forward.
alter table payment_transaction
    add column customer_id varchar(255) not null default '';

alter table payment_transaction
    alter column customer_id drop default;
