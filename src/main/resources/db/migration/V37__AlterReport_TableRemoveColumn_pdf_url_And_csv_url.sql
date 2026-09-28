ALTER TABLE reports
DROP COLUMN IF EXISTS url;

ALTER TABLE reports
DROP COLUMN IF EXISTS report_format;

ALTER TABLE reports
DROP COLUMN IF EXISTS pdf_url;

ALTER TABLE reports
DROP COLUMN IF EXISTS csv_url;


ALTER TABLE reports
    ADD COLUMN url VARCHAR(2048);

ALTER TABLE reports
    ADD COLUMN report_format report_format_enum NOT NULL DEFAULT 'CSV';

ALTER TABLE reports
    ALTER COLUMN report_format DROP DEFAULT;