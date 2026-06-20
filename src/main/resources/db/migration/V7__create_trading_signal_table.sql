CREATE TABLE trading_signal (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT,
    symbol VARCHAR(20) NOT NULL,
    signal_type VARCHAR(20) NOT NULL,
    target_price NUMERIC(19,2),
    stop_loss NUMERIC(19,2),
    current_price NUMERIC(19,2),
    rationale TEXT,
    signal_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notes TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_signal_stock FOREIGN KEY (stock_id) REFERENCES stock(id)
);

CREATE INDEX idx_signal_date ON trading_signal(signal_date DESC);
CREATE INDEX idx_signal_status ON trading_signal(status);
CREATE INDEX idx_signal_symbol ON trading_signal(symbol);
