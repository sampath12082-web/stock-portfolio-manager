CREATE TABLE mf_holding (
    id BIGSERIAL PRIMARY KEY,
    mutual_fund_id BIGINT NOT NULL,
    units NUMERIC(19,4) NOT NULL,
    average_nav NUMERIC(19,4) NOT NULL,
    invested_amount NUMERIC(19,2) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_mf_holding_fund FOREIGN KEY (mutual_fund_id) REFERENCES mutual_fund(id)
);

CREATE INDEX idx_mf_holding_fund ON mf_holding(mutual_fund_id);
