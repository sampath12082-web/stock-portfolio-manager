CREATE TABLE portfolio_snapshot (
    id BIGSERIAL PRIMARY KEY,
    snapshot_date DATE NOT NULL UNIQUE,
    total_investment NUMERIC(19,2) NOT NULL,
    current_value NUMERIC(19,2) NOT NULL,
    total_pnl NUMERIC(19,2) NOT NULL,
    total_pnl_percentage NUMERIC(10,2) NOT NULL,
    holding_count INTEGER,
    top_gainer VARCHAR(20),
    top_loser VARCHAR(20),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_snapshot_date ON portfolio_snapshot(snapshot_date DESC);
