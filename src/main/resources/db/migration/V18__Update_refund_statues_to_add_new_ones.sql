alter table refunds
    modify status enum (
        'REQUESTED',
        'DECLINED',
        'PENDING',
        'PROCESSED',
        'PARTIALLY_PROCESSED',
        'FAILED'
    ) not null;

