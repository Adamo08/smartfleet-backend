alter table addresses
    modify zip_code varchar(20) null;


alter table bookmarks
    alter column created_at set default ((CURDATE()));

alter table favorites
    alter column created_at set default ((CURDATE()));


alter table notifications
    alter column created_at set default ((CURDATE()));

alter table testimonials
    alter column created_at set default ((CURDATE()));

alter table users
    alter column created_at set default ((CURDATE()));

alter table users
    alter column updated_at set default ((CURDATE()));

