-- 1. Drop existing table if exists
DROP TABLE IF EXISTS reports;

-- 2. Drop existing enum types if exists
DROP TYPE IF EXISTS report_format_enum;
DROP TYPE IF EXISTS report_status_enum;
DROP TYPE IF EXISTS report_type_enum;


-- 3. Create PostgreSQL Enum types

CREATE TYPE report_type_enum AS ENUM (
    'ASSET_REPORT',
    'COMPLAINT_REPORT',
    'WORK_ORDER_REPORT'
);

CREATE TYPE report_status_enum AS ENUM (
    'IN_PROGRESS',
    'COMPLETED',
    'FAILED'
);

CREATE TYPE report_format_enum AS ENUM (
    'CSV',
    'PDF',
    'EXCEL'
);


-- 4. Create reports table

CREATE TABLE reports
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    report_type         report_type_enum   NOT NULL,

    status              report_status_enum NOT NULL,

    report_name         VARCHAR(255)       NOT NULL,

    url                 VARCHAR(2048),

    report_format       report_format_enum NOT NULL,

    created_by          UUID               NOT NULL,

    report_initiated_at TIMESTAMP          NOT NULL,

    report_completed_at TIMESTAMP,

    from_date           DATE,

    to_date             DATE,

    error_message       TEXT,

    created_at           TIMESTAMP          NOT NULL,

    updated_at           TIMESTAMP          NOT NULL,

    CONSTRAINT fk_reports_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id)
            ON DELETE RESTRICT
);


-- 5. Indexes

CREATE INDEX idx_reports_created_by
    ON reports (created_by);

CREATE INDEX idx_reports_report_type
    ON reports (report_type);

CREATE INDEX idx_reports_status
    ON reports (status);

CREATE INDEX idx_reports_created_at
    ON reports (created_at);