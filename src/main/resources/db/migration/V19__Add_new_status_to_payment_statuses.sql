alter table payments
    modify status enum ('CANCELLED', 'COMPLETED', 'FAILED', 'PENDING', 'REFUNDED', 'PARTIALLY_REFUNDED') not null;

