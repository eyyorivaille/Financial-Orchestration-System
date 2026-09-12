-- Spring Modulith's JPA event publication registry (transactional outbox for
-- ApplicationModuleListener / Kafka externalization). Schema mirrors what
-- spring-modulith-events-jpa 2.1.1 generates for MySQL, with serialized_event
-- widened from Hibernate's default varchar(255) to text (event payloads are
-- serialized JSON and will routinely exceed 255 chars).
create table event_publication (
    id                      binary(16)   not null,
    listener_id             varchar(255) not null,
    event_type              varchar(255) not null,
    serialized_event        text         not null,
    publication_date        datetime(6)  not null,
    completion_date         datetime(6),
    completion_attempts     integer      not null,
    last_resubmission_date  datetime(6),
    status                  enum ('COMPLETED', 'FAILED', 'PROCESSING', 'PUBLISHED', 'RESUBMITTED'),
    primary key (id)
) engine = InnoDB;
