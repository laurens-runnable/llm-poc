create database seaweed;
\connect seaweed;

create user seaweed with password 'seaweed';
grant all on schema public TO seaweed;

create table if not exists filemeta
(
    dirhash   bigint,
    name      varchar(65535),
    directory varchar(65535),
    meta      bytea,
    primary key (dirhash, name)
);

grant all on all tables in schema public TO seaweed;
