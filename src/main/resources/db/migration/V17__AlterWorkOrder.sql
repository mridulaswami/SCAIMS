ALTER TABLE work_order
    RENAME COLUMN description TO work_report;

ALTER TABLE work_order
    ALTER COLUMN work_report DROP NOT NULL;