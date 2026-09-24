-- 1. create postgreSQL Enum types
CREATE TYPE report_type_enum AS ENUM(
    'ASSET_REPORT',
    'COMPLAINT_REPORT',
    'WORK_ORDER_REPORT'
);

CREATE TYPE report_status_enum as ENUM(
      'IN_PROGRESS',
    'COMPLETED',
    'FAILED'
);

-- 2. Create reports table
CREATE TABLE reports
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    report_type         report_type_enum   NOT NULL,

    status              report_status_enum NOT NULL,

    report_name         VARCHAR(255)       NOT NULL,

    csv_url             VARCHAR(255),

    pdf_url             VARCHAR(255),

    created_by          UUID               NOT NULL,

    report_initiated_at TIMESTAMP          NOT NULL,

    report_completed_at TIMESTAMP,

    from_date           DATE,

    to_date             DATE,

    error_message       TEXT,

    created_at          TIMESTAMP          NOT NULL,

    updated_at          TIMESTAMP          NOT NULL,

    CONSTRAINT fk_reports_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id)
            ON DELETE RESTRICT
);


-- 3. Indexes
CREATE INDEX idx_reports_created_by
    ON reports (created_by);

CREATE INDEX idx_reports_report_type
    ON reports (report_type);

CREATE INDEX idx_reports_status
    ON reports (status);

CREATE INDEX idx_reports_created_at
    ON reports (created_at);