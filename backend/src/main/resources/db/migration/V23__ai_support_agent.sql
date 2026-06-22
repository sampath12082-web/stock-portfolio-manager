-- AI Support Agent: extend support_ticket + create bug_report + ticket_activity

ALTER TABLE support_ticket
    ADD COLUMN ticket_type VARCHAR(30) DEFAULT 'INQUIRY',
    ADD COLUMN ai_response TEXT,
    ADD COLUMN ai_reviewed_at TIMESTAMP,
    ADD COLUMN priority VARCHAR(10) DEFAULT 'MEDIUM',
    ADD COLUMN bug_report_id BIGINT;

CREATE TABLE bug_report (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES support_ticket(id),
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    test_suite VARCHAR(100),
    test_result TEXT,
    test_passed BOOLEAN,
    test_run_at TIMESTAMP,
    admin_notes TEXT,
    approved_at TIMESTAMP,
    estimated_fix_hours INTEGER,
    estimated_fix_description TEXT,
    fixed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE ticket_activity (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES support_ticket(id),
    actor VARCHAR(20) NOT NULL,
    action VARCHAR(50) NOT NULL,
    detail TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE support_ticket
    ADD CONSTRAINT fk_bug_report FOREIGN KEY (bug_report_id) REFERENCES bug_report(id);
