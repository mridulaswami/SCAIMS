-- V36__alter_report_table_add_reportFormat.sql

CREATE TYPE report_format_enum AS ENUM (
    'CSV',
    'PDF'
);

ALTER TABLE reports
    ADD COLUMN url VARCHAR(2048);

ALTER TABLE reports
    ADD COLUMN report_format report_format_enum NOT NULL DEFAULT 'CSV';

ALTER TABLE reports
    ALTER COLUMN report_format DROP DEFAULT;