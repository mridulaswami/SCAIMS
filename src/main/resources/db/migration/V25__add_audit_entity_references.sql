-- =========================================================
-- V12: Add Complaint and Work Order references
-- to status audit tables
-- =========================================================


-- =========================================================
-- Complaint Status Audit
-- =========================================================

ALTER TABLE complaint_status_audits
    ADD COLUMN complaint_id UUID NOT NULL;

ALTER TABLE complaint_status_audits
    ADD CONSTRAINT fk_complaint_status_audits_complaint
        FOREIGN KEY (complaint_id)
            REFERENCES complaint (id);


-- =========================================================
-- Work Order Status Audit
-- =========================================================

ALTER TABLE work_order_status_audits
    ADD COLUMN work_order_id UUID NOT NULL;

ALTER TABLE work_order_status_audits
    ADD CONSTRAINT fk_work_order_status_audits_work_order
        FOREIGN KEY (work_order_id)
            REFERENCES work_order (id);


-- =========================================================
-- Indexes
-- =========================================================

CREATE INDEX idx_complaint_status_audits_complaint_id
    ON complaint_status_audits (complaint_id);

CREATE INDEX idx_work_order_status_audits_work_order_id
    ON work_order_status_audits (work_order_id);

