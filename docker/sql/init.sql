-- Database
CREATE DATABASE todo;
\c todo;

CREATE TABLE notes (
    id SERIAL,
    description VARCHAR NOT NULL UNIQUE,
    importance TEXT,
    PRIMARY KEY(id)
);
