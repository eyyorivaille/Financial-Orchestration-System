-- User profile (com.financial.project.user.internal.UserProfile). DDL verified
-- against the entity mapping by generating it with Hibernate's schema-generation
-- script action (database.action=none), same technique used for V1..V6.
-- customer_id is the Keycloak JWT "sub" claim - a natural key, not generated.
create table user_profile (
    customer_id  varchar(255) not null,
    email        varchar(255),
    display_name varchar(255),
    updated_at   datetime(6)  not null,
    primary key (customer_id)
) engine = InnoDB;
