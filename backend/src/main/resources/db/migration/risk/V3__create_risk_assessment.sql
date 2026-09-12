-- Risk assessment audit trail (com.financial.project.risk.internal.RiskAssessment).
-- One row per assess() call, regardless of outcome, so the velocity rule can
-- count recent assessments per customer and so REJECT/REVIEW decisions leave
-- an audit trail. DDL verified against the entity mapping via Hibernate's
-- schema-generation script action, same technique as V1/V2.
create table risk_assessment (
    id                  binary(16)   not null,
    customer_id         varchar(255) not null,
    amount_minor_units  bigint       not null,
    currency            varchar(3)   not null,
    country_code        varchar(2)   not null,
    score               integer      not null,
    outcome             enum ('APPROVE', 'REJECT', 'REVIEW') not null,
    reason              varchar(255),
    assessed_at         datetime(6)  not null,
    primary key (id)
) engine = InnoDB;

create index idx_risk_assessment_customer_assessed_at
    on risk_assessment (customer_id, assessed_at);
