create table nonce
(
    address   text,
    nonce     text,
    issued_at integer,
    primary key (address, nonce)
);
