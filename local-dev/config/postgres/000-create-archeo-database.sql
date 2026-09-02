create database archeo;
\connect archeo;

create user archeo with password 'archeo';
grant all on schema public TO archeo;
