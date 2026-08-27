
-- =========================================================
-- Complaint Status Audits
-- =========================================================

CREATE TABLE complaint_status_audits (
    id UUID NOT NULL,
    previous_status VARCHAR(50) NOT NULL,
    current_status VARCHAR(50) NOT NULL,
    change_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    changed_by UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,

    CONSTRAINT pk_complaint_status_audits
        PRIMARY KEY (id),

    CONSTRAINT fk_complaint_status_audits_changed_by
        FOREIGN KEY (changed_by)
        REFERENCES users(id)
);


-- =========================================================
-- Work Order Status Audits
-- =========================================================

CREATE TABLE work_order_status_audits (
    id UUID NOT NULL,
    previous_status VARCHAR(50) NOT NULL,
    current_status VARCHAR(50) NOT NULL,
    change_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    changed_by UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,

    CONSTRAINT pk_work_order_status_audits
        PRIMARY KEY (id),

    CONSTRAINT fk_work_order_status_audits_changed_by
        FOREIGN KEY (changed_by)
        REFERENCES users(id)
);


-- =========================================================
-- Indexes
-- =========================================================

CREATE INDEX idx_complaint_status_audits_changed_by
    ON complaint_status_audits(changed_by);

CREATE INDEX idx_complaint_status_audits_change_at
    ON complaint_status_audits(change_at);

CREATE INDEX idx_work_order_status_audits_changed_by
    ON work_order_status_audits(changed_by);

CREATE INDEX idx_work_order_status_audits_change_at
    ON work_order_status_audits(change_at);
