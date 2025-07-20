alter table slots
    add constraint chk_time_order
        check (start_time < end_time);

