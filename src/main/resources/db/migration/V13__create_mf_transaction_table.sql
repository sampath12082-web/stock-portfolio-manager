CREATE TABLE mf_transaction (
    id BIGSERIAL PRIMARY KEY,
    mutual_fund_id BIGINT,
    units NUMERIC(19,4) NOT NULL,
    nav NUMERIC(19,4) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    description VARCHAR(255),
    trade_date TIMESTAMP,
    folio_number VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_mf_txn_fund FOREIGN KEY (mutual_fund_id) REFERENCES mutual_fund(id)
);

CREATE INDEX idx_mf_txn_fund ON mf_transaction(mutual_fund_id);
CREATE INDEX idx_mf_txn_date ON mf_transaction(trade_date DESC);
CREATE INDEX idx_mf_txn_type ON mf_transaction(transaction_type);
